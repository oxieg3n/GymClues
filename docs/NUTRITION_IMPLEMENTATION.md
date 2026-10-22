# GymClues Nutrition

Nutrition uses the existing AppContainer, Room database, Kotlin coroutines/Flow, navigation graph, Material 3 theme and house controls. It adds no dependencies, billing, analytics, paid AI service or parallel application framework.

## Available functionality

- Home summary and Menu → Nutrition, preserving the shared header and music controls.
- Date-based diary with Breakfast, Lunch, Dinner and Snacks, calories/macros, progress and remaining calories, editable quantities/categories, repeat and delete.
- Custom foods, optional label micronutrients (fiber, sugar and sodium), reusable favorites, recent/frequent foods and quick manual calories/macros.
- Local search while typing, explicit external search, cached food details, provider refresh and source attribution.
- UPC-A/UPC-E/EAN-8/EAN-13 camera scanning using the existing ZXing dependency, typed barcodes and Create food from barcode fallback.
- Separate saved meals and recipes. Ingredients retain their nutrition snapshots; recipes calculate totals and per-serving nutrition. Copy yesterday's meal or any previous meal/day.
- Effective-date calorie and macro goals, optional workout/rest targets, kg/lb and g/oz preferences and daily water goals. Targets are manual, never automatically overwritten.
- Body weight with historical editing/deletion, weight graphs and weekly/monthly averages; water in ml or US fluid ounces with editing/deletion.
- 7/30/90-day history with daily totals, adherence, protein consistency, workout frequency, completed lifting volume and training/rest comparisons. Unlogged days and missing nutrients are excluded from averages, and charts distinguish gaps from logged zeroes.
- Delete all nutrition data for the current local owner without deleting workouts.

Ordinary food logging, goals, recipes, history, water and weight work offline. New external searches require connectivity. Food and nutrition are not paywalled.

## Code and data flow

| Location | Responsibility |
| --- | --- |
| `data/nutrition/NutritionModels.kt` | Entities, immutable food/recipe snapshots, Room converters, quantities and unit calculations |
| `data/nutrition/NutritionDao.kt` | Owner-scoped reactive queries and separate provider-cache database |
| `data/nutrition/NutritionMigration.kt` | Additive migration from Room version 6 to 7 |
| `data/nutrition/NutritionRepository.kt` | Validation, provider caching, diary CRUD, transactional meal/recipe operations, goals and schedule integration |
| `data/nutrition/FoodProviders.kt` | Replaceable FoodProvider interface, HTTPS transport, Open Food Facts and optional USDA proxy |
| `data/nutrition/NutritionInsights.kt` | Logged-day averages, adherence and weight trends |
| `ui/nutrition/` | Navigation, dashboard, search/details/editor, saved foods, goals, history, weight/water and shared nutrition controls |

The new UI files are `NutritionNavigation.kt`, `NutritionDashboard.kt`, `FoodScreens.kt`, `SavedFoodScreens.kt`, `NutritionGoalsScreen.kt`, `NutritionHistoryScreen.kt`, `NutritionHealthScreen.kt` and `NutritionUi.kt`. The new test files are `NutritionCalculationTest.kt`, `NutritionDatabaseTest.kt`, `NutritionProviderTest.kt` and `NutritionUiTest.kt`. Existing integration edits are limited to `Db.kt`, `Repositories.kt`, `App.kt`, `AppHeader.kt`, `Screens.kt` and README. Gradle dependencies and existing workout implementations are unchanged.

AppContainer owns NutritionRepository. UI collects Room flows and launches suspend operations without blocking the main thread. FoodProvider exposes `searchFoods`, `getFoodDetails` and `lookupBarcode`; injectable transport and clock keep tests deterministic.

## Storage, migration and calculations

The production database advances **6 → 7** using registered `MIGRATION_6_7`. It creates `nutrition_foods`, `nutrition_diary`, `nutrition_bundles`, `nutrition_bundle_items`, `nutrition_goals`, `nutrition_weights` and `nutrition_water`, with owner/date indexes and a cascading bundle-item foreign key. Existing tables are not dropped or rewritten. Room schemas for GymDb version 7 and FoodCacheDb version 1 are committed. There is no destructive fallback.

Custom foods live in GymDb. Open Food Facts and USDA cache records live in separate private Room files (`nutrition-openfoodfacts.db`, `nutrition-usda.db`) so their catalogs are not blindly combined into a redistributable database. The UI federates the records; each preserves its provider ID, URL and license. Diary and ingredient snapshots intentionally retain the facts the user actually selected, including ingredient provenance for recipes. Deleting/editing a source food or recipe does not rewrite history.

