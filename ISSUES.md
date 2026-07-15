# Known Issues & Backlog

---

## Bug

### GPS Joystick import collapses multiple routes/favorites into generic single entries

**Source**: User feedback ahead of 0.14.0, submitted via app testing during Pokémon GO Fest period.

**Report** (verbatim): "Now I tried to load my old GJ settings with many named favorites and named routes, but I received only one big route (route1) with lot of waypoints and about 20 favorites (favorite1 - favorite20). I assume I have to export all individual routes from GJ one by one into LJ."

**Reply given**: "Oh that's on me, I thought I had fixed it in 0.13.0 but maybe there's different format the app doesn't support yet. Would you mind sharing your file by any chance? Just so I can try to support it in the app."

**Status**: User shared their real GPS Joystick `.db` export (Realm/TightDB binary format). File was downloaded, verified (T-DB magic present at byte offset 16, matching the format documented in `scripts/anonymize-gpsjoystick-fixtures.py`), anonymized via that script (seed 42), and committed as a new test fixture: `feature/settings/impl/src/test/resources/gpsjoystick_user_feedback.db`. The raw (non-anonymized) file was deleted after processing — never committed, per the script's design ("originals not committed").

**Symptom breakdown**:
- Multiple distinct named routes in the source GJ data → collapse into a single route named `route1` containing all waypoints from every source route concatenated together.
- Multiple distinct named favorites → collapse into `favorite1`..`favorite20` — names not preserved, only a generic counter-based name, though count (20) is preserved correctly.

**Root cause — CONFIRMED** by running `GpsJoystickMigrator.parse()` directly against the user's anonymized fixture (`gpsjoystick_user_feedback.db`, via a temporary diagnostic test, since removed): produced exactly **17 favorites, all named `Favorite 1`..`Favorite 17`** (names lost) and **1 route named `Route 1` containing all 232 waypoints merged together** — reproducing the user's report ("about 20 favorites named favorite1-20", "one big route1 with lot of waypoints") near-exactly.

Parser: `GpsJoystickMigrator.kt` (`:feature:settings:impl/src/main/kotlin/com/locationjoystick/feature/settings/impl/GpsJoystickMigrator.kt`). It has no access to the actual Realm table/object structure — it works by scanning the flat binary for `0x0C` (double array) and `0x0D` (string array) blocks and heuristically correlating them by **element count alone**:

1. **Route-name-array matching is a heuristic count match, not structural** (`extractFavoritesAndRoutes`, lines 132–140):
   ```kotlin
   val routeNameArray =
       if (routePair != null) {
           val candidates = dataArrays.filter { it.size != favCount }
           candidates.firstOrNull { routeTotal % it.size == 0 } ?: candidates.lastOrNull()
       } else null
   ```
   For the user's file, no string array in the binary satisfied this — `dataArrays` either contained none with a distinct count from `favCount`, or the true per-route name array's count didn't relate to `routeTotal` in a way the heuristic could find. Result: `routeNameArray = null`.

2. **`buildRoutes` collapses to a single route whenever no name array is resolved** (lines 170–202, specifically line 178):
   ```kotlin
   val routeCount = if (nameArray != null && totalWaypoints >= nameArray.size) nameArray.size else 1
   val base = totalWaypoints / routeCount
   ```
   With `nameArray = null`, `routeCount` falls back to `1` — **all 232 waypoints across every distinct source route get merged into one `Route 1`**. Even in the case where a name array *is* found, splitting is done by `base = totalWaypoints / routeCount` — i.e. **equal-size chunks by index**, not by actual per-route waypoint boundaries. This is only correct by coincidence if every source route happens to have the same waypoint count; GJ routes are user-recorded and essentially never uniform length. This exact fallback shape (`routeCount = 1`) is already implicitly documented as expected behavior in the existing test suite — see `GpsJoystickMigratorTest.kt` fixtures `20251008` / `20251008-1` ("4 default-named favs and **1 fallback route** with 7 waypoints"), i.e. this is a known, reproducible failure mode already present in test fixtures, not a regression — the "fixed in 0.13.0" belief was incorrect, or fixed only a narrower case.

3. **Favorite names lost the same way** — `favNameArray = dataArrays.firstOrNull { it.size == favCount && favCount > 0 }` (line 130) requires an exact count match (17) among the scanned string arrays; none matched for this file, so every favorite falls back to `"Favorite ${i + 1}"` (line 159).

4. **Secondary latent bug spotted while reading the parser** (not yet confirmed against real data): `findDataStringArrays` (lines 212–230) discards an *entire* string array if **any single entry** happens to equal a `SCHEMA_NAMES` value (`"id"`, `"name"`, `"type"`, `"address"`, etc. — line 220: `strings.none { it in SCHEMA_NAMES }`). A user who named one favorite/route exactly `"Name"`, `"Type"`, `"Address"`, etc. would silently lose **every name in that entire array**, not just the offending one. Worth a defensive test once the primary bug is fixed.

