# GymClues landing page

## Scope
Rebuild Website/index.html with local CSS/JS and curated copies of existing public demo assets. Existing hosting uploads Website only; self-contained assets preserve that contract without workflow changes. Keep both v1.1.1 APK actions and add the same destination to navigation. Preserve the separate demo gallery and Android app. No dependencies, database changes, forms or new services.

## Inventory
1. GymClues is an Android workout companion with an equipment-first name and logo.
2. Logo green is #04F730; the accepted app uses black and charcoal.
3. Inter is already bundled with the app under SIL OFL.
4. Home opens an assigned workout and shows progress.
5. Program categories include Bro, PPL, upper/lower, full body, Arnold, torso/limbs, hybrid and HIIT.
6. Custom workouts use the same exercise library.
7. Muscle maps separate targets, assisting movers and stabilizers.
8. Safety and movement instructions accompany each exercise.
9. Equipment is grouped by machine type with setup diagrams.
10. Cardio tracks activity-specific measurements.
11. Rest days can repeat weekly; music controls stay in the app.
12. Three existing narrated, hand-led tutorials last 45 seconds each; a fourth tours five themes.

## Argument and copy deck before markup
Reader: a prospective GymClues user looking for an Android training app.
What it is: workout planning, exercise explanations and equipment setup in one Android app.
First-screen belief: I can see how this app helps with my next session.
Proof: the real muscle map and workout screens, then playable demonstrations.
Next action: download the free Android APK or watch a walkthrough.

Hero — Know your next move. GymClues brings workout programs, muscle maps and equipment guides to your Android phone. Action: Get GymClues free. Secondary: Watch the demos. Proof: actual muscle mapping with adjacent session/setup frames.
Demos — See it in action. Three guided walkthroughs, 45 seconds each. Follow the hand from your first program to your next rest day. Proof: Home/menu, assignment and daily workout/rest demos, plus theme tour.
Features — A closer look. Explore the screens you'll use before, during and after a workout. Proof: eight actual captures with named controls; short copy explains programs, tracking, exercise maps and equipment.
Themes — Your workout, your colors. Choose GymClues Default, Ocean, Ember, Rosewood or Daylight. Your choice stays saved in the app. Proof: five real native Home screenshots with labels.
Download — Your next workout starts here. Get GymClues free for Android. Workout programs, exercise guidance and room for rest. Action: Download v1.1.1. Destination: supplied GitHub APK. No store badges.
Footer — GymClues. Less guesswork. More time for your workout. Links: demos, features, source code, support via repository Issues. No fabricated legal pages or user counts.

## Direction and adaptations
Dealt colour-chapters, product-performs and borrowed-category. Chose The product performs, seed 1250081539: real demonstrations and readable captures can do the explaining instead of feature claims.

On this page, GymClues' muscle-map clues and hand-led tutorials become a working product viewing room: choose a feature, inspect the real screen, then watch the action.

Live thing: labeled screenshot explorer with full-resolution images and ordinary anchor fallbacks. The tutorial selector follows the same simple navigation pattern and pauses hidden videos. All media stays available without JavaScript.

Redraws: hero once because the dealt type-weight/letter-spacing controls are not features GymClues has; the replacement is Sentence split by a frame. CTA explicitly changed to A plain sentence, one action because an inline sign-up form would invent an unavailable service. Two redraws total. Keep black/charcoal/green and Inter, actual captures instead of fabricated HTML app screens. Direction-level readable interface geometry takes precedence over the hero's 16:9 photo crop; the actual Android panels are portrait. The showcase is a real native video player rather than an invented giant workout toggle. No scroll scenes dealt; avoid trapping visitors in a pinned tutorial. House rules require clear section titles and visible first-screen action rather than the generic blueprint's blank spaces.

Assets: only existing public GymClues captures, four finished MP4s, brand logo and Inter with its OFL license. Curated media copies under Website/media keep the existing Website-only publishing workflow and file:// preview working. No VoiceStudio reference clips, private profile identifiers, stock photos or fictional claims.

