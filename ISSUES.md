# ISSUES.md

This document references known issues for agents to pick them and iterate on

## List

- Use our app icon instead of the GPS icon in the Onboarding and Idled screen.
- Coming from the Idle screen to the Settings screen, importing a settings file bring me back to the Idle page without doing the import nor opening the import validation modal

- Codebase naming and consistency is bad. Filenames are confusing. We should drive naming from feature scope + android specific naming, our available screens are: onboarding (when app is installed or missing permissions), idle (the main app landing screen), map, favorites, routes, roaming, settings. then we have a subset of those screen available from the floating widget: map, routes favorites, plus some other features that are widget specific: joystick, lock joystick position, cycle through speed profiles. since those subset pages are linked to the floating widget, we should reference them as floating view, rather than a screen. Let's make sure the file and variable naming is consistent with those definitions, and update the README.md and AGENTS.md accordingly.
