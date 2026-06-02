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

Declare the following:

| Data type | Collected | Shared | Purpose | Required by user |
|-----------|-----------|--------|---------|-----------------|
| Approximate location | No | No | App functionality | No |
| App interactions | No | No | App functionality | No |

Also declare:
- Location data (fine) — collected, not shared — app functionality (map centre, route recording)
- Data is encrypted in transit (HTTPS only)
- Users can request deletion? → No user data stored server-side; all data is on-device

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
- This keeps content rating at E (utility app, no adult content)

---

## Store Listing Assets

- [ ] **Short description** (≤80 chars): `Mock your GPS location on Android — no root, no ads.`
- [ ] **Full description** (≤4000 chars): explain all features
- [ ] **Screenshots**: 15 already in `docs/wiki/screenshots/` — upload at least 2 phone screenshots
- [ ] **Feature graphic** (1024×500 px): design and upload — shown at top of listing
- [ ] **App icon** (512×512 px): use `docs/wiki/icon.png` (resize if needed)

---

## App Content Declarations

Play Console → App content:

- [ ] Ads: **No ads** — app contains no advertising SDKs or ad placements
- [ ] Sensitive permissions: declare `ACCESS_FINE_LOCATION` and `SYSTEM_ALERT_WINDOW` usage
- [ ] News app: No
- [ ] COVID-19 contact tracing: No

---

## Pre-launch Checklist

- [ ] `make lint` passes with zero errors
- [ ] `make test` passes
- [ ] `make bundle` produces a signed AAB (env vars set)
- [ ] Internal test track upload succeeds in Play Console
- [ ] Privacy Policy URL resolves in browser
- [ ] About screen Privacy Policy link opens correctly in-app
