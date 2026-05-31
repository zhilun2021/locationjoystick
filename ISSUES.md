# Known Issues & Backlog

## Documentation Outdated Items

No outstanding documentation issues.

---

## Screenshots

- Missing capture Screenshots 14 (`joystick_overlay`) and 15 (`widget_overlay`)

---

## Backlog

### UI/UX issues

- The floating widget image (the app icon) doesn't take the full width so we can see an orange background. it should take the full width.
- When the joystick direction is on a border (e.g. we go full north) the direction circle appears "cut" like it reaches the edge of the floating joystick
- The roaming drawer sheet from the floating map view isn't the same as for the map screen. It should be exactly like the map screen one
- The bottom drawer sheet on any map (floating map or map screen) should take at most 80% of the height. right now: floating map screen favorite takes 100%, etc.
- Search results from the floating map view are written in black on a black background. it should be like in the map screen, text in white

### Bug

- When spoofing is started, clicking on a search result shouldn't change the location right away, it should open the bottom drawer sheet confirmation (like when clicked on the map) so the user can decide what action to do.
- The joystick isn't properly draggable everywhere on the screen, it should be exactly like the floating widget to be able to move it anywhere
- When generating a "roaming" route with "Return to start" checked, the generated route should be a loop, otherwise it would double the length of the roaming session.
- Smoke tests do not assert floating widget behavior.
- Remove the "create route from map" buttom from the floating widget route menu, let's make it read only instead for simplicity. user create routes from the app, not the floating view.
