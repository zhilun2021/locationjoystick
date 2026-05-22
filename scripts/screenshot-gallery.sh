#!/usr/bin/env bash
# screenshot-gallery.sh
#
# Captures Play Store / GitHub gallery screenshots from a connected Android device.
# Re-run any time the app updates. Saves PNGs to ./screenshots/ (or --output DIR).
#
# Usage:
#   ./scripts/screenshot-gallery.sh
#   ./scripts/screenshot-gallery.sh --output /tmp/gallery
#   ./scripts/screenshot-gallery.sh --device emulator-5554
#
# Prerequisites: adb in PATH, device connected with USB debugging on.
# The app must already be installed and past onboarding (permissions granted).
#
# Overlay screens (joystick + widget) require manual activation — the script
# will pause and prompt you at those steps.

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
bounds_of() {
  local dump="$1" term="$2"
  # Extract the bounds attribute from the line containing the term.
  local bounds
  bounds=$(grep -oP "(text|content-desc)=\"[^\"]*${term}[^\"]*\"[^>]*bounds=\"\[\K[0-9,\]\[]*" "$dump" | head -1)
  if [[ -z "$bounds" ]]; then
    echo ""
    return
  fi
  # bounds format: x1,y1][x2,y2
  local x1 y1 x2 y2
  x1=$(echo "$bounds" | grep -oP '^\d+')
  y1=$(echo "$bounds" | grep -oP '^\d+,\K\d+')
  x2=$(echo "$bounds" | grep -oP '\]\[(\d+)' | grep -oP '\d+')
  y2=$(echo "$bounds" | grep -oP '\]\[\d+,\K\d+')
  echo "$(( (x1 + x2) / 2 )) $(( (y1 + y2) / 2 ))"
}

# Tap a UI element by its visible text (case-insensitive substring).
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

# Press the hardware back button.
back() { $ADB shell input keyevent KEYCODE_BACK; }

# Wait N seconds with a visible countdown.
wait_s() {
  local n="$1" msg="${2:-Waiting}"
  for (( i=n; i>0; i-- )); do
    printf "\r  %s… %ds " "$msg" "$i"
    sleep 1
  done
  printf "\r%*s\r" 40 ""   # clear line
}

# Capture screen and pull to OUTPUT_DIR/<name>.png
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

# Navigate the app drawer to a destination by its label.
drawer_navigate() {
  local label="$1"
  # Open drawer via hamburger (top-left)
  log "Opening navigation drawer"
  $ADB shell input tap 60 120
  wait_s 1 "Drawer opening"
  tap_text "$label"
  wait_s 2 "Navigating"
}

# ── Setup ────────────────────────────────────────────────────────────────────

mkdir -p "$OUTPUT_DIR"

log "Checking device..."
if ! $ADB devices | grep -q "device$"; then
  echo "Error: no device found. Connect a device or pass --device <serial>."
  exit 1
fi

DEVICE_MODEL=$($ADB shell getprop ro.product.model | tr -d '\r')
SCREEN_SIZE=$($ADB shell wm size | grep -oP '\d+x\d+')
log "Device: $DEVICE_MODEL ($SCREEN_SIZE)"

# ── 1. Launch app at IdleScreen ──────────────────────────────────────────────

log "Launching app..."
$ADB shell am start -n "${PACKAGE}/${ACTIVITY}" \
  --activity-clear-top --activity-single-top >/dev/null
wait_s 3 "App launching"

# If we land on onboarding, bail out early with a clear message.
dump=$(ui_dump)
if grep -qi "onboarding\|Welcome\|grant\|permission" "$dump" 2>/dev/null; then
  rm -f "$dump"
  echo ""
  echo "Error: App appears to be on the onboarding screen."
  echo "Complete onboarding (grant permissions, enable mock location) then re-run."
  exit 1
fi
rm -f "$dump"

# ── 2. Map screen ────────────────────────────────────────────────────────────

log "=== MAP ==="
tap_text "Map"
wait_s 3 "Map loading"
screenshot "01_map"

# ── 3. Routes screen ─────────────────────────────────────────────────────────

log "=== ROUTES ==="
back
wait_s 1 "Going back"
tap_text "Routes"
wait_s 2 "Routes loading"
screenshot "02_routes"

# ── 4. Settings screen ───────────────────────────────────────────────────────

log "=== SETTINGS ==="
back
wait_s 1 "Going back"
tap_text "Settings"
wait_s 2 "Settings loading"
screenshot "03_settings"

# Scroll down in settings to capture speed profiles / realism section
log "Scrolling settings for more content..."
$ADB shell input swipe 540 1800 540 600 600
wait_s 1 "Scrolling"
screenshot "04_settings_scrolled"

# ── 5. Back to IdleScreen ────────────────────────────────────────────────────

back
wait_s 1 "Going back"

# ── 6. Joystick overlay ──────────────────────────────────────────────────────

log "=== JOYSTICK ==="
pause_for_user "Start mock location then enable the Floating Joystick in the app.
  The joystick overlay should be visible on screen before you press ENTER.
  Tip: Map screen → start spoofing → enable joystick from widget or drawer."
screenshot "05_joystick_overlay"

# ── 7. Floating widget ───────────────────────────────────────────────────────

log "=== FLOATING WIDGET ==="
pause_for_user "Dismiss the joystick (if open) and enable the Floating Widget instead.
  The widget bubble should be visible on screen before you press ENTER."
screenshot "06_widget_overlay"

# ── Done ─────────────────────────────────────────────────────────────────────

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
