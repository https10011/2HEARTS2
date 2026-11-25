#!/usr/bin/env python3
"""Register and remove the baked checker matte from the supplied Yuki artwork."""

import argparse
import hashlib
import io
import json
import math
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw
from scipy import ndimage

ASSETS = Path(__file__).resolve().parents[1] / "Yuki Assets"
OUTPUT = ASSETS / "Runtime"


def extract_cat(source):
    rgb = np.asarray(source.convert("RGB"))
    channels = rgb.astype(np.int16)
    chroma = channels.max(axis=2) - channels.min(axis=2)
    labels, count = ndimage.label((chroma >= 25) | (channels.max(axis=2) < 191))
    if not count:
        raise ValueError("The source contains no cat silhouette")
    areas = np.bincount(labels.ravel())
    areas[0] = 0
    outline = labels == areas.argmax()

    # Keep the cream fur inside the outline. Transparent gaps between limbs
    # contain checker values at multiple levels, including the darker squares.
    enclosed, count = ndimage.label(ndimage.binary_fill_holes(outline) & ~outline)
    for index, region in enumerate(ndimage.find_objects(enclosed, max_label=count), start=1):
        patch = enclosed[region] == index
        neutral = channels[region][patch].mean(axis=1)
        if neutral.size < 12 or np.percentile(neutral, 10) > 234 or np.ptp(neutral) < 10:
            outline[region] |= patch

    # At the contour extend the nearest real colour into fractional alpha.
    # This carries the brown linework through scaling and avoids a pale halo.
    alpha = ndimage.gaussian_filter(outline.astype(np.float32), 0.76)
    fringe = (alpha > 0) & ~outline
    nearest = ndimage.distance_transform_edt(~outline, return_distances=False, return_indices=True)
    effect = rgb.copy()
    effect[fringe] = rgb[nearest[0, fringe], nearest[1, fringe]]
    effect[alpha == 0] = 0
    rgba = np.dstack((effect, np.rint(255 * alpha).astype(np.uint8)))
    return Image.fromarray(rgba), np.flatnonzero(outline.any(axis=1))[-1]


def register(source, layout, body, eye_span):
    sprite, floor = extract_cat(source)
    width, height = source.size
    scale = layout["eyeSpan"] / (eye_span * width)
    target = round(width * scale), round(height * scale)
    resized = sprite.resize(target, Image.Resampling.LANCZOS)
    ground_x, ground_y = layout["ground"]
    left = round(ground_x - body * target[0])
    top = round(ground_y - floor * target[1] / height)
    bounds = resized.getchannel("A").point(lambda x: 255 if x >= 128 else 0).getbbox()
    x0, y0, x1, y1 = (bounds[0] + left, bounds[1] + top, bounds[2] + left, bounds[3] + top)
    if x0 < 4 or y0 < 4 or x1 > layout["tile"][0] - 4 or y1 > layout["tile"][1] - 4:
        raise ValueError(f"Registered silhouette clips its tile: {(x0, y0, x1, y1)}")
    tile = Image.new("RGBA", tuple(layout["tile"]))
    tile.paste(resized, (left, top))
    return tile, (left, top, target[0], target[1])


def tile_point(transform, point):
    left, top, width, height = transform
    x, y = point
    return round(left + x * width), round(top + y * height)


def blink(sprite, eyes, fully_closed):
    frame = sprite.copy()
    ink = Image.new("L", (sprite.width * 3, sprite.height * 3))
    paint = ImageDraw.Draw(ink)
    for x, y in eyes:
        left, right = x - 10, x + 10
        left_edge, right_edge = [cream_edge(sprite, column, y) for column in (left, right)]
        for column in range(x - 8, x + 9):
            fraction = (column - left) / (right - left)
            line = left_edge + fraction * (right_edge - left_edge)
            for row in range(y - 8, y + 9):
                blend = min(1, max(0, 8.7 - math.hypot(column - x, row - y)))
                if blend == 0:
                    continue
                first = sprite.getpixel((left, round(row + left_edge - line)))
                last = sprite.getpixel((right, round(row + right_edge - line)))
                original = sprite.getpixel((column, row))
                coat = [a + fraction * (b - a) for a, b in zip(first, last)]
                frame.putpixel((column, row), tuple(round(a + blend * (b - a)) for a, b in zip(original, coat)))
        if fully_closed:
            paint.arc(tuple(p * 3 for p in (x - 6, y - 5, x + 6, y + 4)), start=22, end=158, fill=255, width=6)
        else:
            paint.ellipse(tuple(p * 3 for p in (x - 5, y - 2, x + 5, y + 3)), fill=255)
    frame.paste((109, 49, 30, 255), (0, 0, frame.width, frame.height), ink.resize(frame.size, Image.Resampling.LANCZOS))
    return frame


