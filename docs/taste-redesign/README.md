# GymClues native UI redesign

[Open the screenshot gallery](index.html). These are renders of the native Compose app, with test-only data, rather than UI mockups. The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## Direction

Taste-skill app workflow: **true-black-flat**, seed **349504594**, no redraws. The alternative offered was data-terminal; the selected direction suits repeated gym sessions and quick recording on a phone. The draw and product inventory are in `.taste-skill/direction.json` and `.taste-skill/notes.md`.

Following feedback that the original palette was too black and white, the product face now combines forest and warm charcoal surfaces, mint controls and navigation, sage section accents, amber recovery actions and bundled Inter. This is an explicit user-requested palette override to the dealt monochrome rules; the original seed and layout foundation remain recorded above. The signature is today's actual training prescription with a direct start action, followed by a schedule that accounts for recovery days. A new account is explicitly unassigned; it no longer appears to have completed a program.

| Screen | Question it answers |
| --- | --- |
| Today | What am I training today, and how do I start? |
| Program types and routines | Which routine fits my schedule? |
| Session preview and logger | What is prescribed, and what do I record next? |
| Custom workout and picker | Which exercises belong in my session? |
| Exercise library | Which primary muscle am I training? |
| Equipment and exercise guides | How do I set up and perform this safely? |
| Completed sessions | What did I actually finish and record? |
| Gym and admin forms | Which information is needed for the next action? |

## Implementation and scope

Shared colors, typography, buttons, fields, cards, list rows and section labels apply throughout the app. Navigation and music controls use a compact persistent header. Phone content has 20dp margins; wide layouts use a centered 1040dp canvas, with two home columns at 700dp and above. Counts use tabular figures. Labels remain above editable fields, and numeric controls align with the input itself.

Fields retain keyboard, password, validation, enabled/read-only and accessibility behavior. Primary controls have a 48dp minimum height and expand for text; compact native icon targets remain at least 44dp. The picker opens expanded. History adapts its column count to available width. Assigned buttons remain disabled and readable. Scientific diagram colors keep their existing muscle-role and setup meanings.

Inter is bundled for offline use, with its SIL license at `app/src/main/assets/fonts/OFL-Inter.txt`. No application dependency or database schema was added. Catalog entries, logging, migrations, assignment handling, safety content, diagrams, gym equipment controls and walkthrough targets remain connected to the existing repositories.

## Visual review

Three native screenshot rounds, including 320dp and 390dp phones, 768dp tablet, 1440dp wide canvas and 1.3x text:

1. Checked hierarchy, common edges, neutral surfaces, folded menu, readable zeros and reachable actions. Identified instructions narrowed by the remove control, a partially expanded picker and inconsistent margins.
2. Gave workout instructions the full card width, expanded the picker, unified margins and made history responsive. Removed the progress component's end marker so a zero bar has no green mark. Reviewed program, library, safety, muscle-map, equipment, builder, history, gym and PIN screens.
3. Aligned set numbers, checkboxes and search controls with the new labeled inputs. Reviewed the resulting phone and wide renders, including disabled assignment, strength and cardio logging. No additional visual changes remained from that review.

Palette follow-up: regenerated all 33 gallery renders and reviewed home, programs, workout entry, navigation, tablet/wide layouts and enlarged text. Mint actions and forest surfaces now carry the brand throughout the UI; the native contrast checks pass.

The screen's main task leads the hierarchy. Panels share the same outer edges; lists scroll on phones. Wide screens keep a readable centered canvas without stretching sparse content. There are no decorative gradients, icon tiles or invented achievements. Mint identifies actions, navigation, focus and progress; amber identifies recovery. Scientific artwork retains its original role colors.

Data checked against the displayed source: 24 programs / eight types, 35 equipment guides, 298 exercise guides; B1 day one has six exercises and 21 working sets. Preview history contains three dated 75lb x 8 entries, only one within the newly assigned program, so the progress is 1 / 50 = 2%. After setting today as recovery, that prescription shifts to tomorrow. The completed-session fixture contains one exercise and one entry. Draft cardio values are illustrative test input, not claimed user performance.

Updated text contrast on the #1C231E surface: primary 14.5:1 and secondary 9.4:1. Secondary text on the forest feature card is 6.8:1; primary button text is 10.2:1; input outlines are 4.0:1 against the field fill. Mint primary buttons have dark text. Empty and populated states are shown separately in the gallery.

## Validation and limits

- Debug APK rebuilt successfully. The full redesign suite passed **61 tests** before the palette follow-up; **18 targeted tests passed after the color changes**, with zero failures or skips. Includes navigation, saved custom workouts, cardio validation and persistence, migrations, primary-muscle grouping, all guide assets, session history, rest scheduling, admin authentication and six new redesign checks.
- Existing test fixtures were updated for the HIIT catalog and expanded exercise library. Older fixtures now cancel background observers and close their databases between tests.
- `git diff --check` passed. Android lint was attempted but its analyzer fails on the installed Java runtime with `25.0.3`; it did not produce a usable lint result.
- Taste-skill's browser inspector and source linter cannot assess native Android views or Kotlin styling. Native screenshots, semantics, control-bound checks, interaction tests and token contrast checks substitute for those browser checks. No browser inspection pass is claimed.
- Robolectric previews use API 34 at mdpi. The preview test has a documented, test-only workaround for Compose 1.7's prefetch clock; it is not a scrolling-performance benchmark.
- No physical-device, OEM keyboard, live music, camera or GPS verification was performed. No release signing or publishing was performed.

Commands used on this Java 25 host: `gradlew.bat :app:assembleDebug :app:testDebugUnitTest -I build/robolectric-java25.init.gradle --console=plain`. The local init script overrides test-host ASM to 9.8 for Robolectric compatibility; it does not change application dependencies. Set `TASTE_PREVIEWS` to an output directory to regenerate the native redesign screenshots.
