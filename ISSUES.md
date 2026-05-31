# Known Issues & Backlog

## Documentation Outdated Items

No outstanding documentation issues.

---

## Bugs

- implement ~/.claude/plans/let-s-make-sure-the-concurrent-kitten.md
- make sure the follow roads is always set for "walk" we don't want to follow the road direction, just the shortest path between two points following the roads
- starting a route that was saved with "follow roads" do not follow roads when replaying it, they are just straight lines. we should have enough points saved so that the route is immutable once saved, it shouldn't be recomputed by OSRM
- the generated roaming route distance should include the distance required to return to the initial location when "return to start" is checked, we also shouldn't re-use the exact same path for the return path than the one way. Let's try to make it looks like a "loop" on the map

## Frontend UI/UX

- /frontend-design increase spacing between list items (replicate what's done in routes list) in favorite list screen
- /frontend-design route bottom drawer sheet in the map screen should have the exact same layout and style as the favorite bottom drawer sheet
- /frontend-design the floating widget route and favorite list should have the exact same layout and style as the route and favorite list screen
- /frontend-design in the map screen, roaming and center on map doesn't seem to have the same bg color as the other floating icon
- /frontend-design the recent searches should have awhite text like every other text in the map that is on a dark background
