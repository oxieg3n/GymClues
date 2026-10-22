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

## Nutrition

Open **Menu → Nutrition**, or the nutrition summary on Home, to track food alongside your workouts. The private, offline-first diary includes breakfast, lunch, dinner and snacks; editable portions; calories and macros; custom foods; favorites, recent and frequent foods; saved meals; recipes; meal/day copying; and quick calorie/macro entry. Manual goals can differ between scheduled workout and rest days. Weight and water tracking and 7/30/90-day history combine nutrition with completed workouts and lifting volume.

Food search uses **Open Food Facts** without an API key, with explicit online searches and UPC/EAN barcode lookup through the existing camera scanner. Previously cached and custom foods remain usable offline. Missing nutrition stays unknown; historical entries retain their original nutrition facts. Camera access is needed only for scanning; typed barcode entry and manual logging remain available.

Open Food Facts data is **ODbL 1.0 / DbCL 1.0** and retains source attribution in a separate provider cache. USDA FoodData Central (**CC0**) is optionally supported through an operator-provided HTTPS proxy configured in Nutrition goals. The server must hold the USDA API key; no USDA key belongs in the APK. No working GymClues cloud backend is assumed. Do not publish a combined food database without reviewing the licenses.

The additive Room **6 → 7** migration preserves workout tables and data. Nutrition remains local and is excluded from community sharing and the sync outbox. AI logging and Health Connect are future integrations, not features of this build. See [Nutrition implementation, provider configuration, attribution and tests](docs/NUTRITION_IMPLEMENTATION.md).

With a compatible Android SDK and JDK installed, run `./gradlew :app:assembleDebug :app:testDebugUnitTest`. On Windows use `gradlew.bat`. Tests include real Room migrations and deterministic provider fixtures; they do not need API keys or live network requests.
