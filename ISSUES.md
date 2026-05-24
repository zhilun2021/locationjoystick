# Known Issues & Backlog

## Documentation Outdated Items

No outstanding documentation issues.

---

## Backlog

### UI / UX improvements

- In the settings screen, make the idle radius and interval side by side input. Same for the moving radius and interval.
- In the settings screen, the floating save settings button is the same color as the import one, when it appears the color blend and it's hard to distinguish. let's remove the floating button in favor of a "Save" in the top bar in LjAccent And a "Discard" button in grey to close without saving changes.
- In the about screen, text shouldn't be centered, only the app icon should be. Also make sure there's no leftover in that screen and that it is still updated for the credits etc.
- In the about screen, the topbar provides a "back" button, it should be the same open drawer button as every other screens.
- In the floating map view, adding a "new point" when an active "walk to" point exists deletes the traced line. the line should just be updated with the new waypoint.
- In the floating map view, the "start" and "stop" buttons do not have a color like the regular Map screen
- In the floating map view, clicking on the search icon brings the search input, however clicking the search input does nothing.


### Save last location

- When stopping "spoof" and closing the app, re-opening it should put the map on the last known location. instead it puts us back in the default Paris coordinate.

### Routes

- Clicking the stop button from the routes screen list stops spoofing. make sure there's nothing in the codebase that stops spoofing except the actual "stop" spoofing button.
