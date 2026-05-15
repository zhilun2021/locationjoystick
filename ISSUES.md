# ISSUES.md

This document references known issues for agents to pick them and iterate on

## List

- App UX: Use our app icon instead of the GPS icon in the Onboarding and Idled screen.

- Bug: Coming from the Idle screen to the Settings screen, importing a settings file bring me back to the Idle page without doing the import nor opening the import validation modal

- DX: Codebase naming and consistency is bad. Iterate over plan .opencode/plans/naming.md, search for other missing renames.

- Map UX: Playing a route, or going from the current location to an other (e.g. after clicking the map and chosing "Walk"), the route should be traced in the map, in order to give better feedback to the user.

- Bug: Speed isn't correct. I've tested different other apps and their speed is usually consistent at the same km/h, however ours is too fast. it seems like we go at least 2 times faster than we should.
