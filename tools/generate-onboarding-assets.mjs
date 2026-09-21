/**
 * TwoHearts — onboarding & decorative asset generation tool.
 *
 * The application ships owner-supplied SVG artwork, but Android cannot decode
 * SVG at runtime and the app has no image loader. Phase 1 solved this for the
 * brand mark with `generate-brand-assets.mjs`; this tool applies the same
 * approach to the *decorative* artwork the onboarding experience needs:
 *
 *  - the replaceable welcome hero photo (`assets/images/`), and
 *  - a curated subset of the owner-approved Rose/Lily florals
 *    (`assets/decorations/`) used to frame the welcome and completion moments.
 *
 * Nothing is redrawn, recoloured or recreated — the SVG sources are rendered
 * exactly as supplied and only re-encoded into a form Android can decode
 * efficiently. Only the variants the onboarding actually composes are
 * rasterized, so no additional dead weight enters the APK.
 *
 * Usage (from `tools/` with playwright installed):
 *   node generate-onboarding-assets.mjs
 */
import { chromium } from 'playwright';
import fs from 'fs';
import path from 'path';

const ROOT = path.resolve(import.meta.dirname, '..');
const OUT = path.join(ROOT, 'app/src/main/res/drawable-nodpi');

/**
 * Rasterization targets. `width` is chosen so the drawable stays sharp when
 * displayed at its composed dp size on an xxxhdpi screen (the Tecno Spark 10
 * Pro class target), while staying small enough not to bloat the APK.
 */
const TARGETS = [
  {
    svg: 'app/src/main/assets/images/onboarding-welcome-photo.svg',
    png: 'onboarding_welcome_photo.png',
    width: 960,
  },
  {
    svg: 'app/src/main/assets/decorations/rose-lily-01.svg',
    png: 'decor_rose_lily_01.png',
    width: 480,
  },
  {
    svg: 'app/src/main/assets/decorations/rose-lily-11.svg',
    png: 'decor_rose_lily_11.png',
    width: 480,
  },
  {
    svg: 'app/src/main/assets/decorations/rose-lily-15.svg',
    png: 'decor_rose_lily_15.png',
    width: 480,
  },
];

const chromePath = process.env.CHROME_PATH || '/usr/bin/chromium';
const browser = await chromium.launch({ executablePath: chromePath, args: ['--no-sandbox'] });

fs.mkdirSync(OUT, { recursive: true });

for (const target of TARGETS) {
  const svg = fs.readFileSync(path.join(ROOT, target.svg), 'utf8');
  const viewBox = svg.match(/viewBox="([\d.\-\s]+)"/);
  let height = target.width;
  if (viewBox) {
    const [, , vbW, vbH] = viewBox[1].trim().split(/\s+/).map(Number);
    height = Math.round((target.width * vbH) / vbW);
  }

  const page = await browser.newPage({
    viewport: { width: target.width, height },
    deviceScaleFactor: 1,
  });
  await page.setContent(
    `<html><head><style>
       html,body{margin:0;padding:0;background:transparent}
       svg{display:block;width:100%;height:auto}
     </style></head><body>${svg}</body></html>`,
    { waitUntil: 'load' }
  );
  await page.screenshot({
    path: path.join(OUT, target.png),
    omitBackground: true,
  });
  await page.close();

  const bytes = fs.statSync(path.join(OUT, target.png)).size;
  console.log(`${target.png}  ${target.width}x${height}  ${(bytes / 1024).toFixed(1)}KB`);
}

await browser.close();
console.log(`\nWrote ${TARGETS.length} drawables to app/src/main/res/drawable-nodpi/`);
