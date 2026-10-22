# GymClues color themes

[Open the native screenshot gallery](index.html).

Menu > **Theme** offers exactly five choices:

| Theme | Palette | Mode |
| --- | --- | --- |
| GymClues Default | Black, charcoal and logo-green outlines | Dark |
| Ocean | Teal, aqua and sand | Dark |
| Ember | Warm charcoal, apricot and olive | Dark |
| Rosewood | Burgundy, blush and sage | Dark |
| Daylight | Ivory, pine and copper | Light |

Each card previews its own background, featured-card color and primary control. The active card has a check, an outline and a disabled Selected button. The screen also names the current theme above the choices. Use theme changes the app immediately and saves the choice on this device. Missing or unrecognized selections use GymClues Default. Default now follows the supplied logo reference: #000000 app background, #0C0C0C information panels and 1.5dp #04F730 outlines. The green was sampled from the bundled logo. Ocean, Ember, Rosewood and Daylight keep their existing palettes.

## Taste-skill direction

Settings sheet: **true-black-flat**, seed **1784168129**, no redraws. The existing layout and Inter typography are retained. A later user reference explicitly adds green outlines and charcoal information sections to Default; this overrides the flat-panel rule for that theme only. The user's request for four additional palettes explicitly overrides the sheet's monochrome restrictions and adds a light option. The draw and inventory are recorded in `.taste-skill/themes/`.

Question: which colors feel right for my workout? The signature is five real Material previews with working selection buttons, using the app's shared controls. The default spans the grid; the four alternatives form two balanced rows on wide screens. Phones use one column. The 20dp gutters, centered 1040dp canvas, expanding 48dp controls and enlarged-text support follow the existing native design.

The palettes apply to navigation, forms, cards, progress and system bars. Light mode uses dark status-bar icons and readable guide labels. Muscle-map artwork and its color swatches retain their original role colors; the labels use the theme's foreground. Safety and setup text use darker gold and blue in Daylight. No demonstration data is added to production screens.

## Validation

- **69 unit and Compose tests passed**, zero failures or skips; debug APK built successfully.
- The four alternate home-screen previews matched their previous PNG hashes exactly after the Default-only update.
- Eight theme checks cover exactly five stable IDs, preservation of the default, unknown/missing ID fallback, Default-only panel borders, all five selection buttons, saved-choice restoration after rebuilding the UI, switching back to Default, live color changes retaining workout input, system bars, light-mode guides, and narrow/tablet/wide layouts.
- All five palettes pass 4.5:1 contrast for the tested text/background roles and 3:1 for input outlines. Checks include secondary text, disabled buttons, featured cards, links, recovery and error colors.
- Native screenshot review at 320, 390, 768 and 1440dp, including 1.3x text. First review identified a need to show the current selection above the scrolling cards; second review confirmed the added label and reachable controls. The tablet/wide layouts display all five choices together, with shared edges and balanced rows. No gradient, glow or decorative icon tile was added.
- Screenshots use an empty account. The Daylight workout's 75lb / 8-rep values are test input only. Diagram roles and real equipment content remain unchanged.
- Browser DOM tools cannot inspect Compose; native renders, semantics, behavior, bounds and contrast checks substitute for that browser inspection. No browser pass is claimed.
- No new dependency, database migration or account setting was introduced. Selection uses the existing local UI preferences and does not restart the activity when colors change.

Command on this Java 25 host: `gradlew.bat :app:assembleDebug :app:testDebugUnitTest -I build/robolectric-java25.init.gradle --console=plain`. Set `THEME_PREVIEWS` to regenerate the theme gallery renders from `ThemeTest`.

Physical-device and OEM system-bar behavior remain unverified. Android lint was not repeated because the installed analyzer already failed on the Java 25 runtime during the redesign. This update has not been pushed or released.
