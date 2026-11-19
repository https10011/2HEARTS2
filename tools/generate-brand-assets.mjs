/**
 * Builds Android brand resources from the supplied logo and mark SVGs.
 *
 * Run npm ci in tools, then npm run generate-brand-assets. Set CHROME_PATH
 * when Chromium is installed somewhere other than /usr/bin/chromium.
 */
import { chromium } from 'playwright';
import fs from 'node:fs/promises';
import path from 'node:path';

const root = path.resolve(import.meta.dirname, '..');
const source = path.join(root, 'app/src/main/assets/branding');
const resources = path.join(root, 'app/src/main/res');
const [logo, mark] = await Promise.all([
  fs.readFile(path.join(source, 'twohearts-logo.svg'), 'utf8'),
  fs.readFile(path.join(source, 'twohearts-logo-mark.svg'), 'utf8'),
]);

const browser = await chromium.launch({
  executablePath: process.env.CHROME_PATH || '/usr/bin/chromium',
  args: ['--no-sandbox'],
});
try {
  const page = await browser.newPage();
  const results = await page.evaluate(async ({ logo, mark }) => {
    function canvas(width, height) {
      const result = document.createElement('canvas');
      result.width = width;
      result.height = height;
      return result;
    }

    async function artwork(svg, width, height) {
      const image = new Image();
      image.src = `data:image/svg+xml,${encodeURIComponent(svg)}`;
      await image.decode();

      const source = canvas(width, height);
      const context = source.getContext('2d', { willReadFrequently: true });
      context.drawImage(image, 0, 0, width, height);
      const alpha = context.getImageData(0, 0, width, height).data;
      let left = width;
      let right = 0;
      let top = height;
      let bottom = 0;
      for (let y = 0; y < height; y++) {
        for (let x = 0; x < width; x++) {
          if (alpha[(y * width + x) * 4 + 3] === 0) continue;
          left = Math.min(left, x);
          right = Math.max(right, x + 1);
          top = Math.min(top, y);
          bottom = Math.max(bottom, y + 1);
        }
      }

      if (right <= left || bottom <= top) throw new Error('Logo rendered with no visible pixels');
      const centerX = (left + right) / 2;
      const centerY = (top + bottom) / 2;
      let radius = 0;
      for (let y = top; y < bottom; y++) {
        for (let x = left; x < right; x++) {
          if (alpha[(y * width + x) * 4 + 3] !== 0) {
            radius = Math.max(radius, Math.hypot(x + 0.5 - centerX, y + 0.5 - centerY));
          }
        }
      }
      return { source, left, top, width: right - left, height: bottom - top, radius };
    }

    function output(source, width, height, scale, backgroundRadius = null) {
      const result = canvas(width * 2, height * 2);
      const context = result.getContext('2d');
      context.scale(2, 2);
      context.imageSmoothingQuality = 'high';
      if (backgroundRadius !== null) {
        context.beginPath();
        context.roundRect(0, 0, width, height, backgroundRadius);
        context.fillStyle = '#FAF2E7';
        context.fill();
      }

      const artWidth = source.width * scale;
      const artHeight = source.height * scale;
      context.drawImage(
        source.source, source.left, source.top, source.width, source.height,
        (width - artWidth) / 2, (height - artHeight) / 2, artWidth, artHeight,
      );

      const downsampled = canvas(width, height);
      const finalContext = downsampled.getContext('2d');
      finalContext.imageSmoothingQuality = 'high';
      finalContext.drawImage(result, 0, 0, width, height);
      return downsampled.toDataURL('image/png').split(',')[1];
    }

    const lockup = await artwork(logo, 1520, 1302);
    const hearts = await artwork(mark, 1228, 1141);
    const assets = [
      ['drawable-nodpi/brand_logo.png',
        output(lockup, 720, 617, Math.min(672 / lockup.width, 569 / lockup.height))],
      ['drawable-nodpi/brand_logo_mark.png',
        output(hearts, 512, 476, Math.min(476 / hearts.width, 440 / hearts.height))],
      // The 108 dp adaptive layer is masked around its central 66 dp safe region.
      ['drawable-nodpi/ic_launcher_foreground.png', output(hearts, 576, 576, 576 * 30.5 / 108 / hearts.radius)],
    ];

    for (const [density, size] of [
      ['mdpi', 48], ['hdpi', 72], ['xhdpi', 96], ['xxhdpi', 144], ['xxxhdpi', 192],
    ]) {
      const scale = size * 0.33 / hearts.radius;
      assets.push(
        [`mipmap-${density}/ic_launcher.png`, output(hearts, size, size, scale, size * 0.22)],
        [`mipmap-${density}/ic_launcher_round.png`, output(hearts, size, size, scale, size / 2)],
      );
    }
    return assets;
  }, { logo, mark });

  for (const [relativePath, image] of results) {
    const target = path.join(resources, relativePath);
    await fs.mkdir(path.dirname(target), { recursive: true });
    await fs.writeFile(target, Buffer.from(image, 'base64'));
    console.log(relativePath);
  }
} finally {
  await browser.close();
}
