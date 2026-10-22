# Emulator demo captures

Open [the gallery](index.html) to browse 13 screenshots and play the four videos locally. The gallery has no external dependencies.

The redesigned gallery includes a tutorial playlist, labeled feature screenshots and a five-theme lineup. Selecting a tutorial pauses the other players and shows its matching downloads. Direct links such as `index.html#lesson-assign` work locally. Without JavaScript all four players stay available. Motion follows the device's reduced-motion preference. Inter and the logo are included locally in `assets/`, with the font's OFL license.

Run the gallery's resource and interaction checks with `node docs/demo/source/check-gallery.mjs` from the project root. Design notes and responsive browser captures are in `.taste-skill/demo/`.

Captured on October 22, 2026 from the running Android Studio emulator (`emulator-5554`, `sdk_gphone64_x86_64`) using ADB. These are actual app captures, not rendered mockups.

- Screenshots: original PNG, 1080 × 2400 pixels.
- [Narrated Home and menu guided tour](videos/home-menu-tour-narrated.mp4): exactly 45 seconds, 1080 × 2400, 30 fps, H.264/AAC MP4. Replaces the workout-and-exercise walkthrough in the gallery. The [silent master](videos/home-menu-tour.mp4) is preserved.
- [Assign a workout program](videos/assign-program-narrated.mp4): exactly 45 seconds, 1080 × 2400, 30 fps, H.264/AAC MP4, narrated with VoiceStudio's GymClues profile.
- [Today's workout and rest days](videos/workout-rest-days-narrated.mp4): exactly 45 seconds, 1080 × 2400, 30 fps, H.264/AAC MP4, narrated with VoiceStudio's GymClues profile.
- [Color themes](videos/themes.mp4): 16.45 seconds, 1080 × 2400 MP4.

The assignment and rest-day changes were staged offline for the tutorials. The emulator's original app data was backed up first and restored afterward, with every saved file hash checked before relaunching. The emulator is back on Home with the original assigned program and rest rules. No sets were logged or sessions completed. No application source files were changed for these captures.

## Home and menu tour

The new tour uses current emulator captures with an edited two-second branded launch card, animated hand pointer, a click pulse when opening the hamburger menu, smooth scrolling, and dimmed spotlights around each described feature. The overlays are part of the video; the app itself was not changed. Menu entries are highlighted without navigating away, keeping the tour focused on Home and the menu.

- 00:00-00:02: logo opening, silent.
- 00:02-00:23: gym selection, music, today's training, training rhythm, program progress, and upcoming workouts/calendar/rest-day controls.
- 00:23-00:25: hand clicks the hamburger menu.
- 00:25-00:45: workout programs/custom workouts, completed sessions, active program, exercises, equipment, personal exercises, themes, and admin.

[Plain TTS script](voiceover.txt), [timed narration guide](voiceover-timed.md), and [SRT captions](home-menu-tour.srt) match the on-screen narration text. The gallery now plays the English narrated version using **VoiceStudio's GymClues voice profile**, generated locally with its OmniVoice engine through the codex-tts compatible API workflow. This replaces the earlier Microsoft Zira narration.

Download the aligned [narration MP3](audio/home-menu/home-menu-tour-narration.mp3) or [WAV master](audio/home-menu/home-menu-tour-narration.wav). The first two seconds remain silent. Fourteen separately generated sections are fitted to the existing cues with pitch-preserving tempo adjustments and normalized together. See the [audio notes and manifests](audio/home-menu/README.md) for the measured durations and synchronization details.

To render again on Windows with a local FFmpeg executable:

```powershell
./docs/demo/source/render-tour.ps1 -FFmpeg ./build/demo-tour/ffmpeg.exe
```

The script validates the timeline and capture dimensions, writes the silent video to `build/demo-tour/home-menu-tour.mp4`, and generates the narration scripts and poster frames. After verifying the output, copy the MP4 into `docs/demo/videos/`. The three source PNGs are in `source/`; the logo comes from the app's existing drawable. The portable FFmpeg executable is a build tool and is not an app dependency. To realign the generated audio sections and rebuild the narrated version, run `python docs/demo/source/sync-home-audio.py --ffmpeg build/demo-tour/ffmpeg.exe`.

## Program assignment tutorial

The second guided tutorial starts without an assigned program and demonstrates Menu > Pick a Program > Bro split > Outlift Perfect Bro Split > Details > Assign to me. It selects Today in the start-date dialog, confirms Assign, shows the disabled Already Assigned button, and returns through the three back arrows to Home.

[TTS script](assign-program-voiceover.txt), [timed narration guide](assign-program-timed.md), and [captions](assign-program.srt).

[Narration MP3](audio/assign-program/assign-program-narration.mp3), [WAV master](audio/assign-program/assign-program-narration.wav), and [audio generation notes](audio/assign-program/README.md). Eleven independently generated narration sections match the existing cues.

## Daily workout and rest days tutorial

The third guided tutorial opens today's assigned session through Home > Preview session > Start workout, shows the editable session fields, then returns to Home and scrolls to Rest Day. It demonstrates Set Today as Rest Day, shows the session moving to tomorrow, and opens Rest Day > Plan Rest Day. Selecting Sun under Every week saves a repeating weekly rest day immediately. Single days > Add date is highlighted as the separate option for one-off dates.

[TTS script](workout-rest-days-voiceover.txt), [timed narration guide](workout-rest-days-timed.md), and [captions](workout-rest-days.srt).

[Narration MP3](audio/workout-rest-days/workout-rest-days-narration.mp3), [WAV master](audio/workout-rest-days/workout-rest-days-narration.wav), and [audio generation notes](audio/workout-rest-days/README.md). Twelve independently generated narration sections match the existing cues.

Both new videos use the same two-second silent logo opening, hand pointer, click pulses, dimmed spotlights, and on-screen narration as the first tour. Both now include English audio using the same VoiceStudio GymClues profile, OmniVoice engine, and generation settings as the Home tour. The original silent masters remain available as `videos/assign-program.mp4` and `videos/workout-rest-days.mp4`.

To rebuild either new video from its captured screens and timeline:

```powershell
./docs/demo/source/render-tour.ps1 -FFmpeg ./build/demo-tour/ffmpeg.exe -Timeline ./docs/demo/source/assign-program.json
./docs/demo/source/render-tour.ps1 -FFmpeg ./build/demo-tour/ffmpeg.exe -Timeline ./docs/demo/source/workout-rest-days.json
```

Validate and copy the resulting MP4s from `build/demo-tour/` to `docs/demo/videos/`. The renderer checks contiguous scene timing, highlight/cursor bounds, click timing, capture sizes, and narration pacing. Adjacent back-navigation shots share one narration cue so the TTS script does not repeat a sentence for each back tap.

To realign their generated audio and rebuild the narrated versions:

```powershell
python docs/demo/source/sync-home-audio.py --ffmpeg build/demo-tour/ffmpeg.exe --tour assign-program
python docs/demo/source/sync-home-audio.py --ffmpeg build/demo-tour/ffmpeg.exe --tour workout-rest-days
```

The captures demonstrate UI flows and do not replace physical-device testing.
