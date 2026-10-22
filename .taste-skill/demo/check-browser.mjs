import assert from 'node:assert/strict';
import { writeFileSync } from 'node:fs';
import { Browser, sleep } from 'file:///C:/Users/Oxiegen/.codex/plugins/cache/taste-skill/taste-skill/2.1.4/skills/taste-skill/scripts/lib/cdp.mjs';

const browser = await Browser.launch();
const results = [];
const url = 'file:///F:/Projects/GymClues/docs/demo/index.html';
try {
  const page = await browser.newPage({ width: 1440, height: 900 });
  await page.send('Emulation.setEmulatedMedia', { features: [{ name: 'prefers-reduced-motion', value: 'no-preference' }] });
  await page.goto(url);
  assert(await page.eval(`document.fonts.check('16px Inter')`));
  assert.equal(await page.eval(`document.querySelectorAll('.lesson:not([hidden])').length`), 1);
  for (const id of ['home', 'assign', 'rest', 'themes']) {
    await page.eval(`document.querySelector('[href="#lesson-${id}"]').click()`);
    await sleep(500);
    const state = await page.eval(async () => {
      const lesson = document.querySelector('.lesson:not([hidden])');
      const video = lesson.querySelector('video');
      video.muted = true;
      await video.play();
      return { id: lesson.id, duration: video.duration, width: video.videoWidth, height: video.videoHeight,
        current: document.querySelector('[data-lesson][aria-current="true"]').hash,
        hiddenPaused: [...document.querySelectorAll('.lesson[hidden] video')].every(video => video.paused) };
    });
    await sleep(300);
    assert.equal(state.id, `lesson-${id}`);
    assert.equal(state.current, `#lesson-${id}`);
    assert(state.hiddenPaused);
    assert.equal(state.width, 1080);
    assert.equal(state.height, 2400);
    assert(Math.abs(state.duration - (id === 'themes' ? 16.45 : 45)) < .1);
    assert(await page.eval(`document.querySelector('.lesson:not([hidden]) video').currentTime > 0`));
    results.push({ tutorial: id, ...state, playback: 'passed' });
  }
  await page.eval(`document.querySelector('[href="#lesson-assign"]').focus()`);
  await page.send('Input.dispatchKeyEvent', { type: 'keyDown', key: 'Enter', code: 'Enter', windowsVirtualKeyCode: 13 });
  await page.send('Input.dispatchKeyEvent', { type: 'keyUp', key: 'Enter', code: 'Enter', windowsVirtualKeyCode: 13 });
  await sleep(500);
  assert.equal(await page.eval(`document.querySelector('.lesson:not([hidden])').id`), 'lesson-assign');
  await page.screenshot({ path: 'F:/Projects/GymClues/.taste-skill/demo/selected-tutorial.jpg' });
  results.push({ keyboardSelection: 'passed' });
  for (const width of [320, 390, 768, 1440]) {
    await page.setViewport({ width, height: 900 });
    assert(await page.eval(`document.documentElement.scrollWidth <= innerWidth`), `Overflow at ${width}`);
  }
  results.push({ overflowWidths: [320, 390, 768, 1440], result: 'passed' });
  await page.goto(`${url}#lesson-rest`);
  assert.equal(await page.eval(`document.querySelector('.lesson:not([hidden])').id`), 'lesson-rest');
  assert.equal(page.consoleErrors.length, 0);
  results.push({ fileUrlAndDeepLink: 'passed', consoleErrors: page.consoleErrors });

  const reduced = await browser.newPage({ width: 390, height: 844, reducedMotion: true });
  await reduced.goto(url);
  assert.equal(await reduced.eval(`document.getAnimations().length`), 0);
  assert(await reduced.eval(`[...document.querySelectorAll('[data-reveal]')].every(e => getComputedStyle(e).opacity === '1')`));
  results.push({ reducedMotion: 'passed' });

  const fallback = await browser.newPage({ width: 390, height: 844 });
  await fallback.send('Emulation.setScriptExecutionDisabled', { value: true });
  await fallback.goto(url);
  assert.equal(await fallback.eval(`document.querySelectorAll('.lesson:not([hidden])').length`), 4);
  assert(await fallback.eval(`[...document.querySelectorAll('[data-reveal]')].every(e => getComputedStyle(e).opacity === '1')`));
  results.push({ noJavaScript: 'four playable videos and all screenshots visible' });
  writeFileSync('F:/Projects/GymClues/.taste-skill/demo/browser-checks.json', JSON.stringify(results, null, 2));
  console.log(JSON.stringify(results, null, 2));
} finally {
  await browser.close();
}
