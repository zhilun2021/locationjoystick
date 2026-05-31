# Play Store Launch Checklist — Manual Steps

These require action in Google Play Console or external tools.
Everything code-side is already implemented.

---

## Before First Upload

- [ ] Create Google Play Console developer account ($25 one-time fee)
- [ ] Create app entry — package `com.locationjoystick.app`, Android App Bundle

---

## Privacy Policy

- [ ] Publish `docs/wiki/privacy.html` to GitHub Pages
  - Enable GitHub Pages on the repo (Settings → Pages → main branch / docs folder)
  - Verify URL works: `https://shortcuts.github.io/locationjoystick/privacy.html`
- [ ] Add the privacy policy URL in Play Console → App content → Privacy policy

---

## Data Safety Form (mandatory — blocks submission)

Complete at Play Console → App content → Data safety.

AdMob collects the following — declare each:

| Data type | Collected | Shared | Purpose | Required by user |
|-----------|-----------|--------|---------|-----------------|
| Advertising ID | Yes | Yes (Google) | Advertising | No |
| Device or other IDs | Yes | Yes (Google) | Advertising | No |
| Approximate location | Yes | Yes (Google) | Advertising | No |
| App interactions | Yes | Yes (Google) | Analytics / Advertising | No |

Also declare:
- Location data (fine) — collected, not shared — app functionality (map centre, route recording)
- No data is encrypted in transit? → Mark as encrypted (HTTPS only)
- Users can request deletion? → Yes (delete advertising ID on device; no account to delete)

---

## Content Rating Questionnaire (mandatory)

Play Console → App content → App content rating → Start questionnaire.

Expected answers for locationjoystick:
- Category: **Utility / Tools**
- Violence: None
- Sexual content: None
- Language: None
- Controlled substances: None
- User-generated content: None (no UGC features)

Expected result: **Everyone (E)**

---

## Target Audience & Content (mandatory)

Play Console → App content → Target audience and content.

- Target age group: **Everyone** (no adult content, utility app)
- Does the app appeal to children? **No** (technical developer tool — not designed for children)
- This keeps content rating at E while avoiding child-directed ad restrictions (AdMob can still serve standard ads)

---

## Store Listing Assets

- [ ] **Short description** (≤80 chars): e.g. `Mock your GPS location on Android — no root required.`
- [ ] **Full description** (≤4000 chars): explain all features
- [ ] **Screenshots**: 15 already in `docs/wiki/screenshots/` — upload at least 2 phone screenshots
- [ ] **Feature graphic** (1024×500 px): design and upload — shown at top of listing
- [ ] **App icon** (512×512 px): use `docs/wiki/icon.png` (resize if needed)

---

## App Content Declarations

Play Console → App content:

- [ ] Ads: **Yes, contains ads** (AdMob banners)
- [ ] Sensitive permissions: declare `ACCESS_FINE_LOCATION` and `SYSTEM_ALERT_WINDOW` usage
- [ ] News app: No
- [ ] COVID-19 contact tracing: No

---

## Pre-launch Checklist

- [ ] `make lint` passes with zero errors
- [ ] `make test` passes
- [ ] `make bundle` produces a signed AAB (env vars set)
- [ ] Internal test track upload succeeds in Play Console
- [ ] UMP consent dialog appears on fresh install with EU locale:
  ```bash
  adb shell setprop persist.sys.locale de-DE && adb reboot
  ```
- [ ] Ads load after consent granted (check logcat for `Ads: Initialize`)
- [ ] Privacy Policy URL resolves in browser
- [ ] About screen Privacy Policy link opens correctly in-app
