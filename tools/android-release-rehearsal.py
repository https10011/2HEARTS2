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

    def reveal(self, label, *, prefix=False):
        width, height = map(int, re.findall(r"size: (\d+)x(\d+)", adb("shell", "wm", "size").decode())[-1])
        for _ in range(4):
            for node in self.dump().iter("node"):
                texts = self.node_labels(node)
                if (prefix and any(text.startswith(label) for text in texts)) or label in texts:
                    return
            adb("shell", "input", "swipe", width // 2, height * 4 // 5, width // 2, height // 4, 350)

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

    def background(self):
        activity = adb(
            "shell", "cmd", "package", "resolve-activity", "--brief",
            "-a", "android.intent.action.MAIN", "-c", "android.intent.category.HOME",
        ).decode().strip().splitlines()[-1]
        assert "/" in activity, f"No Android launcher resolved: {activity}"
        launcher = activity.split("/", 1)[0]
        adb("shell", "input", "keyevent", "KEYCODE_HOME")
        self.find(lambda node: node.get("package") == launcher, "the Android launcher")

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

    def capture_entry_sequence(self):
        """Sample a separate cold start so screenshot work cannot skew the timed launch."""
        self.background()
        adb("shell", "am", "force-stop", PACKAGE)
        with subprocess.Popen(
            ["adb", "shell", "am", "start", "-W", "-n", ACTIVITY],
            stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
        ) as launch:
            for index in range(8):
                self.capture(f"entry-{index:02d}")
                if launch.poll() is not None:
                    break
            result, _ = launch.communicate(timeout=40)
        assert launch.returncode == 0 and b"Status: ok" in result, result
        self.see("Begin")

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
        if is_baseline:
            self.reveal("Use ", prefix=True)
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

    def add_note(self, is_baseline=False):
        self.tap("Notes")
        self.tap("Write your first note")
        self.enter(FIXTURE_NOTE)
        if is_baseline:
            self.reveal("Content")
        self.enter("Saved on Android before the update.", index=-1)
        self.tap("Save" if is_baseline else "Keep this note")
        self.see(FIXTURE_NOTE)

    def yuki(self):
        self.tap_prefix("Us")
        self.reveal("Yuki")
        self.tap("Yuki")
        self.see("Yuki’s room")
        self.capture("yuki-room")
        self.tap("Pet")
        self.see("Yuki’s room")
        self.background()
        self.launch("yuki-warm-launch")
        self.see("Yuki’s room")
        self.capture("yuki-after-resume")

    def instrument(self, test_apk):
        self.install(test_apk)
        result = adb(
            "shell", "am", "instrument", "-w", "-r",
            "-e", "class", f"{PACKAGE}.ReleaseRuntimeTest",
            f"{PACKAGE}.test/androidx.test.runner.AndroidJUnitRunner",
            timeout=170, check=False,
        )
        (self.evidence / "instrumentation.txt").write_bytes(result)
        video = adb("exec-out", "run-as", PACKAGE, "cat", "files/rehearsal-video.png", check=False)
        if video.startswith(b"\x89PNG\r\n\x1a\n"):
            (self.evidence / "decoder-playback.png").write_bytes(video)
        assert b"OK (2 tests)" in result, result.decode(errors="replace")

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
    parser.add_argument("--upgrade-fixtures", type=Path, help="Directory with baseline.apk and upgrade.apk for API 33")
    parser.add_argument("--instrumentation", type=Path, help="Android test APK signed together with the candidate")
    args = parser.parse_args()
    assert adb("get-serialno").strip().startswith(b"emulator-"), "A disposable emulator is required"
    assert PACKAGE.encode() not in adb("shell", "pm", "list", "packages", PACKAGE), "An empty emulator is required"
    api = int(adb("shell", "getprop", "ro.build.version.sdk").strip())
    upgrade_fixtures = args.upgrade_fixtures if api == 33 else None
    if upgrade_fixtures:
        for name in ("baseline.apk", "upgrade.apk"):
            assert (upgrade_fixtures / name).is_file(), f"Missing upgrade fixture {name}"
    adb("shell", "settings", "put", "secure", "show_ime_with_hard_keyboard", "1")
    rehearsal = Rehearsal(args.evidence)
    try:
        rehearsal.install(args.apk)
        rehearsal.launch("fresh-launch", cold=True)
        rehearsal.see("Begin")
        rehearsal.capture("welcome")
        rehearsal.capture_entry_sequence()
        if api == 33:
            rehearsal.onboard()
            rehearsal.capture("home")
            print("Onboarding and Android permission denial passed", flush=True)
            rehearsal.add_note()
            rehearsal.launch("returning-launch", cold=True)
            rehearsal.unlock()
            rehearsal.tap("Notes")
            rehearsal.see(FIXTURE_NOTE)
            rehearsal.yuki()
            print("Cold PIN gate, local note, and Yuki return passed", flush=True)
            if args.instrumentation:
                rehearsal.instrument(args.instrumentation)
        else:
            rehearsal.background()
            rehearsal.launch("warm-launch")
            rehearsal.see("Begin")
        if upgrade_fixtures:
            assert b"Success" in adb("uninstall", PACKAGE)
            rehearsal.install(upgrade_fixtures / "baseline.apk")
            rehearsal.launch("baseline-launch", cold=True)
            rehearsal.onboard(is_baseline=True)
            rehearsal.add_note(is_baseline=True)
            print("Populated the schema-13 fixture", flush=True)
            rehearsal.install(upgrade_fixtures / "upgrade.apk", upgrade=True)
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