ROUND 1
Seen in one second: a real muscle map between the two halves of an invitation to train.
Idea (one sentence): GymClues explains itself through inspectable screens and hand-led demonstrations.
Live thing: feature explorer, plus the tutorial selector using the same navigation behavior.
Layout words by section: hero=reel / demos=viewing room / features=index / themes=lineup / download=invitation / footer=strip.
Scores: design 7.5 · usability 7.5 · creativity 7 · content 8.5 → overall 6.0
Caps applied: inspector late-title finding. The split title's text bounds pass across the central image even though the letters do not overlap; reunite its words at the left for clearer reading and hit testing.
Top three problems (most visible first), each with the screenshot it shows in and the fix:
1. Desktop-fold: the two title halves are detached by the full screenshot. Keep the reel and place both title lines together on its left, following the direction's general upper-left headline rule.
2. Desktop-page-01: tutorial caption touches the player and its links lose the intended spacing. Fix the generated caption class to match panel-copy styles.
3. Tablet-fold: the hero description and call to action are cramped between narrow columns. Keep 20px readable copy and regroup the headline; the phone already stacks with its download before the image.

ROUND 2
Seen in one second: an exercise muscle map, a clear two-line invitation and a green Android download action.
Idea (one sentence): GymClues' own guidance screens and hand-led tutorials explain the app by letting visitors inspect and watch it.
Live thing: eight-feature screenshot explorer with direct image links; the demo selector pauses inactive recordings.
Layout words by section: hero=reel / demos=viewing room / features=index / themes=lineup / download=invitation / footer=strip.
Scores: design 8 · usability 8 · creativity 7 · content 9 → overall 7.9
Caps applied: none. This is a polished product demonstration approach rather than a new visual concept, hence creativity 7.
Top three problems (most visible first), each with the screenshot it shows in and the fix:
1. Tablet-fold: the portrait reel leaves the supporting text narrower than on desktop. Copy remains at 20px and the download stays fully visible; retain the image at readable native proportions rather than squeeze three screenshots together.
2. Desktop-page-01: the active portrait screenshot continues below the selector's text, leaving space beneath the left column. Keep full-size details and original aspect ratio rather than crop the exercise UI; this is an intentional viewing stage.
3. Mobile-page-01: theme previews are small overview images. Each opens the complete native render; feature screenshots and videos provide the primary detailed views.

## Validation and handoff
- Source lint: 0 FAIL, 0 WARN. Final browser inspection: 0 FAIL, 0 WARN at 1440 × 900, 768 × 1024 and 390 × 844. Before: 12 FAIL and 7 WARN in rendered checks; 30 FAIL and 18 WARN in source lint.
- Opened all eight final fold/contact-sheet images across desktop, tablet and mobile. Reviewed each section, with clear titles, equal margins, readable copy and no missing reveals. Headlines stay within two lines. The inspector recorded 26 moving elements at each width.
- Used the existing inspection wrapper because this Windows host defaults to reduced motion; it explicitly tests normal motion and preserves the inspector's explicit reduced-motion check. Separate real-browser checks confirm zero animations with reduced motion.
- `node Website/check-site.mjs` passes: all assets stay within the published Website folder, three APK buttons use v1.1.1, four demos and eight screenshot panels are present, viewer selections are independent, history works and hidden videos pause.
- Isolated HTTP server rooted at Website: all four videos actually decode/play at 1080 pixels wide, durations 45/45/45/16.448 seconds, advancing playheads, no missing requests or browser exceptions. All eight screenshot choices decode their original 1080-pixel images. Keyboard Enter selects a feature.
- No horizontal overflow at 320, 390, 768 or 1440 pixels. file:// opening works, loads local Inter and applies reduced motion. With JavaScript disabled, all four players and eight feature captures remain visible.
- All 21 copied media assets are referenced and hash-identical to existing project sources. Five Home theme images are native preview renders with an empty dashboard; they are illustrative product states, not customer activity claims. Inter's OFL is bundled. No found photos, external fonts, invented testimonials or placeholders.
- No Android rebuild needed; no Android source or deployment workflow was changed. The existing workflow still listens to main, as before; this task did not publish, push or change its trigger. Safari/Firefox and the actual release APK download were not tested.
- Future recording/screenshot changes need the corresponding curated Website/media copy refreshed, documented in Website/README.md.
