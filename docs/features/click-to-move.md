# Click-to-Move / Teleport

Long-press map → bottom sheet with "Walk here" or "Teleport here".

Key files: `:feature:map:impl/MapViewModel.kt`, `:core:location/EphemeralReplayController.kt`, `:core:data/WalkCoordinator.kt`

## Walk Here

- Bearing computed from current position to target.
- Advances at `currentSpeed` m/s per tick.
- Snaps to target when within `AppConstants.LocationConstants.WALK_ARRIVAL_THRESHOLD_METERS`.

## Teleport

- Sets position directly.
- Pushes one update.

## Walk via Roads

- Long-press map → bottom sheet → "Walk via roads".
- Fetches OSRM route from current position to target; walks it segment by segment.
- On OSRM failure, falls back to straight-line walk silently.

## Add Next Point (Ephemeral Replay)

While a walk-here is active, the user can tap "Add next point" on the map to chain waypoints without saving a route.

Managed by `EphemeralReplayController` (`@Singleton`, `:core:location`), injected by both `MapViewModel` and `FloatingWidgetService`:

- **First tap** (walk active): cancels the walk via `WalkCoordinator`, builds a 3-point list (walkStart → walkTarget → newPoint), starts `RouteReplayEngine` in ephemeral mode.
  - If the current walk was "via roads", the `walkTarget → newPoint` leg is resolved via OSRM (`followRoads = true`).
- **Subsequent taps** (already in `ROUTE_REPLAY`): appends the new point to the live route.
- **No active walk**: no-op.

This eliminates duplicated state-machine logic that previously existed in both `MapViewModel` and `FloatingWidgetService`.

## Edge Cases

- New walk-here cancels the previous one.
- Walk-here while route replay is active → show confirmation dialog to stop replay before proceeding.
- "Add next point" while in roaming mode → no-op (only valid during walk-to or active ephemeral replay).
