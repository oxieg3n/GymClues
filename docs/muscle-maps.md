# Exercise muscle maps

Every exercise prescription in all 21 bundled programs has an offline front/back
anatomy image. Open **Muscles** beside its name in the session preview or active
workout. Each popup provides readable muscle names as well as three colors:

- Coral: target muscles (or mobility / recovery regions for warm-ups).
- Gold: assisting movers.
- Blue: stabilizers.

Open [muscle-maps.html](muscle-maps.html) in a browser to review every exercise,
switch between prescribed movement options, and download labelled SVG diagrams.
The page is self-contained and works offline. Android uses bundled vector images
in `app/src/main/res/drawable/mm_*.xml` without a network request or new dependency.

## Content and interpretation

`tools/build_muscle_maps.py` contains the original vector anatomy artwork, named
muscle regions, explicit exercise aliases, muscle roles and technique notes.
Run `python tools/build_muscle_maps.py` after changing those definitions or adding
program exercises. It generates the Android images, `assets/muscle_maps.json`,
and the review page. Generation fails for an unmapped program exercise, missing
anatomy region or conflicting muscle role. Equivalent punctuation/capitalization
shares artwork; distinct movements have explicit assignments, never keyword-based
guesses. Compound prescriptions such as “pull-up or machine row” have selectable
maps, and the clean-and-press has separate clean and press phases.

Roles are editorial summaries of typical technique, rather than measured muscle
activation or an exhaustive list. Support, grip, range, body position and individual
anatomy affect participation. An isometric target (for example, abs in a plank)
can be a target even though it stabilizes rather than moving a joint. Deep muscles
are projected schematically onto surface regions; the text gives their names.
Maps show both sides for alternating movements, not an assertion that both sides
are equally active during a single-arm or single-leg repetition. Warm-up stretches
and foam rolling identify mobility or recovery regions rather than treating those
regions as simultaneous prime movers. Unspecified techniques have assumptions
stated in the popup notes.

General anatomy/movement references used while preparing the summaries:

- [NASM exercise library](https://www.nasm.org/resource-center/exercise-library)
- [NASM barbell bench press](https://www.nasm.org/resource-center/exercise-library/barbell-bench-press)
- [NASM barbell deadlift](https://www.nasm.org/resource-center/exercise-library/barbell-deadlift)
- [NASM dumbbell Romanian deadlift](https://www.nasm.org/resource-center/exercise-library/dumbbell-romanian-deadlift)
- [NASM plank](https://www.nasm.org/resource-center/exercise-library/plank)
- [NASM supported row](https://www.nasm.org/resource-center/exercise-library/seated-machine-row-close-grip)
- [ACE exercise library](https://www.acefitness.org/resources/everyone/exercise-library/)

These references inform the summaries; the sources do not independently validate
every stabilizer assignment or the schematic artwork. The artwork is original,
generated deterministically as native vectors rather than AI raster images.

## Verification

`MuscleMapTest` checks coverage against the actual programs asset, loads every
image, compares important movement variants, renders representative diagrams,
and exercises the preview and active-workout popups, including set-input
preservation. Optional `SHARE_OUT` points test previews at a local output folder.

Validation on this workstation built the debug APK and passed all six muscle-map
tests. Robolectric 4.13 required a temporary test-runtime ASM 9.8 override under
Java 25; the override was applied through an ignored Gradle init script, without
changing application dependencies. The broader suite reported 17 failures from
background database observers during SQLite test cleanup.
