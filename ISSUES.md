# ISSUES.md

This document references known issues for agents to pick them and iterate on

## List

### Icon

We should use the map icon everywhere we link to a map screen or map view, e.g. from the widget, not a gps or location icon.

### Feature refactor: Roaming

We need to plug-in and redesign the roaming feature.

The roaming screen on the app must disappear, it will be a standalone feature that is only available from the map screens (main app map screen or floating map view). Roaming is basically just creating a random route, the user provides a radius around the current location, and we dynamically generate a route that matches the distance in that radius.

In the map, a new floating icon is added (we can use the 360 material icon). Clicking on this icon opens the roaming menu (similar bottom drawer to the pick location), with roaming options. First input is a slider to select the radius around the current location (goes from 1km to 100km), defaults to 5km, second input is a slider to select the distance of the route to generate (goes from 50m to 50km), defaults to 1km, third input is buttons to select the speed profile (Walk/Run/Bike) which determines the speed (read the setting speed), and then two checkboxes: "follow roads" (when checked we create a route that follows OSRM roads for the selected speed profile, otherwise we create straight lines randomly in that radius), "return to initial location" (when checked, appends the initial location to the generated GPX roaming route).

In the settings, the default roaming option will be defined (and can be exported/imported), radius is 5km, distance is 1km, speed profile is walk, follow roads true, return to initial location true.
