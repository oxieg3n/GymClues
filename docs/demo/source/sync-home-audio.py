"""Fit the TTS skill's measured section clips to an existing demo's cues."""
import argparse
import json
from pathlib import Path
import subprocess


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--ffmpeg", type=Path, required=True)
    parser.add_argument("--tour", choices=("home-menu-tour", "assign-program", "workout-rest-days"), default="home-menu-tour")
    args = parser.parse_args()
    ffmpeg = args.ffmpeg.resolve()
    demo = Path(__file__).resolve().parents[1]
    tour = args.tour
    audio_folder = "home-menu" if tour == "home-menu-tour" else tour
    build = demo.parents[1] / "build" / ("home-narration" if tour == "home-menu-tour" else tour + "-narration")
    build.mkdir(parents=True, exist_ok=True)
    audio = demo / "audio" / audio_folder
    manifest = json.loads((audio / "tts-manifest.json").read_text(encoding="utf-8"))
    cues = (demo / (tour + ".srt")).read_text(encoding="utf-8").strip().split("\n\n")
    assert len(cues) == len(manifest["sections"]) > 0, "Unexpected section count"

    def run(*arguments):
        subprocess.run([str(ffmpeg), "-y", "-hide_banner", "-loglevel", "error", *map(str, arguments)], check=True)

    def seconds(stamp):
        hours, minutes, tail = stamp.replace(",", ".").split(":")
        return int(hours) * 3600 + int(minutes) * 60 + float(tail)

    intro = build / "intro-silence.wav"
    run("-f", "lavfi", "-i", "anullsrc=r=48000:cl=mono", "-t", "2", "-c:a", "pcm_s16le", intro)
    pieces, timings = [intro], []
    previous = 2.0
    for index, (cue, section) in enumerate(zip(cues, manifest["sections"])):
        lines = cue.splitlines()
        start, end = map(seconds, lines[1].split(" --> "))
        assert start == previous and end > start, "Noncontiguous narration cues"
        assert " ".join(lines[2:]) == section["text"], "TTS text differs from video captions"
        source = audio / section["file"]
        assert source.is_file() and source.stat().st_size > 44, "Empty TTS clip"
        duration = section["duration_seconds"]
        assert duration and duration > 0, "Missing measured TTS duration"
        slot = end - start
        tempo = max(1.0, duration / (slot - 0.12))
        assert tempo <= 1.65, f"Regenerate this section at a faster speaking rate: {section['title']}"
        aligned = build / f"aligned-{index:02d}.wav"
        run("-i", source, "-af", f"atempo={tempo:.9f},apad,atrim=duration={slot},asetpts=N/SR/TB",
            "-ar", "48000", "-ac", "1", "-c:a", "pcm_s16le", aligned)
        pieces.append(aligned)
        timings.append({"title": section["title"], "text": section["text"], "source_file": section["file"],
                        "source_duration_seconds": duration, "start_seconds": start, "end_seconds": end,
                        "tempo_multiplier": round(tempo, 6), "minimum_end_pause_seconds": 0.12})
        previous = end
    assert previous == 45.0, "Video must remain 45 seconds"
    listing = build / "aligned-concat.txt"
    listing.write_text("\n".join("file '" + str(p).replace("\\", "/").replace("'", "'\\''") + "'" for p in pieces), encoding="utf-8")
    wav = audio / (tour + "-narration.wav")
    mp3 = audio / (tour + "-narration.mp3")
    narrated = demo / "videos" / (tour + "-narrated.mp4")
    run("-f", "concat", "-safe", "0", "-i", listing, "-af", "loudnorm=I=-16:TP=-1.5:LRA=7",
        "-ar", "48000", "-ac", "1", "-c:a", "pcm_s16le", wav)
    run("-i", wav, "-c:a", "libmp3lame", "-b:a", "192k", mp3)
    run("-i", demo / "videos" / (tour + ".mp4"), "-i", wav,
        "-map", "0:v:0", "-map", "1:a:0", "-c:v", "copy", "-c:a", "aac", "-b:a", "192k",
        "-t", "45", "-movflags", "+faststart", narrated)
    result = {"provider": manifest["provider"], "model": manifest["model"], "voice": manifest["voice"],
              "language": "en-US", "generated_with": "codex-tts/scripts/tts.mjs",
              "duration_seconds": 45, "intro_silence_seconds": 2, "sample_rate_hz": 48000,
              "loudness_target_lufs": -16, "true_peak_target_dbtp": -1.5, "sections": timings}
    (audio / "synchronized-manifest.json").write_text(json.dumps(result, indent=2) + "\n", encoding="utf-8")
    print(f"Aligned {len(timings)} sections to 45 seconds. Maximum tempo adjustment: {max(t['tempo_multiplier'] for t in timings):.3f}x")
    print(f"Audio: {mp3}\nNarrated video: {narrated}")


if __name__ == "__main__":
    main()
