# GymClues site and interactive user guide

Open `Website/index.html` directly or serve `Website/` with any static web server. GitHub Pages publishes this folder to <https://oxieg3n.com/GymClues/>. Everything needed for the page is local; no accounts, third-party widgets, or network requests are required for the guide itself.

The interactive guide uses actual screenshots from the `PublicReleasev1` Android APK (GymClues 4.5, build 20). Transparent guide buttons overlay controls on those screenshots. This is a guided preview, not a web port of GymClues, and does not save anything. The written walkthroughs also work without JavaScript. Keep each data entry's accessible step text and measured hotspot in sync with its screenshot. Preserve the source PNGs and exact APK provenance in the private capture handoff; the page ships only reduced WebP screenshots.

The four original demos, older feature gallery and theme previews remain available. The video sources, fallback links, posters and download links were preserved. In the original demo player, switching demos pauses hidden players, URL fragments select their matching panel, and all videos remain usable without JavaScript. Reduced-motion settings disable animation.

The three Android download buttons point to the exact public release asset. Video download links still point to the original MP4 files.

Run from the repository root:
- `node Website/check-site.mjs`
- `node Website/check-guide.mjs`
