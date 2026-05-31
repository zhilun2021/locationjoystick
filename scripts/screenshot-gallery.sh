#!/usr/bin/env bash
# screenshot-gallery.sh
#
# Captures wiki / Play Store gallery screenshots from a connected Android device.
# Re-run any time the app updates. Saves PNGs to ./screenshots/ (or --output DIR).
#
# Usage:
#   ./scripts/screenshot-gallery.sh
#   ./scripts/screenshot-gallery.sh --output /tmp/gallery
#   ./scripts/screenshot-gallery.sh --device emulator-5554
#
# Prerequisites:
#   - adb in PATH, device connected with USB debugging on
#   - App installed and past onboarding (permissions granted)
#   - At least one saved route must exist in the app (required for step 10)
#
# Overlay screens (joystick + widget) require manual activation — the script
# will pause and prompt you at those steps.
#
# Android Demo Mode is enabled for the duration of the run so screenshots show
# a clean status bar (neutral clock, full battery/signal, no notifications).
# Demo mode exits automatically on completion or error.
#
# Output files (15 canonical PNGs):
#   01_idle, 02_map, 03_routes, 04_favorites, 05_settings,
#   06_map_routes_sheet, 07_map_favorites_sheet, 08_map_roaming_sheet,
#   09_route_creator, 10_route_detail, 11_map_picker,
#   12_settings_scrolled, 13_qr_share,
#   14_joystick_overlay, 15_widget_overlay

set -euo pipefail

# ── Config ───────────────────────────────────────────────────────────────────

PACKAGE="com.locationjoystick.app"
ACTIVITY=".MainActivity"
OUTPUT_DIR="./screenshots"
ADB_DEVICE=""

# ── Arg parsing ──────────────────────────────────────────────────────────────

while [[ $# -gt 0 ]]; do
  case "$1" in
    --output)  OUTPUT_DIR="$2"; shift 2 ;;
    --device)  ADB_DEVICE="-s $2"; shift 2 ;;
    *) echo "Unknown arg: $1"; exit 1 ;;
  esac
done

ADB="adb $ADB_DEVICE"

# ── Helpers ──────────────────────────────────────────────────────────────────

log()  { echo "▶ $*"; }
warn() { echo "⚠ $*"; }

# Dump UI hierarchy to a temp file and return its path.
ui_dump() {
  local tmp
  tmp=$(mktemp /tmp/uidump.XXXXXX.xml)
  $ADB shell uiautomator dump /sdcard/uidump.xml >/dev/null 2>&1
  $ADB pull /sdcard/uidump.xml "$tmp" >/dev/null 2>&1
  echo "$tmp"
}

# Given UI dump file and a search term (matched against text= or content-desc=),
# echo the centre point as "X Y" of the first matching node.
# Uses [^>]* between the text match and bounds= to stay within one XML node
# (the uiautomator dump is a single line; [^>]* prevents crossing node boundaries).
bounds_of() {
  local dump="$1" term="$2"
  perl -lne '
    if (/(?:text|content-desc)="[^"]*'"${term}"'[^"]*"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"/i) {
      printf "%d %d\n", int(($1+$3)/2), int(($2+$4)/2);
      last;
    }
  ' "$dump" 2>/dev/null
}

# Tap a UI element by its visible text or content-desc (case-insensitive substring).
tap_text() {
  local text="$1"
  local dump centre x y
  dump=$(ui_dump)
  centre=$(bounds_of "$dump" "$text")
  rm -f "$dump"
  if [[ -z "$centre" ]]; then
    warn "Could not find UI element containing \"$text\" — skipping tap."
    return 1
  fi
  read -r x y <<< "$centre"
  log "Tapping \"$text\" at ($x, $y)"
  $ADB shell input tap "$x" "$y"
}

