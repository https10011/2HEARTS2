#!/usr/bin/env python3
"""Exercise a signed TwoHearts APK on a disposable Android emulator."""

import argparse
from pathlib import Path
import re
import subprocess
import time
import xml.etree.ElementTree as ET

PACKAGE = "com.twohearts.app"
ACTIVITY = f"{PACKAGE}/.MainActivity"
FIXTURE_NOTE = "Release fixture note"


def adb(*args, timeout=40, check=True):
    return subprocess.run(
        ["adb", *map(str, args)], timeout=timeout, check=check,
        stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
    ).stdout


class Rehearsal:
    def __init__(self, evidence):
        self.evidence = evidence
        evidence.mkdir(parents=True, exist_ok=True)

    def dump(self):
        result = adb("shell", "uiautomator", "dump", "/sdcard/twohearts-ui.xml", timeout=20, check=False)
        if b"dumped to" not in result:
            return ET.Element("unavailable")
        raw = adb("exec-out", "cat", "/sdcard/twohearts-ui.xml")
        return ET.fromstring(raw)

    def find(self, predicate, description, seconds=35):
        deadline = time.monotonic() + seconds
        while time.monotonic() < deadline:
            tree = self.dump()
            for node in tree.iter("node"):
                if predicate(node):
                    return node
            time.sleep(0.35)
        (self.evidence / "last-ui.xml").write_text(ET.tostring(tree, encoding="unicode"))
        raise AssertionError(f"Could not find {description}")

    @staticmethod
    def node_labels(node):
        return tuple(node.get(name, "") for name in ("text", "content-desc"))

    @staticmethod
    def tap_node(node):
        coordinates = [int(value) for value in re.findall(r"-?\d+", node.get("bounds", ""))]
        assert len(coordinates) == 4, node.attrib
        x1, y1, x2, y2 = coordinates
        assert x2 > x1 and y2 > y1, node.attrib
        adb("shell", "input", "tap", (x1 + x2) // 2, (y1 + y2) // 2)

    def see(self, label):
        return self.find(lambda node: label in self.node_labels(node), label)

    def tap(self, label):
        self.tap_node(self.see(label))

    def tap_prefix(self, prefix):
        self.tap_node(self.find(
            lambda node: any(value.startswith(prefix) for value in self.node_labels(node)),
            prefix,
        ))

    def enter(self, value, index=0):
        field = self.find(lambda node: node.get("class") == "android.widget.EditText", "text field")
        if index:
            fields = [node for node in self.dump().iter("node") if node.get("class") == "android.widget.EditText"]
            field = fields[index]
        self.tap_node(field)
        adb("shell", "input", "text", value.replace(" ", "%s"))
        if b"mInputShown=true" in adb("shell", "dumpsys", "input_method"):
            self.back()

    @staticmethod
    def back():
        adb("shell", "input", "keyevent", "KEYCODE_BACK")

    def capture(self, name):
        png = adb("exec-out", "screencap", "-p")
        assert png.startswith(b"\x89PNG\r\n\x1a\n"), name
        (self.evidence / f"{name}.png").write_bytes(png)

    def launch(self, name, cold=False):
        if cold:
            adb("shell", "am", "force-stop", PACKAGE)
        result = adb("shell", "am", "start", "-W", "-n", ACTIVITY)
        (self.evidence / f"{name}.txt").write_bytes(result)
        assert b"Status: ok" in result and b"Error:" not in result, result.decode()
        assert adb("shell", "pidof", PACKAGE).strip(), "TwoHearts process is missing"

    def install(self, apk, upgrade=False):
        result = adb("install", *(["-r"] if upgrade else []), apk, timeout=120)
        assert b"Success" in result, result.decode()

    def onboard(self, is_baseline=False):
        self.tap("Begin")
        if not is_baseline:
            self.tap("Choose photo")
            self.find(
                lambda node: any(part in node.get("package", "") for part in ("providers.media", "documentsui", "photopicker")),
                "Android photo selection window",
            )
            self.capture("system-photo-picker")
            self.back()
        self.enter("Avery")
        self.tap("Continue")
        self.enter("Sam")
        self.tap_prefix("The day you count from:")
        self.tap_prefix("Use ")
        self.tap("Continue")
        self.tap("Continue")
        if not is_baseline:
            self.tap("Allow alerts")
            self.tap_node(self.find(
                lambda node: any(label.replace("’", "'") == "Don't allow" for label in self.node_labels(node)),
                "Android notification denial button",
            ))
            self.tap("Continue without alerts")
            self.enter("4826")
            self.enter("4826", index=1)
            self.tap("Set the PIN")
        else:
            self.tap("Not now")
        self.tap("Open TwoHearts")
        self.see("Avery")
        self.see("Sam")

    def unlock(self):
        self.see("TwoHearts is locked")
        self.capture("locked-cold-start")
        self.enter("4826")
        self.tap("Unlock")
        self.see("Avery")

    def add_note(self):
        self.tap("Notes")
        self.tap("Write your first note")
        self.enter(FIXTURE_NOTE)
        self.enter("Saved on Android before the update.", index=1)
        self.tap("Keep this note")
        self.see(FIXTURE_NOTE)

    def yuki(self):
        self.tap_prefix("Us")
        for _ in range(4):
            tree = self.dump()
            if any("Yuki" in self.node_labels(node) for node in tree.iter("node")):
                break
            match = re.findall(r"size: (\d+)x(\d+)", adb("shell", "wm", "size").decode())[-1]
            width, bottom = map(int, match)
            x = width // 2
            adb("shell", "input", "swipe", x, bottom * 4 // 5, x, bottom // 4, 350)
        self.tap("Yuki")
        self.see("Yuki’s room")
        self.capture("yuki-room")
        self.tap("Pet")
        self.see("Yuki’s room")
        adb("shell", "input", "keyevent", "KEYCODE_HOME")
        self.launch("yuki-warm-launch")
        self.see("Yuki’s room")
        self.capture("yuki-after-resume")

    def diagnostics(self):
        commands = {
            "crash-buffer": ("logcat", "-b", "crash", "-d"),
            "activity-exits": ("shell", "dumpsys", "activity", "exit-info", PACKAGE),
            "package": ("shell", "dumpsys", "package", PACKAGE),
            "runtime-api": ("shell", "getprop", "ro.build.version.sdk"),
            "frame-stats": ("shell", "dumpsys", "gfxinfo", PACKAGE, "framestats"),
        }
        for name, command in commands.items():
            (self.evidence / f"{name}.txt").write_bytes(adb(*command, check=False))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("apk", type=Path)
    parser.add_argument("evidence", type=Path)
    parser.add_argument("--full", action="store_true")
    parser.add_argument("--baseline", type=Path)
    parser.add_argument("--upgrade", type=Path)
    args = parser.parse_args()
    if bool(args.baseline) != bool(args.upgrade):
        parser.error("supply both --baseline and --upgrade")
    assert adb("get-serialno").strip().startswith(b"emulator-"), "A disposable emulator is required"
    assert PACKAGE.encode() not in adb("shell", "pm", "list", "packages", PACKAGE), "An empty emulator is required"
    adb("shell", "settings", "put", "secure", "show_ime_with_hard_keyboard", "1")
    rehearsal = Rehearsal(args.evidence)
    try:
        rehearsal.install(args.apk)
        rehearsal.launch("fresh-launch", cold=True)
        rehearsal.see("Begin")
        rehearsal.capture("welcome")
        if args.full:
            rehearsal.onboard()
            rehearsal.capture("home")
            rehearsal.add_note()
            rehearsal.launch("returning-launch", cold=True)
            rehearsal.unlock()
            rehearsal.tap("Notes")
            rehearsal.see(FIXTURE_NOTE)
            rehearsal.yuki()
        else:
            adb("shell", "input", "keyevent", "KEYCODE_HOME")
            rehearsal.launch("warm-launch")
            rehearsal.see("Begin")
        if args.baseline:
            assert b"Success" in adb("uninstall", PACKAGE)
            rehearsal.install(args.baseline)
            rehearsal.launch("baseline-launch", cold=True)
            rehearsal.onboard(is_baseline=True)
            rehearsal.add_note()
            rehearsal.install(args.upgrade, upgrade=True)
            rehearsal.launch("retained-data-launch", cold=True)
            rehearsal.see("Avery")
            rehearsal.see("Sam")
            rehearsal.tap("Notes")
            rehearsal.see(FIXTURE_NOTE)
            rehearsal.capture("retained-note")
    finally:
        rehearsal.diagnostics()
    crash = (args.evidence / "crash-buffer.txt").read_text(errors="replace")
    assert f"Process: {PACKAGE}," not in crash, "Android recorded a fatal TwoHearts exception"
    print("Android release rehearsal passed")


if __name__ == "__main__":
    main()
