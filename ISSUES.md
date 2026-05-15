# ISSUES.md

This document references known issues for agents to pick them and iterate on

## List

- DX: Codebase naming and consistency is bad. Iterate over plan .opencode/plans/naming.md, search for other missing renames.

- DX: There is too many constant spread in different files, sometimes we redefine some, we also manually provide them in the README.md file etc. we should have a single source of truth file that is imported from everywhere which constains all of the constant values with a clear description of what it does, so the README.md and AGENTS.md can just read form it. Implement the plan as your base findings .opencode/plans/constants-consolidation.md and iterate over the codebase to make sure everything is tackled and referenced.