def cream_edge(sprite, column, center_y):
    # The white muzzle runs underneath the eyes. Follow that change in colour
    # on both sides of each eye to repaint the missing coat along its curve.
    return max(
        range(center_y - 9, center_y + 15),
        key=lambda row: sprite.getpixel((column, row + 1))[1] - sprite.getpixel((column, row - 1))[1],
    )


def flex(sprite, center, influence, dx, dy):
    pixels = np.asarray(sprite, dtype=np.float32) / 255
    y, x = np.mgrid[:sprite.height, :sprite.width]
    rx, ry = influence
    distance = ((x - center[0]) / rx) ** 2 + ((y - center[1]) / ry) ** 2
    weight = np.maximum(0, 1 - distance) ** 2
    coordinates = y - weight * dy, x - weight * dx
    pixels[:, :, :3] *= pixels[:, :, 3:]
    result = np.stack([ndimage.map_coordinates(pixels[:, :, channel], coordinates, order=1) for channel in range(4)], axis=2)
    np.divide(result[:, :, :3], result[:, :, 3:], out=result[:, :, :3], where=result[:, :, 3:] > 0)
    warped = np.rint(result.clip(0, 1) * 255).astype(np.uint8)
    unchanged = weight == 0
    warped[unchanged] = np.asarray(sprite)[unchanged]
    return Image.fromarray(warped)


def encoded_png(sprite):
    buffer = io.BytesIO()
    sprite.save(buffer, format="PNG", optimize=True)
    return buffer.getvalue()


def generate():
    layout = json.loads((ASSETS / "sprite-layout.json").read_text(encoding="utf-8"))
    expected = {path.name for path in ASSETS.glob("*.png")}
    supplied = {source for source, _, _ in layout["poses"].values()}
    if expected != supplied or len(supplied) != len(layout["poses"]):
        raise ValueError("Each source PNG requires one pose registration")

    images = {}
    inventory = {}
    for pose, (source, body, eye_span) in layout["poses"].items():
        path = ASSETS / source
        with Image.open(path) as original:
            images[pose], transform = register(original, layout, body, eye_span)
        inventory[pose] = {"source": source, "sha256": hashlib.sha256(path.read_bytes()).hexdigest()}
        if pose == "sit":
            seated_transform = transform

    sit = images["sit"]
    eyes = [tile_point(seated_transform, point) for point in layout["seatedEyes"]]
    ear = tile_point(seated_transform, layout["seatedEarTip"])
    tail = tile_point(seated_transform, layout["seatedTailTip"])
    additions = {
        "sit_half_blink": blink(sit, eyes, fully_closed=False),
        "sit_blink": blink(sit, eyes, fully_closed=True),
        "sit_ear_flick": flex(sit, ear, (28, 39), -6, 8),
        "sit_tail_left": flex(sit, tail, (37, 70), -10, 4),
        "sit_tail_right": flex(sit, tail, (37, 70), 9, -7),
    }
    images.update(additions)
    for name in additions:
        inventory[name] = {"derivedFrom": "sit"}

    used = {pose for frames in layout["clips"].values() for pose in frames}
    if used != images.keys():
        raise ValueError(f"Clip catalog and generated frames differ: {used ^ images.keys()}")
    atlas = {
        "version": 1, "tile": layout["tile"], "ground": layout["ground"],
        "poses": inventory, "clips": layout["clips"],
    }
    files = {f"{pose}.png": encoded_png(image) for pose, image in images.items()}
    files["atlas.json"] = (json.dumps(atlas, indent=2, ensure_ascii=False) + "\n").encode()
    return files


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true", help="compare generated bytes with checked-in assets")
    args = parser.parse_args()
    files = generate()
    if args.check:
        actual = {path.name for path in OUTPUT.iterdir() if path.is_file()}
        mismatches = sorted(actual ^ files.keys() | {
            name for name in actual & files.keys() if (OUTPUT / name).read_bytes() != files[name]
        })
        if mismatches:
            parser.exit(1, "Yuki artwork needs regenerating: " + ", ".join(mismatches) + "\n")
    else:
        OUTPUT.mkdir(exist_ok=True)
        for name, content in files.items():
            (OUTPUT / name).write_bytes(content)
    print(f"Verified {len(files) - 1} aligned Yuki sprites and the clip catalog")


if __name__ == "__main__":
    main()
