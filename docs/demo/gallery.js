const watch = document.querySelector('#watch');
const lessons = [...watch.querySelectorAll('.lesson')];
const choices = [...watch.querySelectorAll('[data-lesson]')];

function selectLesson() {
  const selected = lessons.find(lesson => `#${lesson.id}` === location.hash) || lessons[0];
  for (const lesson of lessons) {
    lesson.hidden = lesson !== selected;
    if (lesson.hidden) lesson.querySelector('video').pause();
  }
  for (const choice of choices) {
    if (choice.hash === `#${selected.id}`) choice.setAttribute('aria-current', 'true');
    else choice.removeAttribute('aria-current');
  }
}

// Keep unrelated section links from resetting a tutorial that is already playing.
window.addEventListener('hashchange', () => {
  if (lessons.some(lesson => `#${lesson.id}` === location.hash)) selectLesson();
});
selectLesson();
watch.dataset.ready = '';

if ('IntersectionObserver' in window && !matchMedia('(prefers-reduced-motion: reduce)').matches) {
  const observer = new IntersectionObserver(entries => {
    for (const entry of entries) {
      if (entry.isIntersecting) {
        entry.target.classList.add('is-visible');
        observer.unobserve(entry.target);
      }
    }
  }, { threshold: 0.1 });
  document.querySelectorAll('[data-reveal]').forEach(element => observer.observe(element));
}
