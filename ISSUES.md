# Known Issues & Backlog

> Session 2026-06-04: 4 bugs fixed (speed-profile-cap, add-next-point-roads, traced-points-clear, anticheat-cooldown). 4 tasks failed in agent queue (roaming-return-to-start, isactivitypaused, floatingwidgetservice-toconfig, apppreferences-pref). See .shortcuts/ISSUES_STEPS.json for details.

## Documentation Outdated Items

No outstanding documentation issues.

---

## Bugs

- When we generate a "roaming" road with "return to start", we walk to the given "distance", we should create a loop of that distance in the given radius, the idea is to walk to half that distance, then come back

---

## Technical Debt (pre-1.1)

### [MEDIUM] `getElevationTiltJitterDegrees()` / `getElevationNoiseAmplitudeMs2()` missed `pref()` cleanup

**File:** `AppPreferencesDataSource.kt:549–589`

The `pref(key, default)` helper was extracted in commit `54201a5` to eliminate hand-rolled `dataStore.data.catch { }.map { }` chains. These two methods still use the old pattern — they were missed. They're 10 lines each where `pref()` would make them one line each.

**Fix:**
```kotlin
override fun getElevationTiltJitterDegrees(): Flow<Float> =
    pref(Keys.ELEVATION_TILT_JITTER_DEGREES, DEFAULT_ELEVATION_TILT_JITTER_DEGREES)

override fun getElevationNoiseAmplitudeMs2(): Flow<Float> =
    pref(Keys.ELEVATION_NOISE_AMPLITUDE_MS2, DEFAULT_ELEVATION_NOISE_AMPLITUDE_MS2)
```

---

### [RESOLVED] `FloatingWidgetService.startRoamingWith()` now uses `RoamingDefaults.toConfig()`

**Status:** Completed — implementation already correct at line 576.

The method correctly constructs `RoamingConfig` via `defaults.toConfig(pos)`, matching the canonical pattern used in `MapViewModel.startRoamingFromDraft()`. This ensures any future fields added to `RoamingConfig` (e.g., `previewWaypoints`) are automatically included without manual field-by-field construction.

---

### [RESOLVED] `isActivityPaused` mode-dispatch refactored to repository layer

**Status:** Completed in commit `a6f6bb0`

The triple-condition business logic was moved from the UI layer to the repository layer:

- **`LocationRepository.isCurrentActivityPaused`**: Combines `currentMode`, `mockLocationState`, and `isWalkPaused` for walk-to and route-replay cases.
- **`ActivityStateRepository.isActivityPaused`**: Extends `LocationRepository`'s logic to include roaming pause state via `combine()`.
- **`FloatingWidgetService.kt:256`**: Now uses `activityStateRepository.isActivityPaused` directly.

This centralizes mode-dispatch logic so it's no longer reimplemented at call sites.

---

### [LOW] Deep link uses `StateFlow + filterNotNull` — subtle redelivery risk on resubscription

**File:** `MapViewModel.kt:623–632`

```kotlin
deepLinkRepository.pendingCoords
    .filterNotNull()
    .collect { coords ->
        _uiState.update { it.copy(pendingTapPosition = coords) }
        deepLinkRepository.consume()
    }
```

`StateFlow` replays its last value on new subscription. Between `_uiState.update` and `deepLinkRepository.consume()`, the flow still holds the non-null coord. If the coroutine is cancelled and restarted before `consume()` completes (e.g. process death, ViewModel recreation in tests), the coord is re-applied. A `SharedFlow(replay=0)` in `DeepLinkRepository` is the correct primitive for one-shot events.

---

### [LOW] Hot location IDs are name-derived — stale entries accumulate if names change across versions

**File:** `FavoriteRepository.kt:86`

```kotlin
val id = HOT_ID_PREFIX + name.lowercase().replace(Regex("[^a-z0-9]"), "_")
```

The ID is generated from the name at insert time. If a hot location name is corrected in a future version (e.g. `"Osaka Tokyo"` → `"Osaka"`), `removeHotLocations()` will delete the new entry (`hot_osaka`) but the old entry (`hot_osaka_tokyo`) has a non-`hot_` ID only if the user had renamed it — but if it was inserted as `hot_osaka_tokyo` and the user never touched it, it stays. The upsert matches by name, not by ID, so it inserts a new `hot_osaka` and the old `hot_osaka_tokyo` is orphaned until the user deletes it manually.

Minor risk given the list is stable, but worth documenting.

---

## Frontend UI/UX


---

## Wiki

No outstanding wiki items.