# Tap a UI element by text/content-desc, but only match nodes whose vertical
# centre is at or below min_y. Filters out closed-drawer items that remain in
# the semantics tree and would otherwise ambiguate IdleScreen card taps.
tap_text_below() {
  local text="$1" min_y="$2"
  local dump centre x y
  dump=$(ui_dump)
  centre=$(perl -lne '
    while (/(?:text|content-desc)="[^"]*'"${text}"'[^"]*"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"/gi) {
      my $cy = int(($2+$4)/2);
      next if $cy < '"${min_y}"';
      printf "%d %d\n", int(($1+$3)/2), $cy;
      last;
    }
  ' "$dump" 2>/dev/null)
  rm -f "$dump"
  if [[ -z "$centre" ]]; then
    warn "Could not find \"$text\" below y=$min_y — skipping tap."
    return 1
  fi
  read -r x y <<< "$centre"
  log "Tapping \"$text\" at ($x, $y) [y≥$min_y filter]"
  $ADB shell input tap "$x" "$y"
}

# Press the hardware back button.
back() { $ADB shell input keyevent KEYCODE_BACK; }

# Wait N seconds with a visible countdown.
wait_s() {
  local n="$1" msg="${2:-Waiting}"
  for (( i=n; i>0; i-- )); do
    printf "\r  %s… %ds " "$msg" "$i"
    sleep 1
  done
  printf "\r%*s\r" 40 ""
}

# Enter Android Demo Mode: clean status bar (neutral clock, full battery/signal,
# no notifications) so screenshots don't leak personal phone information.
demo_mode_enter() {
  log "Entering demo mode (clean status bar)..."
  $ADB shell settings put global sysui_demo_allowed 1 2>/dev/null || true
  $ADB shell am broadcast -a com.android.systemui.demo \
    -e command enter >/dev/null 2>&1 || true
  $ADB shell am broadcast -a com.android.systemui.demo \
    -e command clock -e hhmm 1200 >/dev/null 2>&1 || true
  $ADB shell am broadcast -a com.android.systemui.demo \
    -e command battery -e level 100 -e plugged false >/dev/null 2>&1 || true
  $ADB shell am broadcast -a com.android.systemui.demo \
    -e command network -e mobile show -e level 4 -e datatype lte \
    -e wifi show -e level 4 >/dev/null 2>&1 || true
  $ADB shell am broadcast -a com.android.systemui.demo \
    -e command notifications -e visible false >/dev/null 2>&1 || true
}

# Exit Android Demo Mode and restore the real status bar.
demo_mode_exit() {
  $ADB shell am broadcast -a com.android.systemui.demo \
    -e command exit >/dev/null 2>&1 || true
  log "Demo mode exited"
}

# Capture screen and pull to OUTPUT_DIR/<name>.png (idempotent overwrite).
screenshot() {
  local name="$1"
  local dest="$OUTPUT_DIR/${name}.png"
  log "Capturing → $dest"
  $ADB exec-out screencap -p > "$dest"
  echo "  Saved: $dest"
}

# Pause and wait for the user to perform a manual step.
pause_for_user() {
  local msg="$1"
  echo ""
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
  echo "  MANUAL STEP:"
  echo "  $msg"
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
  read -rp "  Press ENTER when ready..."
  echo ""
}

# Force-stop and restart the app to guarantee a clean IdleScreen landing.
# --activity-single-top only redelivers the intent; the Compose nav stack stays
# wherever it was. Force-stop is the only reliable way to reset it.
go_idle() {
  log "Returning to IdleScreen..."
  $ADB shell am force-stop "$PACKAGE"
  sleep 1
  $ADB shell am start -n "${PACKAGE}/${ACTIVITY}" >/dev/null
  wait_s 4 "App starting"
}

# ── Setup ────────────────────────────────────────────────────────────────────

mkdir -p "$OUTPUT_DIR"

log "Checking device..."
if ! $ADB devices | grep -q "device$"; then
  echo "Error: no device found. Connect a device or pass --device <serial>."
  exit 1
fi

DEVICE_MODEL=$($ADB shell getprop ro.product.model | tr -d '\r')
SCREEN_SIZE=$($ADB shell wm size | awk '{print $NF}')
log "Device: $DEVICE_MODEL ($SCREEN_SIZE)"

demo_mode_enter
trap demo_mode_exit EXIT

# Screen height for Y-threshold disambiguation of IdleScreen card taps.
SCREEN_H=$(echo "$SCREEN_SIZE" | awk -F'x' '{print $2}')
# IdleScreen cards live roughly in the bottom 70% of the display.
CARD_Y_MIN=$(( SCREEN_H * 30 / 100 ))

# ── 1. Launch app ────────────────────────────────────────────────────────────

log "Launching app (force-stop to clear any saved nav state)..."
$ADB shell am force-stop "$PACKAGE"
sleep 1
$ADB shell am start -n "${PACKAGE}/${ACTIVITY}" >/dev/null
wait_s 4 "App launching"

dump=$(ui_dump)
if grep -qi "onboarding\|Welcome\|grant\|permission" "$dump" 2>/dev/null; then
  rm -f "$dump"
  echo ""
  echo "Error: App appears to be on the onboarding screen."
  echo "Complete onboarding (grant permissions, enable mock location) then re-run."
  exit 1
fi
rm -f "$dump"

# ── 01. IdleScreen ───────────────────────────────────────────────────────────

log "=== 01 IDLE ==="
screenshot "01_idle"

# ── 02. Map screen ───────────────────────────────────────────────────────────

log "=== 02 MAP ==="
# Y-min filter prevents matching the closed drawer "Map" item in the semantics tree.
tap_text_below "Map" "$CARD_Y_MIN"
wait_s 3 "Map loading"
screenshot "02_map"

# ── 03. Routes screen ────────────────────────────────────────────────────────

log "=== 03 ROUTES ==="
go_idle
tap_text_below "Routes" "$CARD_Y_MIN"
wait_s 2 "Routes loading"
screenshot "03_routes"

# ── 04. Favorites screen ─────────────────────────────────────────────────────

log "=== 04 FAVORITES ==="
go_idle
tap_text_below "Favorites" "$CARD_Y_MIN"
wait_s 2 "Favorites loading"
screenshot "04_favorites"

# ── 05. Settings screen ──────────────────────────────────────────────────────

log "=== 05 SETTINGS ==="
go_idle
tap_text_below "Settings" "$CARD_Y_MIN"
wait_s 2 "Settings loading"
screenshot "05_settings"

# ── 06. Map → Routes bottom sheet ────────────────────────────────────────────

log "=== 06 MAP ROUTES SHEET ==="
go_idle
tap_text_below "Map" "$CARD_Y_MIN"
wait_s 3 "Map loading"
# Use content-desc of the FAB, not the bare label "Routes" which would match the drawer.
tap_text "open routes"
wait_s 2 "Routes sheet opening"
screenshot "06_map_routes_sheet"
back
wait_s 1 "Dismissing sheet"

# ── 07. Map → Favorites bottom sheet ─────────────────────────────────────────

log "=== 07 MAP FAVORITES SHEET ==="
tap_text "open favorites"
wait_s 2 "Favorites sheet opening"
screenshot "07_map_favorites_sheet"
back
wait_s 1 "Dismissing sheet"

# ── 08. Map → Roaming bottom sheet ───────────────────────────────────────────

log "=== 08 MAP ROAMING SHEET ==="
tap_text "start roaming"
wait_s 2 "Roaming sheet opening"
screenshot "08_map_roaming_sheet"
back
wait_s 1 "Dismissing sheet"

# ── 09. Route creator ────────────────────────────────────────────────────────

log "=== 09 ROUTE CREATOR ==="
go_idle
tap_text_below "Routes" "$CARD_Y_MIN"
wait_s 2 "Routes loading"
tap_text "Add route"
wait_s 1 "Add menu opening"
tap_text "from map"
wait_s 3 "Route creator loading"
screenshot "09_route_creator"
back
wait_s 1 "Returning to Routes"

# ── 10. Route detail ─────────────────────────────────────────────────────────

log "=== 10 ROUTE DETAIL ==="
pause_for_user "Ensure at least one route exists in the Routes list, then press ENTER."
# Open the overflow menu on the first visible route and tap Edit.
tap_text "Menu"
wait_s 1 "Menu opening"
tap_text "Edit"
wait_s 2 "Route detail loading"
screenshot "10_route_detail"
back
wait_s 1 "Returning to Routes"

# ── 11. Map picker (from Favorites add flow) ──────────────────────────────────

log "=== 11 MAP PICKER ==="
go_idle
tap_text_below "Favorites" "$CARD_Y_MIN"
wait_s 2 "Favorites loading"
tap_text "Add favorite"
wait_s 1 "Add menu opening"
tap_text "from map"
wait_s 3 "Map picker loading"
screenshot "11_map_picker"
back
wait_s 1 "Returning to Favorites"

# ── 12. Settings scrolled ────────────────────────────────────────────────────

log "=== 12 SETTINGS SCROLLED ==="
go_idle
tap_text_below "Settings" "$CARD_Y_MIN"
wait_s 2 "Settings loading"
$ADB shell input swipe 540 1800 540 600 600
wait_s 1 "Scrolling"
screenshot "12_settings_scrolled"

# ── 13. QR share dialog ──────────────────────────────────────────────────────

log "=== 13 QR SHARE ==="
tap_text "Export"
wait_s 1 "Export menu opening"
tap_text "QR"
wait_s 2 "QR share dialog opening"
screenshot "13_qr_share"
back
wait_s 1 "Dismissing QR dialog"

# ── 14. Joystick overlay ─────────────────────────────────────────────────────

log "=== 14 JOYSTICK OVERLAY ==="
pause_for_user "Start mock location then enable the Floating Joystick.
  The joystick overlay should be visible on screen before you press ENTER.
  Tip: Map screen → start spoofing → enable joystick from widget or drawer."
screenshot "14_joystick_overlay"

# ── 15. Floating widget ──────────────────────────────────────────────────────

log "=== 15 FLOATING WIDGET ==="
pause_for_user "Dismiss the joystick (if open) and enable the Floating Widget instead.
  The widget bubble should be visible on screen before you press ENTER."
screenshot "15_widget_overlay"

# ── Done ─────────────────────────────────────────────────────────────────────

demo_mode_exit
trap - EXIT

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  Gallery captured: $OUTPUT_DIR"
echo ""
ls -1 "$OUTPUT_DIR"/*.png 2>/dev/null | while read -r f; do
  SIZE=$(du -h "$f" | cut -f1)
  DIMS=$( sips -g pixelWidth -g pixelHeight "$f" 2>/dev/null \
    | awk '/pixelWidth/{w=$2} /pixelHeight/{h=$2} END{print w"x"h}' \
    || echo "?" )
  printf "  %-35s  %s  %s\n" "$(basename "$f")" "$DIMS" "$SIZE"
done
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
