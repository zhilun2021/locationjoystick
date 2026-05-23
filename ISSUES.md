# Known Issues & Backlog

## Documentation Outdated Items

No outstanding documentation issues.

---

## Backlog

### Onboarding screen

We have one section with "check again" but not the others, if we believe we need it we should add it everywhere, otherwise let's just remove it. i personally never had to click on it, it was refreshed right away

### Info screen

- Everything should be renamed "About", that's the name of the screen in the app, but in the code it's "info"

### Map screen

- When having an active spoof location, clicking on a favorite opens a bottom drawer sheet that has a different style of the standard bottom drawer sheet after clicking on the map (with the Do nothing), we should reuse the one with the "Do nothing" and remove the one with the "Back" from the codebase.
- When clicking on the map to add a new point to walk to, the tracing disappears, but the route seems to properly execute still