**Why this is structurally hard to fix without more format knowledge**: the parser has no way to see Realm's actual table/list-of-lists boundaries (which `CoordinateData` rows belong to which `RouteData` row) — it only sees flat arrays of doubles and strings, and guesses correlation by matching counts. A robust fix likely needs to locate the actual per-route waypoint-count/link array in the Realm binary (Realm stores list properties as arrays of row-group references, not raw counts) rather than inferring boundaries from `totalWaypoints / routeCount` division. This may require deeper reverse-engineering of the GJ Realm schema (linklist/subtable arrays) beyond the current flat double/string scanning strategy.

**Next steps**:
1. ~~Add `gpsjoystick_user_feedback.db` as a new fixture case in `GpsJoystickMigratorTest.kt`, initially asserting the *current* (buggy) output (17 favorites default-named, 1 route/232 waypoints) so the regression is pinned, then update expectations once fixed.~~ **Done** — `testGpsJoystickImportUserFeedback` pins this.
2. **Investigated, not fixable via this parser's approach**: the file's underlying Realm/TightDB commit history was never compacted — the coordinate table is physically duplicated across ~1400 historical versions (13 MB on disk vs 12-16 KB for every other fixture). More importantly, unlike every other fixture, **none** of the string arrays recoverable via the flat 0x0D scan correspond to actual favorite/route names — every string array present in the file is a schema/column-name table (`pk_table`/`pk_property`, `latitude`/`longitude`/`altitude`, `id`/`name`/`typeId`/…). The real per-row names, and the real per-route waypoint boundaries (which live in a Realm linklist/subtable structure, not a flat array this parser recognizes), are not reachable through flat double/string array scanning at all for this file. Recovering them would require a real Realm-Core B+tree/linklist parser — out of scope for a heuristic flat-array scanner. Test remains pinned to the current (degraded) output.
3. ~~Fix the `SCHEMA_NAMES` collision bug (item 4 above) defensively — e.g. only skip an array as "schema" if it exactly equals the known column-name set, not if it merely contains one matching string.~~ **Done** — `findDataStringArrays` now only treats an array as schema metadata if *every* entry is a known column name (`strings.all { it in SCHEMA_NAMES }`), not if just one entry collides. Covered by a new test.
4. Re-run `scripts/anonymize-gpsjoystick-fixtures.py` if additional real-world fixtures are obtained, to keep test coverage broad across GJ export variants.
5. Reply to the user: explain that their specific export's names aren't recoverable by the current parser because the file itself doesn't contain them in a structurally-recognizable form (likely due to how their Realm file accumulated uncompacted history) — a real fix requires a proper Realm B+tree parser, not a quick patch. Not yet done, still open.

---

## Documentation Outdated Items

---

## Frontend UI/UX

### Add "Reset all data" button (clear favorites/routes/settings without OS-level app data clear)

**Source**: User feedback ahead of 0.14.0.

**Report** (verbatim): "How can I reset all my favorites and routes and start over again? okay, I can clear the data&cache of the app."

**Reply given**: "That's a good point, I should add a button for that, will do!"

**Problem**: Currently the only way to fully reset app state (favorites, routes, settings) is via Android's system-level "Clear data & cache" for the app, which is non-obvious, destroys onboarding-completion state too (forces re-onboarding), and isn't discoverable from within the app.

