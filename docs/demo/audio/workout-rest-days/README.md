# Today's workout and rest days narration

- [Narrated video](../../videos/workout-rest-days-narrated.mp4)
- [Aligned MP3](workout-rest-days-narration.mp3)
- [WAV master](workout-rest-days-narration.wav): 48 kHz mono PCM, exactly 45 seconds.
- [Section script](voiceover-sections.md), [generation manifest](tts-manifest.json), and [timing manifest](synchronized-manifest.json).

Twelve narration sections generated locally with VoiceStudio's **GymClues** profile and `k2-fsa/OmniVoice` on CUDA, using English, 32 generation steps, and the profile's existing voice direction/seed. The codex-tts compatible API workflow supplies a target duration for each section. No private profile identifiers or reference recordings are included here.

The opening two seconds remain silent. Sections are measured, fitted to the existing caption cues without changing pitch, and normalized together toward -16 LUFS / -1.5 dBTP. The original silent video and its visual stream are preserved.

Rebuild the audio and narrated video:

```powershell
python docs/demo/source/sync-home-audio.py --ffmpeg build/demo-tour/ffmpeg.exe --tour workout-rest-days
```

Technical validation covers section order/text, audio presence, cue timing, silence, levels, decoding, and unchanged video content. Listening review was unavailable in the agent session.
