# ISSUES.md

This document references known issues for agents to pick them and iterate on

## List

- App UX: Switching from screen is not ideal, it's not smooth, let's revise the plan at .opencode/plans/navigation_transitions.md

- DX: Codebase naming and consistency is bad. Iterate over plan .opencode/plans/naming.md, search for other missing renames.

- DX: There is too many constant spread in different files, sometimes we redefine some, we also manually provide them in the README.md file etc. we should have a single source of truth file that is imported from everywhere which constains all of the constant values with a clear description of what it does, so the README.md and AGENTS.md can just read form it.

- Map UX: Playing a route, or going from the current location to an other (e.g. after clicking the map and chosing "Walk"), the route should be traced in the map, in order to give better feedback to the user. See plan at .opencode/plans/map-route-tracing.md
