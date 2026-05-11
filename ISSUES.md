# ISSUES.md

This document references known issues for agents to pick them and iterate on

## route row

on a created route, the row displayed in the route list, the buttons overflows to the next line, we should instead make each route a two line row:
- first line: ${Route name} - ${Distance}
- second line: every buttons

## route crash

when clicking on a route row, to display the waypoint, the app crashes. we should also remove the "edit" button and move the name edit to the click on row.

## UX consistency

when creating a route, clicking save opens a modal to enter the name
when creating a favorite from a map, the input for the name is always present before saving

change the create favorite from map behavior to behave like create route

## route following the roads

it follows the road assuming we are a car, it should find the shortest way instead, and not follow the direction of traffic.

## settings input

remove any keystroke listener on input and do not save on button state change, add a save button

## onboarding screen

we should skip the onboarding screen when all of the required settings are met, and go directly to the "map" view. also remove the "optional" battery optimization option recommendation.

## route start

when starting a route there should be two icon: the current "start" icon should walk to the first gpx of the route, basically saying that the route starts from current location. second icon should be "play" icon, this sets the current location to be the first gpx of the route.
