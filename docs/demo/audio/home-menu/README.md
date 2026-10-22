# Home and menu narration

- [Aligned MP3](home-menu-tour-narration.mp3): 45 seconds, including two seconds of opening silence.
- [WAV master](home-menu-tour-narration.wav): 48 kHz, mono, 16-bit PCM, exactly 45 seconds.
- [Narrated video](../../videos/home-menu-tour-narrated.mp4): original video stream plus synchronized AAC narration.
- [Section script](voiceover-sections.md): the original English narration, formatted for the codex-tts skill.
- [Generation manifest](tts-manifest.json): source section order, text, sizes, and measured durations.
- [Synchronization manifest](synchronized-manifest.json): each section's video cue and pitch-preserving tempo adjustment.

Generated locally with **VoiceStudio 0.5.6**, using the existing voice profile named **GymClues** and its `k2-fsa/OmniVoice` engine on CUDA. Generation used the codex-tts compatible API workflow against VoiceStudio's local speech endpoint, with English language, the profile's existing voice direction/seed, 32 generation steps, and each section's target duration. The profile's reference recording and internal identifier remain in VoiceStudio and were not included in the public artifacts. No hosted TTS service was used.

The numbered WAV files are the original VoiceStudio sections, replacing the earlier Windows speech clips. Their timings were measured before fitting them to the existing video. VoiceStudio's duration control keeps the required pitch-preserving tempo adjustment at or below 1.081x. The synchronization step preserves a minimum 0.12-second pause at the end of each cue and normalizes the assembled master toward -16 LUFS with a -1.5 dBTP ceiling. The original silent video remains available.

Rebuild the synchronized audio and narrated video with:

```powershell
python docs/demo/source/sync-home-audio.py --ffmpeg build/demo-tour/ffmpeg.exe
```

Technical checks cover section contents/order, nonempty audio, cue alignment, opening silence, clipping, stream decoding, and video-stream preservation. Listening review was unavailable in the agent session; review pronunciation and delivery before publishing.
