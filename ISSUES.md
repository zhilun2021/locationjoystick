# Known Issues & Backlog

---

## Bug

---

## Documentation Outdated Items

---

## Frontend UI/UX

---

## Wiki

---

## Architecture / Consistency

### ARCH-1: ~~Navigation extras split~~ — fixed
`EXTRA_NAVIGATE_TO_ROUTE_CREATOR` moved from `MainActivity.companion` into `AppConstants.ServiceConstants` alongside the other three. `MainActivity` now references it via `AppConstants.ServiceConstants.EXTRA_NAVIGATE_TO_ROUTE_CREATOR`. Smoke test updated.

---

### ARCH-2: ~~Activity pause state split~~ — closed, no issue
Audit completed. Individual `isWalkPaused`/`isRoamingPaused` flows are legitimately needed for mode-specific UI controls ("Resume walk" vs "Resume roaming"). `ActivityStateRepository.isActivityPaused` serves a distinct purpose (generic widget pause button). No misuse found — the three are not redundant, they answer different questions.

---

### ARCH-3: ~~Duplicate enum serialization~~ — fixed
`WidgetFeature.toKey()`, `MapFabFeature.toKey()`, `String.toWidgetFeature()`, `String.toMapFabFeature()` were four identical-pattern extension functions. Replaced with one generic `inline fun <reified T : Enum<T>> String.toEnumFeature(): T?` in `AppPreferencesDataSource.kt`. Serialization now uses `name.lowercase()` inline. All 8 call sites updated across `AppPreferencesDataSource`, `SettingsRepository`, and 2 test files.

---

### ARCH-4: ~~Exception handling inconsistency~~ — closed, no issue
Audit completed. `CoroutineExceptionHandler` (for long-lived scopes where unhandled exceptions would silently kill the coroutine) and try-catch (for bounded one-shot operations) are used together and correctly in services. ViewModels use try-catch appropriately for one-shot async work. The two are complementary, not inconsistent.

---

### ARCH-5: ~~Loading state boilerplate~~ — closed, no issue
Audit completed. Loading state is managed via `stateIn(initialValue = UiState(isLoading = true))` — idiomatic Kotlin Flow, not boilerplate. The one-shot `isLoadingSegment` in `RouteCreatorViewModel` is a different pattern for a bounded async operation. No shared helper needed.

---

## ~~[Release Audit] Documentation gap — Floating Widget (two features)~~ — fixed

**Changed source files:**
- `feature/widget/impl/src/main/kotlin/.../FloatingWidgetService.kt` (eb671f9)
- `feature/widget/impl/src/main/kotlin/.../WidgetPanelContent.kt` (eb671f9)
- `feature/widget/impl/src/main/kotlin/.../MapFloatingView.kt` (6bcde30)
- `feature/widget/impl/src/main/kotlin/.../WidgetPanelPresenter.kt` (6bcde30)
- `core/location/src/main/kotlin/.../MapController.kt` (6bcde30)
- `core/location/src/main/kotlin/.../MapSharedState.kt` (6bcde30)

**Documentation gap:**
Two user-visible features shipped since v0.8.0 are not documented in `docs/features/widget.md` or the wiki:

1. **Red dot badge on FAB** (eb671f9): A red dot appears at the top-right of the widget FAB when a route, walk, or roaming session ends naturally. Tapping to open the panel clears the badge. Document under a "Completion Badge" or "FAB Badge" section — include: what triggers it, what clears it, which modes fire it (route replay, walk-to, roaming).

2. **Route state in floating map** (6bcde30): A route icon appears in the `MapFloatingView` FAB column (gated on `MapFabFeature.ROUTES` setting or active replay). Turns green when replay is active; tapping expands stop and pause/resume buttons to the left. `enabledMapFabFeatures` now flows through `MapSharedState` so the widget respects the same settings toggle as the main map. Document under a "Floating Map" section — include: route icon appearance, green active state, stop/pause controls, settings gate.

**Relevant doc file:** `docs/features/widget.md`

