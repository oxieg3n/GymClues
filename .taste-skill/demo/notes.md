# Demo gallery redesign

## Scope and inventory
Only the standalone demo gallery, its local font/logo assets and gallery documentation change. No Android code, recordings, audio, capture sources, application data, exports or dependencies change. Existing callers are local README links; relative URLs and file-system use must survive.

1. Three narrated tutorials run 45 seconds each.
2. Each opens with the GymClues logo for two silent seconds.
3. A floating hand, click pulses and dimmed spotlights explain real controls.
4. Home shows the assigned session, training rhythm and program progress.
5. Program assignment includes a start date and disabled Already Assigned state.
6. Today's workout opens from Home.
7. Rest days can repeat on chosen weekdays.
8. Muscle maps distinguish targets, assisting movers and stabilizers.
9. Guides include equipment, technique, safety and common mistakes.
10. Equipment is grouped by machine type with setup diagrams.
11. Five themes have real screenshots; the fourth recording tours them.
12. Thirteen original screenshots are 1080 × 2400 PNGs.

## Argument and copy deck, before markup
Reader: someone trying GymClues who wants to see the Android app and learn its controls.
What it is: a workout app with assigned routines, exercise guides and planned rest days.
First-screen belief: I can see exactly how GymClues works before using it.
Proof: actual Android captures and short narrated demonstrations.
Next action: Watch the tutorials.

Hero — TRAIN SMARTER. GymClues is your Android workout companion. Explore assigned programs, exercise guides and rest days in the app. Action: Watch the tutorials. Proof: real Home screenshot.
Watch — Watch it work. Pick a walkthrough. Follow the hand, hear the explanation, then try it yourself. Proof: four original recordings and all companion downloads.
Your day — Your next session, ready. See your program on Home, review today's exercises and start when you're ready. Proof: Home, session and program screenshots.
Exercise — Know every movement. See what works, how to move and what to avoid. Proof: muscle mapping, safety and exercise guide screenshots.
Equipment — Set up with confidence. Find your machine, then follow its setup diagram. Proof: grouped equipment and treadmill screenshots.
Themes — Make it feel like you. Five saved color themes, from logo green to warm daylight. Proof: all five theme screenshots.
Footer — GymClues. Less guesswork. More time for your workout. Links: tutorials, screenshots, source notes, back to top.

## Direction
Dealt options: borrowed-category, loud-flat-poster, quiet-instrument. Chose borrowed-category because the hand-led training demonstrations can become a coach's film room, while the quiet column would underserve the large portrait captures. Seed 198581576. No redraws.

On this page, GymClues' hand-led demonstrations and muscle-map clues become a coach's film room: watch a movement, then study its evidence.

Live thing: choose a tutorial in the viewing room; its native player and matching download resources become active. Without JavaScript every tutorial remains visible and playable. The URL fragment supports direct tutorial links.

Adaptations: use the project's existing screenshots rather than fabricate interface fragments (the generic sheet incorrectly assumes no screenshots). The hero's green poster, tilted product crop and dark seam carry the dealt geometry. The dark showcase uses an accessible playlist instead of dragging dimmed videos. Real screenshots replace mock interactive controls in the annotated-manual section. House rules take priority over picture/type overlap and missing section headings. Keep actual brand logo for the large closing mark rather than reconstruct its letters. No scroll scenes were dealt.

Assets: existing GymClues captures, videos and logo only. Local Inter variable font with its OFL license. No stock imagery, generated substitutes, external fonts or sample content.

ROUND 1
Seen in one second: a green training poster and the real Home workout.
Idea (one sentence): GymClues' hand-led demonstrations become a coach's film room with an adjacent exercise manual.
Live thing: tutorial playlist with matching players and downloads.
Layout words by section: hero=poster / tutorials=viewing room / day=annotated plate / movement=large map and guide pair / equipment=split / themes=lineup / footer=mark.
Scores: design 7.5 · usability 8 · creativity 7 · content 8.5 → overall 6.0
Caps applied: inspector still-page failure; checking whether the Windows reduced-motion preference disables animation in the inspection environment.
Top three problems (most visible first), each with the screenshot it shows in and the fix:
1. Desktop-fold: the tilted capture reaches the top of the tutorial button. Shorten its crop so the action is clear.
2. Mobile-fold and tablet-fold: the explanatory sentence is secondary at 16px. Raise it to 20px and tighten the mobile poster height to keep the action visible.
3. Desktop-fold: the heavy headline's tight spacing joins adjacent strokes. Ease tracking from -.075em to -.055em.

ROUND 2
Seen in one second: a GymClues-green training poster with the real assigned workout, followed by a dark tutorial viewing room.
Idea (one sentence): GymClues' hand-led demonstrations and exercise maps become a coach's film room with the evidence beside it.
Live thing: choose among four tutorials; exactly one player is visible and its companion resources stay with it.
Layout words by section: hero=poster / tutorials=viewing room / day=annotated plate / movement=large map and guide pair / equipment=split / themes=lineup / footer=mark.
Scores: design 8 · usability 8 · creativity 7 · content 9 → overall 7.9
Caps applied: none. The concept remains a known film-room/editorial approach, so creativity stays at 7.
Top three problems (most visible first), each with the screenshot it shows in and the fix:
1. Mobile-page-01: screenshot text is too small for detailed reading in two columns. The labeled images open full-resolution originals; this overview is intentionally for browsing, while native video controls offer fullscreen tutorials.
2. Desktop-page-01: portrait footage leaves dark space beside the player. Keep its original ratio and use that space as a viewing stage; enlarging would require a much taller tutorial section.
3. Desktop-fold: TRAIN SMARTER is a familiar training phrase. Actual GymClues Home, the local wordmark and a concrete app description establish the subject; no extra marketing claims added.

## Final verification
- Source lint: 0 FAIL, 0 WARN.
- Browser inspection at 1440 × 900, 768 × 1024 and 390 × 844: 0 FAIL, 0 WARN; 27 moving elements; no runtime exceptions.
- Opened every round-2 fold and page contact sheet at all three widths and judged those rendered pictures.
- Windows itself requests reduced motion. `inspect-page.mjs` explicitly emulates no-preference for the normal inspection and preserves the inspector's explicit reduced-motion pass. A separate live reduced-motion browser check confirms zero animations and visible content.
- Real Chrome file:// playback: all four original H.264 recordings decode at 1080 × 2400, with duration 45 / 45 / 45 / 16.448 seconds and advancing playheads.
- Switching tutorials pauses hidden video players; keyboard Enter activates a tutorial; direct tutorial fragments select correctly; unrelated section links retain the selection.
- No horizontal page overflow at 320, 390, 768 or 1440 pixels.
- With JavaScript disabled all four native video players and feature content remain visible.
- Resource assertions: all local URLs and anchors resolve, 13 original screenshot links and four videos preserved. `node docs/demo/source/check-gallery.mjs` passes.
- No app build needed: no Android source was touched by this redesign. Firefox, Safari and hosted deployment were not tested. Nothing published or pushed.
- Asset credits: all captures, footage and logo are existing GymClues project assets. Inter uses the bundled SIL OFL license. No found stock, substitute drawings, placeholder testimonials or sample content.
