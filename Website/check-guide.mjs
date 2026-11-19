import assert from 'node:assert/strict';
import { existsSync, readFileSync } from 'node:fs';
import vm from 'node:vm';
const site = new URL('./', import.meta.url);
const from = name => readFileSync(new URL(name, site), 'utf8');
const dataScope = { window: {} };
vm.runInNewContext(from("guide-data.js"), dataScope);
const data = dataScope.window.GYMCLUES_GUIDE;
assert(Array.isArray(data) && data.length >= 2, "At least two real lessons");
const html = from("index.html");
for (const {name, steps} of data) {
  assert(name && steps.length >= 2, "A lesson has a meaningful series of screens");
  for (const [i, s] of steps.entries()) {
    assert(s.title && s.body && s.alt);
    assert.match(s.image,/^media\/guide\/[a-z0-9-]+\.(png|webp)$/);
    assert(existsSync(new URL(s.image, site)), s.image + ' missing');
    if (s.tap) {
      const [x,y,w,h]=s.tap;
      assert(i < steps.length - 1 && s.action && s.tap.length === 4);
      assert(x >= 0 && y >= 0 && w > 0 && h > 0 && x-w/2 >= 0 && x+w/2 <= 100 && y-h/2 >= 0 && y+h/2 <= 100);
    }
    assert(html.includes(s.title.replaceAll('&', '&amp;')), 'No-JS fallback step ' + s.title);
  }
}
let active = null;
function node() {
  return {
    children: [], attrs: {}, dataset: {}, events: {}, style: {setProperty(k,v) { this[k]=v; }},
    hidden: false,
    append(...items){ this.children.push(...items); },
    replaceChildren(...items) { this.children=items; },
    getAttribute(k) { return this.attrs[k]??null; },
    setAttribute(k,v) { this.attrs[k]=v; },
    removeAttribute(k) { delete this.attrs[k]; },
    addEventListener(k,h){ this.events[k]=h; },
    querySelectorAll(){ return this.children.flatMap(o=>o.children); },
    focus(){ active=this; },
    click(){this.events.click?.();}
  };
}
const selectors = Object.fromEntries("chooser workspace shot scene full tap title body count help prev next steps reset broken annotation marker".split(" ").map(s=>[s,node()]));
const root = {querySelector(v){return selectors[v.slice(9,-1)];}};
vm.runInNewContext(from("guide.js"), {
  window: dataScope.window,
  document: {getElementById(){return root;},createElement(){return node();}}
});
assert.equal(selectors.chooser.children.length, data.length);
assert.equal(selectors.prev.disabled, true);
assert.match(selectors.count.textContent, /Step 1 of /);
const first = data[0].steps;
assert.equal(selectors.shot.attrs.src, first[0].image);
assert.equal(selectors.title.textContent, first[0].title);
if (first[0].tap) assert.match(selectors.annotation.textContent, /Highlighted control: .*Next:/);
selectors.shot.events.error();
assert(selectors.shot.hidden && selectors.tap.hidden && selectors.marker.hidden && !selectors.broken.hidden);
selectors.shot.events.load();
assert(!selectors.shot.hidden && selectors.broken.hidden);
(ifTap => ifTap ? selectors.tap : selectors.next)(first[0].tap).click();
assert.equal(selectors.title.textContent, first[1].title);
selectors.steps.children.at(-1).children[0].click();
assert(selectors.next.hidden && selectors.tap.hidden);
selectors.reset.click();
assert.equal(selectors.title.textContent, first[0].title);
selectors.chooser.children[1].click();
assert.equal(selectors.title.textContent, data[1].steps[0].title);
assert.equal(selectors.chooser.children[1].attrs['aria-pressed'], 'true');
assert.equal(selectors.chooser.children[0].attrs['aria-pressed'], 'false');
console.log('Guide checks passed: complete lessons and asset references, hotspot and text navigation, reset, lesson selection, no-JS fallback and failed-image recovery.');
