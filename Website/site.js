document.querySelectorAll('[data-viewer]').forEach(viewer => {
  const panels = [...viewer.querySelectorAll('[data-panel]')];
  const choices = [...viewer.querySelectorAll('[data-choice]')];
  function select(hash) {
    const selected = panels.find(panel => `#${panel.id}` === hash);
    if (!selected) return;
    for (const panel of panels) {
      panel.hidden = panel !== selected;
      if (panel.hidden) panel.querySelector('video')?.pause();
    }
    for (const choice of choices) {
      if (choice.hash === hash) choice.setAttribute('aria-current', 'true');
      else choice.removeAttribute('aria-current');
    }
  }
  select(panels.some(panel => `#${panel.id}` === location.hash) ? location.hash : `#${panels[0].id}`);
  // Reveal the anchor target before the browser scrolls to it; hashchange also covers Back and Forward.
  choices.forEach(choice => choice.addEventListener('click', () => select(choice.hash)));
  window.addEventListener('hashchange', () => select(location.hash));
  viewer.dataset.ready = '';
});

if ('IntersectionObserver' in window && !matchMedia('(prefers-reduced-motion: reduce)').matches) {
  const observer = new IntersectionObserver(entries => {
    for (const entry of entries) {
      if (entry.isIntersecting) {
        entry.target.classList.add('arrived');
        observer.unobserve(entry.target);
      }
    }
  }, { threshold: .1 });
  document.querySelectorAll('[data-reveal]').forEach(element => observer.observe(element));
}
