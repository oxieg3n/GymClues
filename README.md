# <h1>GymClues</h1>
<h3>Less guesswork. More time for your workout.</h3>

<b>GymClues</b> helps you figure out the equipment at your gym and put together your workout. Browse exercises by muscle group or workout style, follow a beginner-friendly plan, or save your own. You can easily switch exercises when a machine is busy, log your lift weight and reps, and it even keeps Spotify within reach. <br>
<br>Oh, and your gym isn’t listed? Add it and its equipment to help the next person out.

<h1><b></b>Features</h1>
- Workout tracking with adjustable sets and in-session exercise additions or removals.
<br>- Custom workouts with configurable sets, reps, rest periods, and exercise order.
<br>- Exercise and equipment guides with exercise-specific setup instructions.
<br>- Equipment tutorials supporting embedded YouTube/MP4 videos and Planet Fitness QR links.
<br>- Nearby gym search using GPS or gym name/city.
<br>- Gym and equipment submissions with camera/gallery photos and QR scanning.
<br>- Music controls showing track information, play/pause, and previous/next.
<br>- Five saved color themes under Menu > Theme: GymClues Default, Ocean, Ember, Rosewood, and Daylight. [Preview the themes](docs/taste-redesign/themes/index.html).

<br> With <b>GymClues</b> there is less guessing & more lifting.

## Exercise library content

The library includes guides for every bundled program exercise, grouped by primary muscle. Workout cards link to the full setup, movement, breathing, muscle map, and safety guidance. Alternative spellings share pages; distinct movement options link to their own guides.

To rebuild the program exercise asset and the [browser preview](docs/program-exercises/index.html), run `python tools/build_program_exercises.py` after updating the authored recipes and aliases in `tools/`. Existing catalog IDs and historical program references are preserved.

## HIIT and cardio

Three sourced HIIT programs are available under Workout programs → HIIT. Use Add exercise → Cardio to add walking, jogging, running, bicycling, rowing, jumping jacks or stair climbing to a workout. Cardio machines have their own setup guides, and cardio entries use activity-specific measurements instead of lifting weight fields. See [program sources, tracking fields and rebuild instructions](docs/hiit-cardio.md).
