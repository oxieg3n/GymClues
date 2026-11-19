import assert from 'node:assert/strict';
import { existsSync, readFileSync } from 'node:fs';
import vm from 'node:vm';

const root = new URL('./', import.meta.url);
const html = readFileSync(new URL('index.html', root), 'utf8');
const apk = 'https://github.com/oxieg3n/GymClues/releases/download/PublicReleasev1/GymClues-Latest-Nov14.apk';
assert.equal(html.split(apk).length - 1, 3);
assert(!/App Store|Google Play|releases\/download\/v1\//i.test(html));
for (const [, path] of html.matchAll(/(?:href|src|poster)="([^"]+)"/g)) {
  if (path.startsWith('#')) assert(html.includes(`id="${path.slice(1)}"`), `Missing anchor ${path}`);
  else if (!path.startsWith('https://')) {
    assert(!path.startsWith('../'), 'Hosting publishes Website only');
    assert(existsSync(new URL(path, root)), `Missing asset ${path}`);
  }
}
assert.equal((html.match(/<video /g) || []).length, 4);
assert.deepEqual([...new Set([...html.matchAll(/media\/videos\/[^" ]+\.mp4/g)].map(([path]) => path))].sort(), [
  'media/videos/assign-program-narrated.mp4',
  'media/videos/home-menu-tour-narrated.mp4',
  'media/videos/themes.mp4',
  'media/videos/workout-rest-days-narrated.mp4',
]);
assert.match(html, /<section class="gym-guide" id="guide"/);
assert.match(html, /<link rel="canonical" href="https:\/\/oxieg3n.com\/GymClues\/">/);
assert.equal((html.match(/<figure class="panel"/g) || []).length, 8);

const handlers = [];
function viewer(prefix, size, video) {
  const panels = Array.from({ length: size }, (_, index) => ({
    id: `${prefix}-${index}`, hidden: false,
    video: video ? { paused: false, pause() { this.paused = true; } } : null,
    querySelector() { return this.video; }
  }));
  const choices = panels.map(panel => ({
    hash: `#${panel.id}`, attributes: {}, events: {},
    setAttribute(key, value) { this.attributes[key] = value; },
    removeAttribute(key) { delete this.attributes[key]; },
    addEventListener(key, handler) { this.events[key] = handler; }
  }));
  return { panels, choices, dataset: {}, querySelectorAll: selector => selector === '[data-panel]' ? panels : choices };
}
const demos = viewer('demo', 4, true);
const screens = viewer('screen', 8, false);
const location = { hash: '#screen-3' };
vm.runInNewContext(readFileSync(new URL('site.js', root), 'utf8'), {
  document: { querySelectorAll: () => [demos, screens] }, location,
  window: { addEventListener: (name, handler) => handlers.push(handler) }
});
assert.equal(screens.panels[3].hidden, false, 'Direct screenshot link selects its image');
for (const group of [demos, screens]) {
  for (const [index, choice] of group.choices.entries()) {
    choice.events.click();
    assert.equal(group.panels.filter(panel => !panel.hidden).length, 1);
    assert.equal(group.panels[index].hidden, false);
    assert.equal(choice.attributes['aria-current'], 'true');
    assert(group.panels.filter(panel => panel.hidden && panel.video).every(panel => panel.video.paused));
  }
}
location.hash = '#demo-1'; handlers.forEach(handler => handler());
assert.equal(demos.panels[1].hidden, false, 'History navigation updates the selected demo');
assert.equal(screens.panels[7].hidden, false, 'The other viewer retains its selection');
location.hash = '#themes'; handlers.forEach(handler => handler());
assert.equal(demos.panels[1].hidden, false, 'Section links retain active media');
console.log('Landing checks passed: self-contained assets, Nov 14 release links, four demos, eight screenshots, independent selection, history and video pausing.');