Food nutrients describe a positive `basisQuantity` of `basisUnit`. A selected portion carries a known canonical amount; logged totals are `nutrients × quantity × canonicalPerUnit / basisQuantity`. Only known gram/ounce, milliliter/US-fluid-ounce or explicitly supplied portions are offered. Cups, tablespoons and pieces are not assigned invented densities. Calories use the provider/label value, with kJ converted only when needed; they are not reconstructed from macros. Internal doubles retain calculation precision, with decimal multiplication and display-only rounding. A missing nutrient remains null and makes that nutrient's aggregate incomplete rather than silently zero.

Diary dates are ISO local calendar dates, separate from UTC epoch-millisecond timestamps. IDs are UUIDs; owner, update timestamps, deletion flags and LOCAL sync state prepare records for later synchronization. Entry forms retain an operation UUID, multi-item actions derive stable child IDs, and Room insert-ignore plus transactions make retries idempotent. UI actions also disable busy controls and debounce rapid quick-action taps.

Goals are versioned by owner/effective date. The applicable goal is the latest version on or before the diary date. Training days use the existing program assignment/rest-day calendar or an owner's completed workout. Rest-day targets require an explicit toggle. Strength workouts do not create calorie-burn estimates or change intake totals. Weight is stored canonically in kg, water in ml.

## Providers and licensing

### Open Food Facts (enabled by default)

