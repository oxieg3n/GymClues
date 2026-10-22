import assert from 'node:assert/strict';
import { readFileSync, existsSync } from 'node:fs';
import vm from 'node:vm';

const root = new URL('../', import.meta.url);
const html = readFileSync(new URL('index.html', root), 'utf8');
for (const [, path] of html.matchAll(/(?:href|src|poster)="([^"]+)"/g)) {
  if (path.startsWith('#')) assert(html.includes(`id="${path.slice(1)}"`), `Missing anchor ${path}`);
  else assert(existsSync(new URL(path, root)), `Missing local resource ${path}`);
}
assert.equal(new Set([...html.matchAll(/href="(screens\/\d[^\"]+\.png)"/g)].map(match => match[1])).size, 13);
assert.equal((html.match(/<video /g) || []).length, 4);

const script = readFileSync(new URL('gallery.js', root), 'utf8');
function setup(hash = '', reduced = false) {
  const lessons = ['home', 'assign', 'rest', 'themes'].map(name => ({
    id: `lesson-${name}`, hidden: false,
    video: { paused: false, pause() { this.paused = true; } },
    querySelector() { return this.video; }
  }));
  const choices = lessons.map(lesson => ({
    hash: `#${lesson.id}`, attributes: {},
    setAttribute(key, value) { this.attributes[key] = value; },
    removeAttribute(key) { delete this.attributes[key]; }
  }));
  const watch = { dataset: {}, querySelectorAll: selector => selector === '.lesson' ? lessons : choices };
  const listeners = {};
  const context = {
    document: { querySelector: () => watch },
    location: { hash }, matchMedia: () => ({ matches: reduced }),
    window: { addEventListener: (event, handler) => { listeners[event] = handler; } }
  };
  vm.runInNewContext(script, context);
  return { lessons, choices, context, select(hash) { context.location.hash = hash; listeners.hashchange(); } };
}
const page = setup();
assert.deepEqual(page.lessons.filter(lesson => !lesson.hidden).map(lesson => lesson.id), ['lesson-home']);
for (const lesson of page.lessons) {
  page.select(`#${lesson.id}`);
  assert.equal(page.lessons.filter(item => !item.hidden).length, 1);
  assert.equal(lesson.hidden, false);
  assert.equal(page.choices.find(choice => choice.attributes['aria-current'] === 'true').hash, `#${lesson.id}`);
  assert(page.lessons.filter(item => item.hidden).every(item => item.video.paused));
}
page.select('#screenshots');
assert.equal(page.lessons[3].hidden, false, 'Section navigation must keep the selected tutorial');
assert.equal(setup('#lesson-rest', true).lessons[2].hidden, false, 'Deep links work with reduced motion');
assert.equal(setup('#unknown').lessons[0].hidden, false, 'Unknown fragments retain the default tutorial');
console.log('Gallery checks passed: all resources, 13 originals, four videos, selection, pausing, deep links and reduced-motion fallback.');
