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

