import { Browser } from 'file:///C:/Users/Oxiegen/.codex/plugins/cache/taste-skill/taste-skill/2.1.4/skills/taste-skill/scripts/lib/cdp.mjs';

// Windows requests reduced motion; inspect both the normal design and the inspector's explicit reduced-motion pass.
const newPage = Browser.prototype.newPage;
Browser.prototype.newPage = async function(options = {}) {
  const page = await newPage.call(this, options);
  await page.send('Emulation.setEmulatedMedia', { features: [{ name: 'prefers-reduced-motion', value: options.reducedMotion ? 'reduce' : 'no-preference' }] });
  return page;
};
await import('file:///C:/Users/Oxiegen/.codex/plugins/cache/taste-skill/taste-skill/2.1.4/skills/taste-skill/scripts/inspect.mjs');
