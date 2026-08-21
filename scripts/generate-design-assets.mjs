#!/usr/bin/env node
/**
 * TwoHearts design-asset generator (Phase 23).
 *
 * Reads the two authoritative, owner-provided sources:
 *   - TwoHearts-Logo-BrandName/TwoHearts-Logo-BrandName.svg  (official brand)
 *   - Rose Lily Vectors/01..20-RoseLily.svg                  (20 approved florals)
 *
 * and emits the consumable runtime assets:
 *   - src/assets/branding/twohearts-logo.svg        (full logo: hearts + wordmark)
 *   - src/assets/branding/twohearts-logo-mark.svg   (hearts mark only)
 *   - src/assets/decorations/rose-lily-NN.svg       (one per approved variant)
 *
 * Why a generator: both sources are Canva full-page exports (810×1800 viewBox)
 * whose artwork occupies a small region of the page. Runtime assets crop the
 * viewBox to the artwork so components size them predictably. Non-rendering
 * <metadata> (Canva provenance) is stripped from emitted files; the sources
 * are NEVER modified. Re-run this script after the owner replaces a source
 * asset and every approved location updates automatically.
 *
 * Pure Node, no dependencies. Deterministic output.
 */
import { mkdirSync, readFileSync, readdirSync, writeFileSync } from 'node:fs';
import { join, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const ROOT = join(dirname(fileURLToPath(import.meta.url)), '..');
const LOGO_SRC = join(ROOT, 'TwoHearts-Logo-BrandName', 'TwoHearts-Logo-BrandName.svg');
const FLORAL_SRC_DIR = join(ROOT, 'Rose Lily Vectors');
const BRAND_OUT_DIR = join(ROOT, 'src', 'assets', 'branding');
const FLORAL_OUT_DIR = join(ROOT, 'src', 'assets', 'decorations');

/** Padding around translate() anchor points (wordmark glyph art). */
const GLYPH_PAD = { left: -60, right: 60, top: -70, bottom: 20 };
const GLYPH_RECT_W = -GLYPH_PAD.left + GLYPH_PAD.right;

function nums(str) {
  return str.split(/[\s,]+/).filter(Boolean).map(Number);
}

/** Strip the non-rendering <metadata> block (Canva provenance). */
function stripMetadata(svg) {
  return svg.replace(/<metadata>[\s\S]*?<\/metadata>/, '');
}

/**
 * Collect the rendered rectangles of <image> elements positioned by an
 * ancestor matrix/translate transform (the Canva export pattern), plus
 * translate() anchor points from wordmark glyph groups.
 */
function findContentRects(svg) {
  const rects = [];
  const imageRe = /<g[^>]*transform="(?:(matrix|translate)\(([^)]*)\))"[^>]*>[\s\S]*?<image\s+([^>]*)\/>[\s\S]*?<\/g>/g;
  let m;
  while ((m = imageRe.exec(svg)) !== null) {
    const [, kind, params, imageTag] = m;
    let tx = 0, ty = 0, sx = 1, sy = 1;
    const p = nums(params);
    if (kind === 'matrix') [sx, , , sy, tx, ty] = p;
    else [tx, ty] = p;
    const w = Number(/width="([^"]+)"/.exec(imageTag)?.[1] ?? 0);
    const h = Number(/height="([^"]+)"/.exec(imageTag)?.[1] ?? 0);
    rects.push({ x: tx, y: ty, w: Math.abs(w * sx), h: Math.abs(h * sy) });
  }
  // Wordmark glyph groups: <g transform="translate(x, y)"><g><path .../></g></g>
  const glyphRe = /<g\s+transform="translate\(([^)]*)\)"[^>]*>\s*<g>\s*<path[\s\S]*?<\/g>\s*<\/g>/g;
  while ((m = glyphRe.exec(svg)) !== null) {
    const [gx, gy] = nums(m[1]);
    rects.push({
      x: gx + GLYPH_PAD.left,
      y: gy + GLYPH_PAD.top,
      w: GLYPH_RECT_W,
      h: -GLYPH_PAD.top + GLYPH_PAD.bottom,
    });
  }
  return rects;
}

function union(rects) {
  if (!rects.length) throw new Error('no content found');
  const minX = Math.min(...rects.map((r) => r.x));
  const minY = Math.min(...rects.map((r) => r.y));
  const maxX = Math.max(...rects.map((r) => r.x + r.w));
  const maxY = Math.max(...rects.map((r) => r.y + r.h));
  return { x: minX, y: minY, w: maxX - minX, h: maxY - minY };
}

function round(n) {
  return Math.round(n * 10000) / 10000;
}

/** Rewrite the svg root viewBox to the content crop; drop fixed width/height. */
function cropSvg(sourceSvg, box) {
  const rootTag = /<svg[^>]*>/.exec(sourceSvg)[0];
  const newRoot = rootTag
    .replace(/\s+width="[^"]*"/, '')
    .replace(/\s+height="[^"]*"/, '')
    .replace(/\s+zoomAndPan="[^"]*"/, '')
    .replace(
      /\s+viewBox="[^"]*"/,
      ` viewBox="${round(box.x)} ${round(box.y)} ${round(box.w)} ${round(box.h)}"`,
    );
  return sourceSvg.replace(rootTag, newRoot);
}

function emit(outPath, svgText) {
  writeFileSync(outPath, svgText);
  return `${outPath.replace(ROOT + '/', '')} (${(svgText.length / 1024).toFixed(1)} KB)`;
}

// ---------------- Brand ----------------
mkdirSync(BRAND_OUT_DIR, { recursive: true });
const brandSvg = stripMetadata(readFileSync(LOGO_SRC, 'utf8'));

const allRects = findContentRects(brandSvg);
const imageRects = allRects.filter((r) => r.w !== GLYPH_RECT_W);
const markBox = union([imageRects[0]]); // hearts art = first image group
const brandBox = union(allRects); // hearts + wordmark + flourishes

console.log(emit(join(BRAND_OUT_DIR, 'twohearts-logo.svg'), cropSvg(brandSvg, brandBox)));
console.log(emit(join(BRAND_OUT_DIR, 'twohearts-logo-mark.svg'), cropSvg(brandSvg, markBox)));

// ---------------- Florals ----------------
mkdirSync(FLORAL_OUT_DIR, { recursive: true });
const floralFiles = readdirSync(FLORAL_SRC_DIR).filter((f) => f.endsWith('.svg')).sort();
for (const file of floralFiles) {
  const svg = stripMetadata(readFileSync(join(FLORAL_SRC_DIR, file), 'utf8'));
  const box = union(findContentRects(svg));
  const num = file.replace(/[^0-9]/g, '');
  console.log(emit(join(FLORAL_OUT_DIR, `rose-lily-${num}.svg`), cropSvg(svg, box)));
}
console.log(`\nGenerated ${floralFiles.length} floral assets + 2 brand assets.`);
