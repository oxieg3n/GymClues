# GymClues theme selection

Taste-skill app settings sheet: true-black-flat, seed 1784168129, no redraws. Retain the accepted layout and Inter typography. The user explicitly wants four new palettes alongside the forest/mint GymClues Default; this overrides the sheet's monochrome color budget and supplies a light option.

Product inventory: offline training; saved programs; adjustable sets; cardio measurements; exercise library; machine guides; role-colored muscle maps; recovery scheduling; gym scope; persistent music controls; existing forest/mint brand.

Question: Which colors feel right for my workout? Signature: five real Material color previews, with the default across the full grid width, four balanced alternatives and working selection buttons. Previews contain color-role labels, not invented workout history.

Palettes: GymClues Default (black/charcoal/logo green); Ocean (teal/aqua/sand); Ember (warm charcoal/apricot/olive); Rosewood (burgundy/blush/sage); Daylight (warm ivory/pine/copper). Keep diagrams' original role colors; labels must remain readable in light mode.

Scope: existing GymCluesTheme callers keep their signature; shared UI preferences store a stable theme ID with a safe default; provider changes colors without replacing navigation/session state. Add Theme menu/route/screen. Replace fixed mint UI status colors with theme roles, update system bars, retain fixed scientific artwork. Tests cover five unique IDs, fallback, color contrast, UI selection/persistence, navigation, light guides and phone/tablet layouts. No dependency or database change.

Native adaptation: preserve 20dp gutters and the centered 1040dp canvas. Two columns at 650dp, default card spans both, phone stacks. 48dp buttons expand for text. Browser DOM checks do not inspect Compose; use native screenshots and semantics/behavior checks at 320/390/768/1440dp with enlarged text.

Default refinement: the user supplied a black-and-green reference. Main background #000000; information panels #0C0C0C; 1.5dp outlines #04F730 sampled from the bundled logo. This explicitly overrides the flat-card rule for Default only. Existing card/list/surface components share one Default-only border rule. Training rhythm and the unassigned progress section also receive panels in Default. Preserve all four other themes.
