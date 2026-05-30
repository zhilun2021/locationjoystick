# Known Issues & Backlog

## Documentation Outdated Items

No outstanding documentation issues.

---

## Backlog

### UI/UX issues

---

## Regressions Found — Pre-Release Smoke Test Audit (2026-05-30)

### CRITICAL — Smoke suite completely broken (all tests fail)

**Root cause:** `@HiltAndroidTest` placed only on abstract `BaseSmokeTest`. Java annotations without `@Inherited` are not visible on subclasses via `getClass().isAnnotationPresent()`. Hilt's `HiltAndroidRule` checks the concrete class at runtime → `IllegalStateException` on every test.

**Fix applied:** Added `@HiltAndroidTest` directly to all 8 concrete smoke test classes (IdleSmokeTest, MapSmokeTest, FavoritesSmokeTest, RoutesSmokeTest, RouteCreatorSmokeTest, RouteDetailSmokeTest, SettingsSmokeTest, AboutSmokeTest).

---

### Regressions in existing smoke tests (UI text/CD mismatch)

| Test | Symptom | Fix applied |
|------|---------|-------------|
| `MapSmokeTest.map_recenter_fab_is_displayed` | CD `"Re-center"` ≠ actual `"Re-center on location"`. Also: FAB hidden when `isFollowingCamera=true` (default). | Replaced with 5 tests for always-visible FABs. |
| `SettingsSmokeTest.export_button_no_crash` | `onNodeWithText("Export")` — Export is an icon button (no text node). | Changed to `onNodeWithContentDescription("Export")`. |
| `SettingsSmokeTest.settings_shows_data_management_section` | `"Data Management"` section removed; export/import refactored to toolbar icon buttons. | Replaced with `settings_export_icon_is_displayed`. |
| `SettingsSmokeTest.settings_shows_import_button` | `"Import Settings"` text no longer exists in UI. | Replaced with `settings_import_icon_is_displayed`. |

---

### Missing smoke test coverage added

| New test | Where |
|----------|-------|
| Map FABs always visible: start simulation, open favorites, open routes, start roaming, search | MapSmokeTest |
| Settings export dropdown: "Export via QR code", "Export settings" | SettingsSmokeTest |
| Settings import dropdown: "Import from QR code", "Import from file", "Import from GPS Joystick", "Import from YAMLA" | SettingsSmokeTest |
| MapPicker back navigation returns to Favorites | FavoritesSmokeTest |
| "from coordinates" bottom sheet opens with Latitude/Longitude fields | FavoritesSmokeTest |

---

### Additional regressions found (stale content descriptions throughout)

| Test | Symptom | Fix applied |
|------|---------|-------------|
| `RoutesSmokeTest.replay_mode_dropdown_shows_all_options` | Checked for `"Walk route"` and `"Loop in reverse"` — these texts don't exist. Dialog now shows `"Walk and start"`, `"Teleport and start"`, `"Reverse"` checkboxes. Renamed to `start_route_dialog_shows_all_options`. | Updated assertions to match actual dialog content. |
| `RoutesSmokeTest.route_overflow_menu_shows_edit_export_delete` | Used `"More options"` CD — Routes overflow icon has `contentDescription = "Menu"`. | Changed to `"Menu"`. |
| `RouteCreatorSmokeTest` (all tests) | `@Before` clicked `"New route"` CD — doesn't exist. Routes add button has `contentDescription = "Add route"` and opens a dropdown (items: `"from map"`, `"from map follow roads"`, `"from GPX file"`). | Fixed @Before to click `"Add route"` then `"from map"`. |
| `RouteCreatorSmokeTest.route_creator_screen_loads` | Checked `onNodeWithText("Save Route")` — Save dialog only appears after clicking Save with ≥2 waypoints. Scaffold title is `"Create Route"`. | Changed to assert `"Create Route"`. |
| `RouteCreatorSmokeTest.route_creator_shows_save_route_button` | Save FAB only visible with ≥2 waypoints — untestable in smoke test without map interaction. | Replaced with `route_creator_shows_favorites_button` checking `"Pick from favorites"` CD (always visible). |
| `FavoritesSmokeTest` (all tests) | Used `"Add options"` CD — Favorites add button has `contentDescription = "Add favorite"`. | Changed to `"Add favorite"` throughout. |
| `RouteDetailSmokeTest` @Before | Used `"More options"` CD for Routes overflow — actual CD is `"Menu"`. | Changed to `"Menu"`. |
| `MainActivityIntentTest` | `intent_navigate_to_map`: checked `onNodeWithText("Map")` — MapScreen title is `"Lj"`. `intent_navigate_to_route_creator`: checked `onNodeWithText("New route")` — doesn't exist. | Fixed to check `"Start location simulation"` CD and `"Save Route"` dialog text. |

---

### Systemic fix: ambiguous text nodes (drawer + screen share labels)

**Root cause:** `ModalNavigationDrawer` always includes drawer items in the Compose semantics tree — even when closed. Every navigation label ("Map", "Routes", "Favorites", "Settings", "About") exists in both the drawer AND the current screen, causing `onNodeWithText()` to fail with "expected 1 but found 2 nodes."

**Fixes applied:**
- Added `semantics { testTag = "nav_drawer" }` to `ModalDrawerSheet` in `LjDrawerContent.kt`
- Updated `navigateFromIdle()` and `navigateViaDrawer()` in `SmokeTestHelpers.kt` to use `filterToOne(hasAnyAncestor(hasTestTag("nav_drawer")))` for disambiguation
- Updated all `*_screen_loads` assertions and `navigate_to_*` post-nav assertions to use screen-unique identifiers (FAB content descriptions, section headers) instead of destination-name text that collides with drawer items
- Updated `drawer_opens_and_shows_items` to use testTag-based drawer item filtering

---

### Production crash: MapView created before MapLibre.getInstance() in MapPicker + RouteCreator

**Severity:** Crash on first-launch if user navigates to MapPicker (Favorites → Add → from map) or RouteCreator before ever opening the Map screen.

**Root cause:** `MapPickerScreen.kt` and `RouteCreatorScreen.kt` use `val mapView = remember { MapView(context) }` without calling `MapLibre.getInstance(context)` first. `MapScreen.kt` correctly calls `MapLibre.getInstance(context)` inside the `remember` block before `MapView(context)`. In production, this has been masked because the user typically visits the Map screen first, initializing MapLibre globally. Smoke tests that skip Map screen expose it directly.

**Fix applied:** Added `MapLibre.getInstance(context)` before `MapView(context)` in both `MapPickerScreen.kt` and `RouteCreatorScreen.kt`, matching the pattern in `MapScreen.kt`.

---

### Documentation gaps fixed

| File | Gap |
|------|-----|
| `docs/constants.md` | Missing: `CooldownConstants`, `UnitConversionConstants`, `MapColorConstants`, `AnimationConstants`, `TimeConstants`. `ExportConstants` missing GPX fields. |
| `docs/features/export-import.md` | Missing: GPX import, GPS Joystick import, YAMLA import flows. |
| `docs/features/routes.md` | Missing: GPX import mention. |
| `docs/features/roaming.md` | Key file listed as `RoamingSheet.kt` — actual file is `MapBottomSheets.kt`. |
| `docs/architecture.md` | Missing `:core:map` module (GeoJSON utils, MapLibre lifecycle bridge, style extensions). |
| `README.md` | Routes feature missing GPX import. Import/Export feature missing GPS Joystick and YAMLA. Modules table missing `:core:map`. |