**Proposed solution**: Add a "Reset all data" action in Settings (likely near Export/Import, since it's the same category of bulk data operation). Should:
- Show a confirmation dialog (destructive, irreversible) before proceeding — consistent with how Import already confirms "replace all data?" (see `docs/features/export-import.md`).
- Clear Room (routes, favorites) + DataStore (settings) — same clear step already used internally by the Import flow, just without the "insert new data" step afterward.
- Likely should NOT reset `ONBOARDING_COMPLETE` (user doesn't need to redo permission grants, just their data) — needs a product decision on scope.

**Files likely affected**: `:feature:settings:impl/SettingsScreen.kt`, `:feature:settings:impl/SettingsViewModel.kt`, `:core:data/SettingsRepository.kt` (reuse existing clear-data logic from the import flow if factored out separately).

**Docs to update per AGENTS.md policy**: `docs/features/export-import.md` (if reset lives adjacent to import/export) or a new feature doc if it's substantial enough; README/wiki feature tables if user-visible enough to warrant a wiki page (`docs/wiki/settings.html`).

---

### Increase speed profiles from 3 to 5 (add "slow walk" and "drive")

**Source**: User feedback ahead of 0.14.0.

**Report** (verbatim): "Can you increase the number of 'speed profiles' to 5? besides walk / run / bike, I love to have 'slow walk' and 'drive'."

**Reply given**: "Sure"

**Current state**: Three fixed presets — Walk, Run, Bike — defined via `AppConstants.ProfileConstants.WALK_SPEED_MPS` / `RUN_SPEED_MPS` / `BIKE_SPEED_MPS` (see `docs/features/speed-profiles.md`). All are user-editable but the set of profiles itself is fixed at 3, not a dynamic list.

**Proposed solution**: Add two more presets — "Slow Walk" and "Drive" — with new constants (e.g. `SLOW_WALK_SPEED_MPS`, `DRIVE_SPEED_MPS`) in `AppConstants.ProfileConstants`. Needs to slot into:
- Widget + Settings screen speed profile selector (currently likely a 3-way chip/segmented button per `docs/features/speed-profiles.md` — "UI: three chips or segmented button" — will need to become a scrollable/wrapping selector or dropdown to fit 5 without cramping).
- `SpeedProfile` domain model (`:core:model`) — already generic (`id`, `name`, `speedMetersPerSecond`), so likely just needs new default entries, not a schema change.
- Anti-cheat warning threshold (`ANTI_CHEAT_WARNING_THRESHOLD_MS`) — "Drive" preset will likely exceed this by design; confirm warning copy still reads sensibly for a profile explicitly named "Drive".
- Export/import (`ExportData.speedProfiles`) — should round-trip automatically since it's a list, but verify existing exports from before this change still import cleanly (list will just be shorter — no migration needed, same pattern as other additive fields).

**Docs to update per AGENTS.md policy**: `docs/features/speed-profiles.md` (preset table), `docs/domain-models.md` (no field change expected, but verify), README feature table if speed profile count is user-visible there, `docs/wiki/settings.html` / relevant wiki page.

---

### Light mode / theme alternative for accessibility (contrast + text size)

**Source**: User feedback ahead of 0.14.0.

**Report** (verbatim): "How do I change that dark mode (color orange on black) and get larger letters?"

**Reply given**: "You can't for now but I wanted to introduce a light mode alternative for sunny days, would that be enough?" — framed as a question to the user, not yet confirmed as accepted scope. Two distinct asks bundled in the original report:
1. Theme/contrast (dark mode orange-on-black is hard to read for this user) → light mode addresses this.
2. Larger text/font size → **not addressed** by light mode alone; still an open ask if the user confirms they need it separately.

**Proposed solution** (theme part): Introduce a light theme variant alongside the current dark theme in `:core:designsystem` (tokens, theme, typography). Needs a user-facing toggle (Settings) to switch, likely persisted via DataStore (new `AppSettings` field or its own key, following the existing pattern — see other boolean toggles in `docs/domain-models.md`).

**Open question**: font/text scaling — not covered by a light mode. Would need separate investigation (Compose `LocalDensity`/font scale overrides, or respecting system font size settings) if the user confirms it's still needed after a light mode ships.

**Docs to update per AGENTS.md policy**: `docs/architecture.md` if theming architecture changes meaningfully, `docs/wiki/settings.html`, README if theme choice becomes a headline feature.

---

### Favorites: surface hot-location area categories + add search/filter

**Source**: User feedback ahead of 0.14.0.

**Report** (verbatim): "In the settings you offer hot locations/routes (superb). They have hierarchical area categories (American Samoa / Pago Pago), very good. Unfortunately this area categories are not shown in the 'favorites menu'. Having a lot of favorites, categories will help or at least offer a 'search' function."

**Reply given**: "Good point as well!!"

**Current state**: Hot locations are a flat `List<Triple<String, Double, Double>>` (name, lat, lon) in `FavoriteRepository.HOT_LOCATIONS` (`docs/features/favorites.md`) — the "hierarchical area categories" the user sees in Settings' hot-locations picker apparently exist only as presentation-layer grouping in that picker UI, not as a persisted field on `FavoriteEntity`/`FavoriteLocation`. Once a hot location is added as a favorite, that grouping info is lost — favorites list has no category concept at all currently (`docs/domain-models.md`: `FavoriteLocation` = `id`, `name`, `position`, `createdAt` only).

**Proposed solution** — two independent asks, either would help, both together is ideal:
1. **Category field**: add an optional category/area field to `FavoriteLocation`/`FavoriteEntity` (nullable — most user-added favorites won't have one unless imported from hot locations or explicitly tagged), surfaced as a group header or filter chip in the Favorites list.
2. **Search**: add a simple text-filter/search bar to the Favorites screen, filtering by name (independent of the category work, likely faster to ship).

**Files likely affected**: `:core:database` (`FavoriteEntity` schema — migration needed if adding a column), `:core:model` (`FavoriteLocation`), `:core:data/FavoriteRepository.kt`, `:feature:favorites:impl/FavoritesScreen.kt` / `FavoritesViewModel.kt`.

**Docs to update per AGENTS.md policy**: `docs/domain-models.md` (new field on `FavoriteLocation`), `docs/features/favorites.md`, `docs/features/export-import.md` (if new field needs export/import handling — likely does, per `ExportData` scope), README/wiki if search/categories become a headline favorites feature.

---

## Wiki

---

