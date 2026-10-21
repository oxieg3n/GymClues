# HIIT and cardio

The HIIT category contains three established interval formats. Each program has a source link in the app. The four-week calendar (three sessions per seven-day cycle) is a GymClues scheduling choice; it does not reproduce every study's training schedule.

| Program | Work and recovery | Session length | Source |
|---|---|---|---|
| Tabata 20/10 Cycling | Eight 20-second efforts and 10-second recovery periods; the app adds five minutes easy before and after | 14 minutes | [Ritsumeikan University](https://www.ritsumei.ac.jp/research/radiant/eng/2019-2021/story5.html/) |
| Norwegian 4×4 | Four four-minute efforts, three minutes easy between efforts, ten-minute warm-up, five-minute cool-down | 40 minutes | [NTNU CERG](https://www.ntnu.edu/cerg/advice) |
| McMaster One-Minute Workout | Three 20-second cycling sprints, two minutes easy between, two-minute warm-up, three-minute cool-down | 10 minutes | [McMaster University](https://news.mcmaster.ca/no-time-to-get-fit-think-again/) |

The original Tabata and McMaster intensities are demanding. Descriptions distinguish the research protocols from lower-intensity adaptations. Equipment settings are individualized; the app does not set a universal treadmill speed or resistance level. Use a clock or the machine's interval mode; GymClues does not control equipment or automatically time intervals.

Use **Add exercise → Cardio** during a workout, or select cardio in the custom workout builder. Outdoor and machine variations have separate guides so outdoor activities do not require a treadmill or stationary bike to be marked available.

| Activity | Logged fields |
|---|---|
| Walking, jogging, running (including treadmill variations) | Minutes, speed in mph, incline in percent |
| Climbing stairs / stair machine | Minutes, speed in steps/min, floors |
| Bicycling / stationary bicycling | Minutes, miles, machine resistance setting |
| Rowing machine | Minutes, meters, machine resistance or damper setting |
| Jumping jacks | Sets and repetitions, without a weight field |

Time accepts decimal minutes or `m:ss`. Time is required for a cardio record; other measurements are optional when no readout is available. A zero speed, flat incline, or zero resistance is preserved. Non-finite and invalid negative values are rejected; decline incline is allowed. A rower's air damper changes stroke feel and is not a universal intensity scale. See [Concept2's technique guidance](https://www.concept2.com/blog/10-ways-to-row-like-a-pro).

**Record cardio** saves a completed block once. Finish Workout also saves edited, valid cardio entries. Untouched preset targets are not treated as completed exercise. Completed Sessions supports viewing, editing, deleting, and sharing cardio measurements. Lifting volume remains based on weight and reps.

Room schema 6 adds one nullable `cardio` column to `SetLog`, storing typed measurements as JSON through the existing Room converter pattern. Migration 5→6 preserves old lifting records and saved workouts; no destructive migration is used.

Run `python tools/build_cardio_content.py` to rebuild the cardio catalog, HIIT programs, equipment diagrams, muscle diagrams and coaching supplements. Existing workbook and strength-program assets remain independent.
