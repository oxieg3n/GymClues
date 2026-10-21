# Equipment setup and exercise safety

Open [the offline preview](index.html) to review all 31 equipment diagrams and
the safety guidance for every bundled catalog exercise and program prescription.
The Android app uses the same authored content and original vector artwork.

In the app:

- Tap an equipment row or an exercise's equipment chip to open its setup guide.
- Match the diagram's four numbered callouts to the adjustment steps below it.
- Use **Enlarge setup diagram**, then pinch, pan or use the zoom controls.
- Exercise pages now include **Safety tips**, **Injury prevention**, and
  **Common mistakes & corrections**.
- **Safety & form** opens the same coaching from program previews and active
  workouts, including exercises without a full catalog page. Prescribed movement
  alternatives have separate choices, with links to known equipment setups.

The supplement covers 106 catalog exercises and 416 normalized exercise names
across the catalog and all 21 programs. All catalog equipment relationships have
configuration entries. Cable exercises specify pulley position and attachment;
bench exercises specify support position and angle. The original 30-degree bench
setup remains intact where already authored. The Smith bench exercise now also
lists the adjustable bench it needs. Custom exercises receive clearly identified
general checks when no specific guide has been authored; user safety notes remain
visible.

## Maintaining the content

Run `python tools/build_training_guides.py` from the repository. It produces
`app/src/main/assets/training_guides.json`, 31 Android vector resources named
`setup_*.xml`, the matching SVGs in this directory, and the offline preview.
It uses only the Python standard library.

Sources of authored content:

- `tools/equipment_setups.py`: numbered diagrams and equipment adjustment steps.
- `tools/training_safety.py`: specific movement families, safety cues, progression
  and range suggestions, and common errors paired with corrections.
- `tools/exercise_configurations.py`: catalog exercise positions, bench angles
  and cable attachments.
- `tools/build_muscle_maps.py`: existing, explicit program movement aliases.

Generation fails for missing equipment, unmapped exercise names, missing movement
safety, incomplete cable/bench configurations, and mistakes without corrections.
The original workbook-generated assets remain the source for IDs, prescriptions,
instructions and alternatives; the supplemental repository layer adds the new
guides and preserves existing exercise-specific setup and safety notes.

## Interpretation and references

These are original coaching summaries and schematic illustrations for typical
technique. Controls, pad shapes, rail angles, start resistance and load limits vary
by model; the actual machine placard and manufacturer instructions take priority.
The diagrams identify setup principles rather than exact mechanical dimensions,
universal notch numbers, repair procedures or a guarantee of injury prevention.
Individual training restrictions require suitable individual instruction.

General references checked during preparation:

- [Mayo Clinic: weight-training technique](https://www.mayoclinic.org/healthy-lifestyle/fitness/in-depth/weight-training/art-20045842)
- [ACE: exercise library](https://www.acefitness.org/resources/everyone/exercise-library/)
- [Life Fitness: manuals and resources](https://support.lifefitness.com/hc/en-us/p/Manuals-Resources)
- [Precor: leg extension and knee-pivot alignment](https://www.precor.com/en-US/products/VSL005BP)
- [NASM: bench press](https://www.nasm.org/resource-center/exercise-library/barbell-bench-press)
- [NASM: deadlift](https://www.nasm.org/resource-center/exercise-library/barbell-deadlift)
- [NASM: lying leg curl](https://www.nasm.org/resource-center/exercise-library/lying-leg-curl)
- [NASM: supported row](https://www.nasm.org/resource-center/exercise-library/seated-machine-row-close-grip)

These references provide general context and selected technique checks. They do
not independently certify every authored guide or illustration.

## Validation

All six `TrainingGuideTest` checks and six existing `MuscleMapTest` checks passed.
Coverage includes rendering all setup resources, every catalog equipment/exercise
relationship, all program prescriptions, movement choices, popup navigation,
equipment-guide links and zoom controls. The debug APK built successfully.
Java 25 validation used an ignored Gradle init script to override test-runtime ASM
for Robolectric 4.13. Test fixtures dispose UI and catalog observers before resetting
SQLite so their work does not leak into the next test.

Set `TRAINING_GUIDE_PREVIEWS` to a workspace folder to save rendered equipment
images and UI captures while running the tests.
