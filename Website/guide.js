(() => {
  "use strict";
  const root = document.getElementById("guide");
  const data = window.GYMCLUES_GUIDE;
  if (!root || !Array.isArray(data) || !data.length) return;
  const get = (id) => root.querySelector("[data-gg=" + id + "]");
  const [chooser, workspace, shots, scene, full, tap, title, body, count, help, previous, next, steps, reset, broken, annotation, marker] =
    ["chooser","workspace","shot","scene","full","tap","title","body","count","help","prev","next","steps","reset","broken","annotation","marker"].map(get);
  const catalog = root.querySelector("[data-catalog]");
  const currentLabel = root.querySelector("[data-current-lesson]");
  const catalogSummary = catalog?.querySelector("summary");
  const groups = [
    ["Getting started", [0, 7, 6, 17]],
    ["Planning and logging workouts", [1, 8, 11, 14, 15, 5, 12, 9, 10]],
    ["Exercises and equipment", [2, 3, 18]],
    ["Food and nutrition", [4, 13, 16]]
  ];
  let lesson = 0, current = 0;
  const lessonButtons = data.map((entry, i) => {
    const button = document.createElement("button");
    button.type = "button";
    button.className = "gg-lesson";
    button.textContent = entry.name;
    button.addEventListener("click", () => {
      lesson = i;
      current = 0;
      draw();
      if (catalog) catalog.open = false;
      catalogSummary?.focus();
    });
    return button;
  });
  if (catalog) {
    const listed = new Set();
    for (const [heading, indexes] of groups) {
      const group = document.createElement("div");
      group.className = "gg-group";
      const title = document.createElement("h3");
      title.textContent = heading;
      group.append(title);
      for (const i of indexes) {
        if (lessonButtons[i] && !listed.has(i)) { group.append(lessonButtons[i]); listed.add(i); }
      }
      chooser.append(group);
    }
    lessonButtons.forEach((button, i) => { if (!listed.has(i)) chooser.append(button); });
    catalog.hidden = false;
  } else chooser.append(...lessonButtons);
  function show(index) {
    current = Math.max(0, Math.min(data[lesson].steps.length - 1, index));
    draw();
  }
  function draw() {
    const lessonData = data[lesson];
    const items = lessonData.steps;
    const item = items[current];
    const last = current === items.length - 1;
    const already = shots.getAttribute("src") === item.image;
    lessonsState();
    title.textContent = item.title;
    body.textContent = item.body;
    count.textContent = lessonData.name + " · Step " + (current + 1) + " of " + items.length;
    previous.disabled = current === 0;
    next.hidden = last;
    next.textContent = item.action ?? "Next step";
    if (!already) {
      shots.hidden = false;
      full.hidden = false;
      broken.hidden = true;
      tap.hidden = true;
      shots.width = item.width || 1080;
      shots.height = item.height || 2400;
      shots.setAttribute("src", item.image);
    }
    shots.alt = item.alt;
    full.href = item.image;
    tap.hidden = !item.tap || last || shots.hidden || !already || !shots.complete;
    marker.hidden = tap.hidden;
    if (item.tap && !last) {
      const [left, top, width, height] = item.tap;
      for (const target of [tap, marker]) {
        target.style.left = left + "%";
        target.style.top = top + "%";
        target.style.setProperty("--tap-w", width + "%");
        target.style.setProperty("--tap-h", height + "%");
      }
      tap.setAttribute("aria-label", item.action || "Next step");
      marker.dataset.pointer = top < 18 ? "below" : "above";
    }
    annotation.hidden = !item.tap || last;
    annotation.textContent = item.tap && !last ? "Highlighted control: " + item.action + ". Next: " + items[current + 1].title + "." : "";
    help.textContent = last ? "Walkthrough complete. Choose another lesson or start this one again." :
      item.tap ? "Select the highlighted app control or use the step button." : "Use the step button to continue.";
    if (steps.dataset.lesson !== String(lesson)) {
      steps.replaceChildren(...items.map((stage, i) => {
        const li = document.createElement("li"), button = document.createElement("button");
        button.type = "button";
        button.className = "gg-step";
        const num = document.createElement("span");
        num.className = "gg-num";
        num.textContent = i + 1;
        const label = document.createElement("span");
        label.textContent = stage.nav || stage.title;
        button.append(num, label);
        button.addEventListener("click", () => show(i));
        li.append(button);
        return li;
      }));
      steps.dataset.lesson = String(lesson);
    }
    [...steps.querySelectorAll("button")].forEach((b, i) => {
      if (current === i) b.setAttribute("aria-current", "step");
      else b.removeAttribute("aria-current");
    });
    scene.setAttribute("aria-label", item.title + " screenshot");
  }
  function lessonsState() {
    if (currentLabel) currentLabel.textContent = data[lesson].name;
    lessonButtons.forEach((b, i) => b.setAttribute("aria-pressed", String(i === lesson)));
  }
  shots.addEventListener("error", () => {
    shots.hidden = true;
    tap.hidden = true;
    marker.hidden = true;
    broken.hidden = false;
    full.hidden = true;
  });
  shots.addEventListener("load", () => {
    shots.hidden = false;
    broken.hidden = true;
    full.hidden = false;
    tap.hidden = !data[lesson].steps[current].tap || current === data[lesson].steps.length - 1;
    marker.hidden = tap.hidden;
  });
  previous.addEventListener("click", () => show(current - 1));
  function forward(origin) {
    show(current + 1);
    if (current === data[lesson].steps.length - 1) reset.focus();
    else if (origin === tap) next.focus();
  }
  next.addEventListener("click", () => forward(next));
  tap.addEventListener("click", () => forward(tap));
  reset.addEventListener("click", () => show(0));
  chooser.hidden = false;
  workspace.hidden = false;
  draw();
})();
