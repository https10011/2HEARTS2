#!/usr/bin/env python3
"""
Phase 3 render analyzer.

The renders are inspected as pixels rather than trusted from source, because
the whole point of Phase 3's validation is to catch what source review cannot:
content overflowing the viewport, a pinned action sitting inside the gesture
area, a headline that wraps badly at the narrow geometry, or a composition
that is simply unbalanced.

For each PNG it reports:

  * viewport geometry and the content bounding box (how much vertical space is
    actually used, and whether anything is clipped at an edge);
  * the bottom-most non-background row, i.e. how close content comes to the
    system navigation area;
  * per-band ink coverage, which reveals whether the vertical composition is
    balanced or everything has collapsed into one region;
  * a blank-row census, which catches large dead gaps and unintended clipping;
  * the rendered dominant colours, to confirm the palette is the TwoHearts
    one and not a Material default.

Usage: python3 tools/analyze-phase3-renders.py <dir>
"""

import sys
from pathlib import Path

from PIL import Image


def rows_of_interest(img):
    """Return, per row, the count of pixels differing from that row's left edge.

    The reference is taken per-row from x=0 rather than from a single
    top-left pixel, because several screens paint a vertical background
    gradient. Comparing against one corner colour made the whole gradient
    read as "content", which is why the first analysis pass reported the dark
    welcome screen as 100% ink. The left edge is safe as a reference: content
    on these screens is centred, and the corner decorations are placed at the
    top-end or bottom-start, never the left edge of every row.
    """
    width, height = img.size
    pixels = img.load()
    counts = []
    for y in range(height):
        bg = pixels[0, y]
        differing = 0
        for x in range(width):
            p = pixels[x, y]
            if abs(p[0] - bg[0]) + abs(p[1] - bg[1]) + abs(p[2] - bg[2]) > 24:
                differing += 1
            # Early exit: once a row is clearly "content" the exact count is
            # irrelevant to every question this tool answers.
            if differing > width // 3:
                differing = width
                break
        counts.append(differing)
    return counts


def content_bounds(counts, width):
    threshold = max(1, width // 200)
    rows = [y for y, c in enumerate(counts) if c > threshold]
    if not rows:
        return None
    return rows[0], rows[-1]


def summarize(path: Path):
    img = Image.open(path).convert("RGB")
    width, height = img.size
    counts = rows_of_interest(img)
    bounds = content_bounds(counts, width)

    print(f"\n=== {path.name}  {width}x{height} ===")
    if bounds is None:
        print("  !! NO CONTENT DETECTED")
        return

    top, bottom = bounds
    print(f"  content rows      : {top} .. {bottom}  (of {height})")
    print(f"  top margin        : {top}px")
    print(f"  bottom margin     : {height - 1 - bottom}px")

    # Clipping: content touching the very first/last rows suggests the frame
    # was drawn under the status bar or the gesture area.
    if top <= 1:
        print("  !! content touches the top edge (status-bar overlap?)")
    if bottom >= height - 2:
        print("  !! content touches the bottom edge (nav-bar overlap?)")

    # Band coverage: split into six bands and report ink density per band.
    band = height // 6
    densities = []
    for i in range(6):
        lo, hi = i * band, min((i + 1) * band, height)
        seg = counts[lo:hi]
        density = sum(seg) / (len(seg) * width) if seg else 0.0
        densities.append(density)
    print("  band density      : " + " ".join(f"{d:5.1%}" for d in densities))

    # Dead space: the longest run of fully blank rows inside the content.
    longest_blank, run, run_start, longest_start = 0, 0, None, None
    for y in range(top, bottom + 1):
        if counts[y] == 0:
            if run == 0:
                run_start = y
            run += 1
            if run > longest_blank:
                longest_blank, longest_start = run, run_start
        else:
            run = 0
    if longest_blank > 0:
        print(f"  largest blank gap : {longest_blank}px at y={longest_start}")

    # Dominant palette: confirms the TwoHearts warm neutrals are in use.
    small = img.resize((width // 8, height // 8))
    colours = small.getcolors(maxcolors=100000) or []
    colours.sort(reverse=True)
    top_colours = ", ".join(
        "#%02X%02X%02X(%d)" % (c[1][0], c[1][1], c[1][2], c[0])
        for c in colours[:5]
    )
    print(f"  dominant colours  : {top_colours}")


def main():
    root = Path(sys.argv[1] if len(sys.argv) > 1 else ".")
    files = sorted(root.glob("p3-*.png"))
    if not files:
        print(f"no p3-*.png found under {root}")
        return 1
    print(f"analyzing {len(files)} renders in {root}")
    for f in files:
        summarize(f)
    return 0


if __name__ == "__main__":
    sys.exit(main())