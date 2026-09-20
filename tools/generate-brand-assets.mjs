/**
 * TwoHearts — brand asset generation tool.
 *
 * Rasterizes the authoritative owner-supplied brand SVGs
 * (`app/src/main/assets/branding/twohearts-logo*.svg`) into Android
 * drawables. The official artwork is never redrawn, recoloured or
 * recreated — this only converts the existing SVG files into a form
 * Android can decode efficiently at runtime.
 *
 * Usage (from a directory with `playwright` installed):
 *   node tools/generate-brand-assets.mjs
 *
 * Requires the `chromium` browser binary. Set CHROME_PATH to override.
 */
import { chromium } from 'playwright';
import fs from 'fs';
import path from 'path';

const ROOT = path.resolve(import.meta.dirname, '..');
const SRC = path.join(ROOT, 'app/src/main/assets/branding');
const OUT = path.join(ROOT, 'app/src/main/res/drawable-nodpi');

/**
 * Each entry renders an official SVG at an explicit pixel size. `height`
 * is derived from the SVG viewBox so the aspect ratio of the official
 * artwork is preserved exactly.
 *
 * nodpi is used because these are resolution-independent brand marks that
 * must render at the exact requested size regardless of device density.
 */
const TARGETS = [
  // Full logo lockup — viewBox 506.3152 x 433.8324
  { svg: 'twohearts-logo.svg', png: 'brand_logo.png', width: 720 },
  // Hearts-only mark — viewBox 306.7499 x 285
  { svg: 'twohearts-logo-mark.svg', png: 'brand_logo_mark.png', width: 512 },
  // Launcher foreground — official mark kept inside the adaptive-icon
  // safe zone (72dp of 108dp => 66.7%). Adaptive-icon foregrounds must be
  // square, so the canvas is forced square and the art centred.
  { svg: 'twohearts-logo-mark.svg', png: 'ic_launcher_foreground.png', width: 576, square: true, safeZone: 0.667 },
];

const VIEWBOX = {
  'twohearts-logo.svg': [506.3152, 433.8324],
  'twohearts-logo-mark.svg': [306.7499, 285],
};

const chromePath = process.env.CHROME_PATH || '/usr/bin/chromium';
const browser = await chromium.launch({ executablePath: chromePath, args: ['--no-sandbox'] });

fs.mkdirSync(OUT, { recursive: true });

/** Legacy launcher icon densities (ic_launcher_size in dp per bucket). */
const LAUNCHER_DENSITIES = {
  'mipmap-mdpi': 48,
  'mipmap-hdpi': 72,
  'mipmap-xhdpi': 96,
  'mipmap-xxhdpi': 144,
  'mipmap-xxxhdpi': 192,
};

/**
 * Renders the official mark on the brand burgundy field for the legacy
 * (pre-adaptive-icon) launcher buckets. This composes the supplied
 * artwork on a brand surface; it does not alter the artwork itself.
 */
async function renderLegacyLauncher() {
  const svg = fs.readFileSync(path.join(SRC, 'twohearts-logo-mark.svg'), 'utf8');
  for (const [bucket, size] of Object.entries(LAUNCHER_DENSITIES)) {
    const dir = path.join(ROOT, 'app/src/main/res', bucket);
    fs.mkdirSync(dir, { recursive: true });
    const inset = Math.round(size * 0.17);
    for (const [name, radius] of [['ic_launcher.png', 0.22], ['ic_launcher_round.png', 0.5]]) {
      const r = Math.round(size * radius);
      const page = await browser.newPage({
        viewport: { width: size, height: size },
        deviceScaleFactor: 1,
      });
      await page.setContent(
        `<html><head><style>
           html,body{margin:0;padding:0;background:transparent}
           .field{width:${size}px;height:${size}px;background:#6A1B2B;
                  border-radius:${r}px;display:flex;align-items:center;
                  justify-content:center;overflow:hidden}
           .mark{width:${size - inset * 2}px}
           svg{display:block;width:100%;height:auto}
         </style></head><body>
           <div class="field"><div class="mark">${svg}</div></div>
         </body></html>`,
        { waitUntil: 'load' }
      );
      await page.screenshot({ path: path.join(dir, name) });
      await page.close();
    }
    console.log(`${bucket}/ic_launcher(.round).png  ${size}x${size}`);
  }
}

for (const target of TARGETS) {
  const [vbW, vbH] = VIEWBOX[target.svg];
  const width = target.width;
  const height = target.square ? width : Math.round((width * vbH) / vbW);
  const svg = fs.readFileSync(path.join(SRC, target.svg), 'utf8');

  const inner = target.safeZone
    ? `<div style="width:${width}px;height:${height}px;display:flex;align-items:center;justify-content:center">
         <div style="width:${Math.round(width * target.safeZone)}px">${svg}</div>
       </div>`
    : svg;

  const page = await browser.newPage({
    viewport: { width, height },
    deviceScaleFactor: 1,
  });
  await page.setContent(
    `<html><head><style>
       html,body{margin:0;padding:0;background:transparent}
       svg{display:block;width:100%;height:auto}
     </style></head><body>${inner}</body></html>`,
    { waitUntil: 'load' }
  );
  await page.screenshot({ path: path.join(OUT, target.png), omitBackground: true });
  await page.close();
  console.log(`${target.png}  ${width}x${height}  (from ${target.svg})`);
}

await renderLegacyLauncher();

await browser.close();
console.log(`\nWrote ${TARGETS.length} drawables to app/src/main/res/drawable-nodpi/`);
console.log('Updated legacy launcher icons for 5 density buckets.');