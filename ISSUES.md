# ISSUES.md

This document references known issues for agents to pick them and iterate on

## Routes not cleared from map

When a route ends (either because the last point have been reached, or the user stopped it), the points and lines should be removed from the mapscreen / mapfloatingview, only leaving the current position point visible. Implement the fix from plan ~/.claude/plans/when-a-route-ends-expressive-shell.md

## Speed issue

The current speed isn't respected, it seems like we are going **at least** 2 times faster than we should, from the GPS speedometer app, when setting 2km/h, we are actually going at 5km/h. From the log when starting a route we can see 05-16 23:32:30.428 23123 24544 I RouteReplayEngine: Replay started: 6 waypoints at 1.4m/s looping=false, which means we are not setting the speed properly from the user settings? Investigate the issue

