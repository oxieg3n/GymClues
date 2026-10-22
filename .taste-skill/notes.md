# GymClues native app redesign

Style: true-black-flat; seed 349504594. No redraws. Chosen for short repeated gym sessions and legible dark-mode logging; data-terminal implies a desktop research workflow.

## Product inventory
- Offline Android app using Compose, Room and existing repositories.
- Actual catalog: 298 exercise guides, 35 equipment entries, 24 programs in eight types.
- Assigned programs schedule individual sessions by date; personal rest days shift the program.
- Weight and reps are logged per set; cardio has time and machine-specific measurements.
- Muscle maps separate primary, assisting and stabilizing roles.
- Machine diagrams have numbered setup instructions and safety guidance.
- Custom workouts are built from the same catalog, and saved sessions remain editable.
- The gym scope and music controls belong to every screen.
- Existing GymClues logo and green data accent are recognizable brand assets.
- A new account has zero completed sessions; it must not appear to have completed a program.

## Questions and signature
Home: What am I training today? Lead with the assigned session and one direct start action.
Programs: Which routine fits my schedule? Group all types and keep assignment state explicit.
Workout: What do I record next? Clear set/interval rows and persist actual input.
Library: Which movement or machine do I need? Search/filter lists with shared row alignment.
Guide: How do I set up and perform this safely? Retain diagrams and full coaching.
History: What did I complete? Actual session records and measurements; no invented achievements.
Builder/forms: Which exercises and settings belong to my workout or submission?

Face after user feedback: forest and warm charcoal surfaces, mint actions/navigation/focus, sage section accents, amber recovery actions, real Inter font and the GymClues mark. The user explicitly requested more color after reviewing the monochrome design; this overrides the dealt palette restrictions without a style redraw.
Signature: today's assigned training prescription and a rest-aware next-session schedule. The zero state explicitly asks for a program.

Native adaptation: 48dp primary controls and at least 44dp compact native icon targets (Android accessibility) replace the sheet's 36px desktop buttons. The DOM inspector cannot inspect Android views; native Robolectric captures and bounds/behavior checks cover 320/390/768/1440dp and enlarged text. Keep all persistence and catalog content. Scientific map colors remain their existing semantic role legend.

Grid: phone content width = viewport minus 40dp (20dp gutters). Tablet canvas = min(viewport, 1040dp), content = canvas minus 40dp, with home columns (W - 16dp)/2. All other lists/forms use the same outer edges. Typography: 12/14/16/20/24/30sp; tabular numeric features.
