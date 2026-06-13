# Roaming Mode

Set a center, radius, and distance. Walks randomly within the radius. Configured via bottom sheet on the Map screen.

Key files: `:core:routing/RoamingEngine.kt`, `:core:routing/OsrmClient.kt`, `:core:data/RoamingRepository.kt`, `:feature:map:impl/MapBottomSheets.kt`, `:feature:map:impl/MapViewModel.kt`

## Modes

- **Simple** (straight-line): no network required.
- **Road-following** (OSRM routes): opt-in. On OSRM failure, falls back to straight-line automatically.

## Algorithm

The entire route is pre-planned before walking begins. The map shows the complete wandering path upfront rather than updating per-segment.

**Waypoint count**: `numPoints = max(2, round(distanceMeters * 30 / 1000))` — scales linearly (1000 m → 30 points).

**Straight-line mode**: All waypoints generated upfront. With `returnToInitialLocation`, the second half mirrors back toward center with the center appended as the final point.

**Road-following mode (OSRM)**: Picks random points iteratively, fetches OSRM segments, accumulates road distance until budget is met (safety cap: 50 OSRM calls). With `returnToInitialLocation`, a final OSRM segment from the last point back to center is fetched. Falls back to straight-line on any OSRM failure.

**Preview = planning**: `generateRoamingPreview` runs the full planning algorithm. `startRoaming` walks the pre-planned route directly. If no preview exists at start time, planning runs inline.

## Configuration Fields (`RoamingConfig`)

| Field | Description |
|---|---|
| `centerPosition` | Center of the roaming area |
| `radiusMeters` | Radius of the random walk area |
| `distanceMeters` | Total distance to walk before stopping |
| `speedProfileId` | Selects OSRM profile (`"bike"` → cycling, else foot) |
| `useRoadSnapping` | Enables OSRM road-following |
| `returnToInitialLocation` | Walk back to center after roaming completes |

`RoamingConfig` is constructed from `RoamingDefaults` via `RoamingDefaults.toConfig(centerPosition)` — the single translation site for `followRoads → useRoadSnapping`.

## OSRM Configuration

Base URL, overview, geometries, and profile constants in `AppConstants.OsrmConstants` and `AppConstants.RoamingConstants`.

## State Management

- `RoamingRepository` owns `isRoaming` and `isRoamingPaused` `StateFlow`s.
- `RoamingEngine` owns `activeJob` and the coroutine scope. Only one session active at a time — starting a new one awaits cancellation of the previous via `cancelAndJoin` before movement begins.
- Completion (natural loop exit) fires `onComplete` callback → `RoamingRepository` resets mode and clears route waypoints.
