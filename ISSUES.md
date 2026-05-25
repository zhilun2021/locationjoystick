# Known Issues & Backlog

## Documentation Outdated Items

No outstanding documentation issues.

---

## Backlog

### Import from QR code

The transfer properly spawns a QR code but scanning (import) one does nothing, no confirmation modal is opened.

### Import from GPS joystick

fails with 05-25 14:18:57.215 17624 17624 W WindowOnBackDispatcher: sendCancelIfRunning: isInProgress=false callback=android.view.ViewRootImpl$$ExternalSyntheticLambda13@5a9c3
05-25 14:18:57.230 17624 17624 E SettingsViewModel: GPS Joystick import failed: Not a valid Realm database file (missing T-DB header)
05-25 14:18:57.249 17624 17624 D InsetsController: hide(ime(), fromIme=false)
05-25 14:18:57.249 17624 17624 I ImeTracker: com.locationjoystick.app:bab5281c: onCancelled at PHASE_CLIENT_ALREADY_HIDDEN

you can find multiple GPS joystick save for you to add assertion test and make sure it works properly: 
