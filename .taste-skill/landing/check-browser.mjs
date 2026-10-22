import assert from 'node:assert/strict';
import { createServer } from 'node:http';
import { createReadStream, statSync, writeFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { resolve, sep, extname } from 'node:path';
import { Browser, sleep } from 'file:///C:/Users/Oxiegen/.codex/plugins/cache/taste-skill/taste-skill/2.1.4/skills/taste-skill/scripts/lib/cdp.mjs';

const root = resolve(fileURLToPath(new URL('../../Website/', import.meta.url)));
const missing = [];
const server = createServer((req, res) => {
  try {
    let path = resolve(root, '.' + decodeURIComponent(new URL(req.url, 'http://localhost').pathname));
    assert(path === root || path.startsWith(root + sep));
    if (statSync(path).isDirectory()) path = resolve(path, 'index.html');
    const types = { '.html': 'text/html; charset=utf-8', '.css': 'text/css', '.js': 'text/javascript', '.png': 'image/png', '.mp4': 'video/mp4', '.ttf': 'font/ttf' };
    res.writeHead(200, { 'content-type': types[extname(path)] || 'application/octet-stream', 'content-length': statSync(path).size });
    createReadStream(path).pipe(res);
  } catch { missing.push(req.url); res.writeHead(404); res.end(); }
});
await new Promise(resolve => server.listen(0, '127.0.0.1', resolve));
const base = `http://127.0.0.1:${server.address().port}`;
const browser = await Browser.launch();
const results = [];
try {
  const page = await browser.newPage({ width: 1440, height: 900 });
  await page.send('Emulation.setEmulatedMedia', { features: [{ name: 'prefers-reduced-motion', value: 'no-preference' }] });
  await page.goto(base);
  for (const name of ['home', 'assign', 'rest', 'themes']) {
    await page.eval(`document.querySelector('[data-choice][href="#demo-${name}"]').click()`);
    await sleep(350);
    const state = await page.eval(async () => {
      const active = document.querySelector('#demos [data-panel]:not([hidden])');
      const video = active.querySelector('video');
      video.muted = true;
      await video.play();
      return { id: active.id, duration: video.duration, width: video.videoWidth,
        othersPaused: [...document.querySelectorAll('#demos [hidden] video')].every(video => video.paused) };
    });
    await sleep(200);
    assert.equal(state.id, `demo-${name}`);
    assert.equal(state.width, 1080);
    assert(state.othersPaused);
    assert(await page.eval(`document.querySelector('#demos [data-panel]:not([hidden]) video').currentTime > 0`));
    results.push({ demo: name, ...state, playback: 'passed' });
  }
  for (const name of ['home', 'session', 'programs', 'muscles', 'safety', 'exercise', 'equipment', 'setup']) {
    await page.eval(`document.querySelector('[data-choice][href="#screen-${name}"]').click()`);
    await sleep(150);
    const state = await page.eval(async () => {
      const panel = document.querySelector('#features [data-panel]:not([hidden])');
      const image = panel.querySelector('img');
      await image.decode();
      return { id: panel.id, width: image.naturalWidth };
    });
    assert.equal(state.id, `screen-${name}`);
    assert.equal(state.width, 1080);
  }
  results.push({ featureSelection: 'eight original images loaded' });
  await page.eval(`document.querySelector('[data-choice][href="#screen-safety"]').focus()`);
  for (const type of ['keyDown', 'keyUp']) await page.send('Input.dispatchKeyEvent', { type, key: 'Enter', code: 'Enter', windowsVirtualKeyCode: 13 });
  await sleep(350);
  assert.equal(await page.eval(`document.querySelector('#features [data-panel]:not([hidden])').id`), 'screen-safety');
  await page.eval(`document.querySelector('#demo-themes video').pause()`);
  results.push({ keyboardSelection: 'passed' });
  for (const width of [320, 390, 768, 1440]) {
    await page.setViewport({ width, height: 900 });
    assert(await page.eval(`document.documentElement.scrollWidth <= innerWidth`), `Overflow at ${width}`);
  }
  results.push({ overflowWidths: [320, 390, 768, 1440], result: 'passed' });
  await page.goto(`${base}/#screen-muscles`);
  assert.equal(await page.eval(`document.querySelector('#features [data-panel]:not([hidden])').id`), 'screen-muscles');
  assert.equal(page.consoleErrors.length, 0);
  results.push({ websiteOnlyHosting: 'passed', missingResources: missing, consoleErrors: page.consoleErrors });
  assert.equal(missing.length, 0);

  const local = await browser.newPage({ width: 390, height: 844, reducedMotion: true });
  await local.goto(new URL('../../Website/index.html', import.meta.url).href);
  assert.equal(await local.eval(`document.getAnimations().length`), 0);
  assert(await local.eval(`document.fonts.check('16px Inter')`));
  assert.equal(await local.eval(`document.querySelectorAll('#demos [data-panel]:not([hidden])').length`), 1);
  results.push({ fileUrlAndReducedMotion: 'passed' });

  const fallback = await browser.newPage({ width: 390, height: 844 });
  await fallback.send('Emulation.setScriptExecutionDisabled', { value: true });
  await fallback.goto(base);
  assert.equal(await fallback.eval(`document.querySelectorAll('[data-panel]:not([hidden])').length`), 12);
  assert(await fallback.eval(`[...document.querySelectorAll('[data-reveal]')].every(e => getComputedStyle(e).opacity === '1')`));
  results.push({ noJavaScript: 'all four videos and eight feature captures available' });
  writeFileSync(new URL('browser-checks.json', import.meta.url), JSON.stringify(results, null, 2));
  console.log(JSON.stringify(results, null, 2));
} finally {
  server.close();
  await browser.close();
}
