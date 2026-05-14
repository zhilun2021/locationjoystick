# ISSUES.md

This document references known issues for agents to pick them and iterate on

## floating widget

the widgets should open below the main icon, not expand on both ends. right now when I click on the icon to open the widgets, it gets pushed up, and icons appear "centered" to the initial icon location, it should just open them below to make it smoother

## route and favorite creation from map

we should see our current location on the map when trying to create a new favorite or a new route from the map screen

## move speed

it seems like our implementation doesn't properly respect the speed and/or meter per seconds, i've compared with other apps and we seem to move faster (i'd say around 2 times faster) at the same km/h defined in the settings config, I'm comparing with GPS Joystick, we can take inspiration of open source apps such as: https://github.com/henryfung0/fake-gps-android/ https://github.com/mcastillof/faketraveler https://github.com/brutalharsh/mock-location-app https://github.com/noobexon1/XposedFakeLocation

## gps jitter

in order to avoid triggering the anti cheat behavior detection of certain apps and games (such as pokemon go) we should add a small gps jitter, other apps provide such feature: https://github.com/henryfung0/fake-gps-android/ https://github.com/mcastillof/faketraveler https://github.com/brutalharsh/mock-location-app https://github.com/noobexon1/XposedFakeLocation
