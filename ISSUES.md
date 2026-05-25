# Known Issues & Backlog

## Documentation Outdated Items

No outstanding documentation issues.

---

## Backlog

### UI/UX issues

#### Settings Import/Export

Let's remove the data management section and replace it with top bar icons instead. We can add a "download" and "upload" icon, that will each open a dropdown menu.

The "download" one provides the following options:
- Export via QR code
- Export settings

The "upload" one provides the following options:
- Import from QR code
- Import from file
- Import from GPS Joystick
- Import from YAMLA

#### Settings user feedback

We should add a small notification , something not intrusive, when an error happens:
- failed to import, failed to save, etc. and append "click to report bug" which opens a link to the github repository issue.

we should also let the user know when everything went well ("settings saved", "import complete"), super discrete, discarded on click.

#### Map screen and floating map view UX

- Orders of the icons bottom right should be the same on both screen

#### Roaming UX

- Starting roaming from the bottom drawer sheet in the map screen does nothing
- Clicking the roaming icon from the map floating view does nothing, not even open the bottom drawer, both screen should behave the same way
- In the roaming bottom sheet, there should be a "generate" button on the left of "start", that generates the road and trace it on the map **without** starting the actual route. clicking "generate" again regenerate the road, we can add a random offset to the location and radius (nothing major like 2% of their actual value) so it can be different
- When roaming is started, the icon should become green, clicking on it expands it like we do for the floating widget route icon when started. it should be treaded like a route so we should be able to start/pause/stop the roaming.

#### Route and Favorite

##### List screen

- Replace the 3 dot vert icon with a "+" to make it easily comprehensible that this menu is used for adding new entity.
- Add a new icon in a topbar to sort (the one with the arrow point up next to an arrow point down), by default we will sort by creation date (newest up, oldest down), the user can revert it by clicking this button (newest down, oldest up)

##### Edit screen

- When deleting the text of the input, the previous name reappears, it makes it hard to start from scratch. we should just do like the SettingsScreen and if something changes, we make a "discard" and "save" text appear in the topbar.
- We don't need the delete icon from the edit screen, it already exists from the list
- When a route has 0 waypoint and we save it, we should just delete it

### Bug

- After starting roaming with spoofing disabled (which shouldn't be possible, roaming should be disabled if spoof isn't enabled), we are not able anymore to "start" spoofing, the icon remains green and stuck, even after we close the app, I'm not sure what is the exact location of the issue, investigate.
