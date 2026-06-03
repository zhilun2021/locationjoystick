# Known Issues & Backlog

## Documentation Outdated Items

No outstanding documentation issues.

---

## Bugs

- FloatingWidgetService -> Map -> Search -> Select result -> Doesn't show the anti-cheat cooldown timeout
- "Add next point" should be "straight" line if the user only clicked "walk here", if he clicked "walk here via roads" it should be adding a point following roads
- Stopping a "walk here" road from the map screen doesn't clear the traced points
- When we generate a "roaming" road with "return to start", we walk to the given "distance", we should create a loop of that distance in the given radius, the idea is to walk to half that distance, then come back

---

## Technical Debt (pre-1.1)

### [MEDIUM] `FloatingWidgetService.startRoamingWith()` bypasses `RoamingDefaults.toConfig()`

**File:** `FloatingWidgetService.kt:581–605`

Manually constructs `RoamingConfig(centerPosition, radiusMeters, distanceMeters, ...)` field-by-field. `MapViewModel.startRoamingFromDraft()` uses the canonical `draft.toConfig(position)` extension for the same operation. If `RoamingConfig` or `RoamingDefaults.toConfig()` gains a field (e.g. `previewWaypoints`), the service path silently drops it — already happened: `MapViewModel` passes `previewWaypoints` via `copy(previewWaypoints = ...)` but the service path has no equivalent.

**Fix:** Replace the manual construction with `defaults.toConfig(pos)` + any overlay-specific `copy(...)` overrides.

---

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

### [MEDIUM] `isActivityPaused` mode-dispatch lives in the UI layer

**File:** `FloatingWidgetService.kt:255–261`

```kotlin
val isActivityPaused =
    isWalkPaused ||
        (mockMode == ROUTE_REPLAY && mockLocationState == PAUSED) ||
        (mockMode == ROAMING && isRoamingPausedWidget)
```

This is mode-dispatch business logic embedded inline in a Composable in a service. The same concept likely exists elsewhere. `LocationRepository` or a dedicated query already owns `isWalkPaused`, `currentMode`, and `mockLocationState` — it should expose a single `isCurrentActivityPaused: Flow<Boolean>` derived property so this triple-condition isn't reimplemented at each call site.

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