- Product lookup uses the documented **v3 product API**. Current v3 does not expose text search, so full-text search uses the documented `/cgi/search.pl` endpoint, bounded to 20 results. Local search is debounced 300 ms; external searches require a button press because OFF prohibits search-as-you-type usage of its search endpoint.
- The app identifies itself as `GymClues/<app version> (https://github.com/oxieg3n/GymClues/issues)`.
- Requests are serialized, search requests spaced by at least 6.1 seconds, product reads by 4.1 seconds, and HTTP 429 honors Retry-After. These per-installation limits cannot coordinate unrelated clients sharing a public IP.
- HTTPS has connection/read timeouts, bounded responses, no automatic redirects, cancellation and explicit failure handling. Offline and malformed responses never produce invented foods or erase local records.
- Only usable named records with reported nutrition are cached. Estimated nutrient fields are ignored. Source-provided missing values remain unknown. Products are contributed data and can be inaccurate; users should compare them with the label.
- Data: [ODbL 1.0](https://opendatacommons.org/licenses/odbl/1-0/) and [DbCL 1.0](https://opendatacommons.org/licenses/dbcl/1-0/). Attribution remains visible in search, food details and ingredient information. No product images are fetched; OFF image licensing is separate.
- Keeping cache files separate is a provenance measure, not a waiver of ODbL obligations. A future public export, shared catalog, derived database or food-data redistribution must receive a licensing review and preserve applicable attribution/share-alike and access obligations. Personal diaries are private and no combined catalog is shipped.

References: [current API documentation](https://openfoodfacts.github.io/openfoodfacts-server/api/), [API tutorial and rate limits](https://openfoodfacts.github.io/openfoodfacts-server/api/tutorial-off-api/), [licensing guidance](https://openfoodfacts.github.io/openfoodfacts-server/api/tutorials/license-be-on-the-legal-side/).

### USDA FoodData Central (optional operator configuration)

USDA requires a data.gov API key. GymClues deliberately does not bundle one or use a shared demo key. An operator must deploy a secure HTTPS proxy and enter its base URL under Nutrition goals → Food data sources. Blank disables USDA while OFF remains available. The app rejects HTTP, embedded credentials, query strings and fragments in the base URL.

The proxy contract forwards these **documented FDC paths** and JSON responses:

```text
GET <configured-base>/foods/search?query=<urlencoded description>&pageSize=20
GET <configured-base>/food/<fdcId>
```

The server adds its private key when forwarding to `https://api.nal.usda.gov/fdc/v1`. It should validate inputs, allowlist those upstream paths, enforce aggregate rate limits, and avoid logging sensitive query contents. Authentication/abuse protection is an operator responsibility; no functioning backend or endpoint is invented in this repository. The app spaces USDA requests by 3.7 seconds and honors 429 cooldowns; server-wide quotas still require server enforcement.

The parser supports the FDC search/detail nutrient shapes, label calories, explicit nutrient units and documented gram-weight portions. General food coverage improves when this proxy is configured; OFF alone primarily covers packaged products and does not guarantee a match for every generic food description. Manual food entry always remains available.

USDA data is public domain/CC0. Attribution is retained. [USDA API guide, key requirements, rate limits and licensing](https://fdc.nal.usda.gov/api-guide/).

## Scanner and privacy

The installed ZXing scanner already supports required food barcode formats, so no ML Kit dependency or Google Play services download was introduced. Camera permission is requested by the scanner when launched. Back cancels; camera failure, unknown products, timeouts and offline lookups leave typed-barcode, search and manual entry available. Nutrition navigation is outside the scan activity and can always be left. A food is saved to the diary only after review and a separate Add action.

Nutrition data is owner-scoped and app-private. Nothing is sent to community sharing or the sync outbox. Only user-requested search text/barcodes/provider IDs go to food providers; diary history, weight and water do not. No nutrition analytics SDK was added. Existing Android application backup/device-transfer policy is unchanged; production privacy policy and backup decisions should explicitly cover these new health records.

## Future integrations (not implemented)

AI photo, text and voice logging are intentionally absent. FoodSnapshot, RecipeComponent and FoodProvider permit future reviewed proposals to resolve real source foods and then use the same validation/snapshot logging path. A future provider must keep credentials server-side and obtain permission before uploads. There are no fake AI buttons or required AI services.

Health Connect import/export and cloud nutrition sync are not implemented. A future integration needs explicit opt-in, granular permissions, stable external record IDs and deduplication before importing/exporting. LOCAL metadata is preparation, not a claim that synchronization is operational. No estimated calorie recommendation or automatic exercise-calorie/net-calorie feature was added; manual goals are fully functional.

## Validation

Run the existing Gradle commands:

```powershell
./gradlew.bat :app:assembleDebug :app:testDebugUnitTest
# Nutrition-only:
./gradlew.bat :app:testDebugUnitTest --tests 'com.gymclues.app.Nutrition*'
```

Use the project's compatible JDK/Android SDK. On this workstation Android Studio supplies Java 25; Robolectric 4.13's older ASM needs a local Gradle init-script override to ASM 9.8 (`-I build/robolectric-java25.init.gradle`). That workstation-only script is not an application dependency. JDK 17 avoids that Java-25-specific workaround.

Tests cover quantities/units, missing nutrients, invalid inputs, goals/averages, provider parsing/failures/rate limits, Room CRUD/ownership/snapshots, transaction rollback, repeat safety, persistence after database reopen, and migrations 1→7 and 6→7 preserving workout data. Native Compose tests exercise navigation, food logging/editing/deletion, barcode fallback, goals, recipes, copying, weight/water and narrow/large-font/tablet layouts. Provider tests use deterministic fake HTTP responses, not fabricated production foods.

Physical camera recognition/permission behavior and an operator's real USDA proxy require device/deployment smoke tests before publishing. Host tests cannot establish those. The implementation does not depend on them for offline/custom logging.

Native test captures use deterministic fixture foods, not a bundled production food catalog: [dashboard](nutrition/screenshots/03-nutrition-logged-390.png), [serving details](nutrition/screenshots/02-food-details-390.png), [meals and recipes](nutrition/screenshots/04-saved-recipes-390.png), [history](nutrition/screenshots/05-history-390.png), [320 dp with larger text](nutrition/screenshots/06-search-large-text-320.png), and [tablet](nutrition/screenshots/07-nutrition-tablet-768.png). Set `NUTRITION_PREVIEWS` to an output directory when running NutritionUiTest to regenerate them.

### Verification on 2026-10-22

- `:app:assembleDebug :app:testDebugUnitTest -I build/robolectric-java25.init.gradle --offline --console=plain`: **BUILD SUCCESSFUL; 105 tests, 0 failures, 0 skipped**. This includes 36 nutrition tests and all 69 existing tests. Nutrition tests comprise 8 calculation, 14 database/migration, 7 provider and 7 native UI tests.
- The full suite covers existing workout logging/history, program navigation, equipment/exercise guides, themes, shell/music controls and sharing, in addition to the new nutrition flows. Migrations validate both 1→7 and 6→7 against exported Room schemas and check existing workout/user records.
- Native captures were reviewed at phone, narrow/large-font and tablet sizes. No physical device/emulator was connected; camera recognition and real permission prompts were not exercised.
- One live OFF v3 barcode lookup returned real reported nutrition. One live CGI text search returned **HTTP 503**, so live text-search success was not verified. Deterministic search parsing, offline, timeout, malformed-response and rate-limit tests passed. Public-source availability/coverage remains a production dependency; no fallback values are fabricated.
- USDA parsing/proxy behavior is fixture-tested only. No operator proxy or secret key was available; USDA is disabled until configured.
- `:app:lintDebug` was attempted but failed during lint initialization under the installed Java 25.0.3 host, before producing app findings. Rerun it with a supported JDK before release. No JDK or dependency upgrade was bundled into this feature.
- Review of new nutrition source found no embedded credentials, sensitive logging, destructive migration, cloud upload or community-sharing path. Existing unrelated configuration/credentials were not changed.

Before release: smoke-test scanning, cancellation/denied camera permission, database upgrade/restart and offline logging on an Android device; configure/test the USDA proxy if generic-food coverage is needed; rerun lint with a compatible JDK; and confirm the privacy/backup policy covers nutrition records. Preserve provider attribution and review ODbL obligations before any public food-data redistribution. AI, Health Connect, cloud sync and automatic calorie estimates remain outside this build.
