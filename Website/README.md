# GymClues landing page

Open `index.html` directly or serve this folder with any static web server. The existing Pages workflow publishes `Website/`; all featured media, the logo and the licensed Inter font are included here, so no parent directory or CDN is required.

The page includes four playable demos, an eight-feature screenshot explorer and five native theme previews. Selectors support keyboard links and URL fragments such as `#demo-assign` and `#screen-muscles`. Switching demos pauses the hidden players. Without JavaScript every video and feature image remains visible. Reduced-motion settings disable animation.

Featured media copies come from `docs/demo/screens`, the four finished recordings in `docs/demo/videos`, and `docs/taste-redesign/themes/screens/home-*-390.png`. Keep these copies in sync when updating a recording or screenshot. The separate demo gallery retains scripts, audio masters and other production resources.

Every APK button points to the supplied v1.1.1 GitHub release asset. Store badges and placeholder legal links are omitted. The Source code and Support links lead to the repository and its Issues page.

Run `node Website/check-site.mjs` from the project root. Responsive inspection captures, the direction sheet and critique are in `.taste-skill/landing/`.
