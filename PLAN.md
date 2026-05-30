# Plan: Public Release Candidate — README, Reddit Post, Wiki

## Context

First public OSS release of locationjoystick. Three surfaces need work before the repo goes public:
README (PM-friendly first section), reddit-post.md (first community announcement), and a new
docs/wiki/ GitHub Pages site with per-screen documentation and screenshots.

Confirmed constraints:
- App **has ads** — do not claim "no ads" anywhere. Keep AdMob section in README.
- Canonical repo URL: **github.com/shortcuts/locationjoystick** — fix README badges.
- Play Store link: leave as labeled TODO (not yet available).

---

## Deliverable 1 — README.md first-section rewrite

**What changes:** The content before `## Download`, plus a full-file find-replace of the org slug (`locationjoystick/locationjoystick` → `shortcuts/locationjoystick`) — the Download and Building sections also contain wrong-org URLs and must be corrected even though their structure is otherwise untouched.

**New structure:**

```
# locationjoystick
[badges — fix org slug; also find-replace all locationjoystick/locationjoystick → shortcuts/locationjoystick in the file]

[2-sentence product description — plain language, non-technical]

## Why locationjoystick?
3–4 bullet points: free/open-source, no root required, works while other apps run,
import from GPS Joystick / YAMLA in seconds.

## What can it do?
Keep existing feature table (14 rows) but move it here, below the why-bullets.
Add a plain-language caption above the table: "Here's everything included:"

## Download
[unchanged...]
```

**Do not touch:** badges structure (only fix org slug), everything from `## Download` down, AdMob env-var section.

---

## Deliverable 2 — docs/reddit-post.md

`docs/reddit-post.md` already exists with a skeleton (title, disclaimer, hook, credibility, GPS Joystick/YAMLA comparison, outro). Complete it — do not rewrite what's already good. Two required fixups:
1. Replace the empty `- ` bullet under feature highlights with 8–10 plain-language bullets.
2. Replace `[on the play store]()` (broken empty href) with plain text `[coming soon]` (no anchor tag).

Full post, 6 beats:

1. **Hook** — "I've spent 4+ years frustrated by GPS Joystick and YAMLA, so I built a replacement."
2. **Credibility** — keep existing pgpemu reference (maintainer of pgpemu, not a one-shot project)
3. **Why better than GPS Joystick** — specific: ad gates before key actions, UX friction (no smooth import, no roaming, clunky widget)
4. **Why better than YAMLA** — specific: missing route looping, no road-following roaming, no widget, no QR transfer, no GPS realism
5. **Feature highlights** — fill the empty `-` bullet with 8–10 plain-language bullets (no game names, no jargon)
6. **Links** — GitHub: `github.com/shortcuts/locationjoystick`, Play Store: `[coming soon]`, docs/wiki link once live

Tone: first-person dev, direct, no marketing superlatives.

---

## Deliverable 3 — docs/wiki/ GitHub Pages site

### GitHub Pages config

Use **Option A**: `main` branch + `/docs` folder. Set Pages source to `docs/` in repo settings.
Site lives at `https://shortcuts.github.io/locationjoystick/wiki/`.
Add `.nojekyll` file to docs/wiki/ to skip Jekyll processing.

### Directory structure

`.nojekyll` must live at `docs/.nojekyll` (root of the Pages source), **not** inside `docs/wiki/`. A subdirectory `.nojekyll` is silently ignored by GitHub Pages; Jekyll will process the whole tree.

After running the screenshot script, `git add docs/wiki/screenshots/` and commit — GitHub Pages serves only what is in the repo. Untracked PNGs = broken images.

**App icon:** copy `app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png` → `docs/wiki/icon.png` (192×192 RGBA, 56 K). Commit it. Every HTML page references it as favicon and the `index.html` uses it as the header logo.

```
docs/
  .nojekyll           ← suppresses Jekyll for the whole docs/ source
  wiki/
    icon.png          ← copied from mipmap-xxxhdpi/ic_launcher_round.png
    index.html        ← overview + nav to all sections
    style.css         ← shared minimal CSS (no JS)
    home.html         ← Idle screen
    map.html          ← Map + all map bottom sheets
    routes.html       ← Routes list + creator + detail
    favorites.html    ← Favorites + map picker
    settings.html     ← Settings (all sections) + QR screens
    overlays.html     ← Joystick + widget overlays
    onboarding.html   ← Onboarding steps
    screenshots/      ← committed PNGs from screenshot-gallery.sh
```

### Page → screenshot → content source

| Page | Screenshot files | Content source |
|------|-----------------|----------------|
| home.html | 01_idle | README feature table |
| map.html | 02_map, 06_map_routes_sheet, 07_map_favorites_sheet, 08_map_roaming_sheet | docs/features/map.md, click-to-move.md, roaming.md |
| routes.html | 03_routes, 09_route_creator, 10_route_detail | docs/features/routes.md |
| favorites.html | 04_favorites, 11_map_picker | docs/features/favorites.md |
| settings.html | 05_settings, 12_settings_scrolled, 13_qr_share | docs/features/export-import.md, speed-profiles.md, widget.md |
| overlays.html | 14_joystick_overlay, 15_widget_overlay | docs/features/joystick.md, widget.md |
| onboarding.html | (manual / device-specific) | docs/features/onboarding.md |

Every HTML page `<head>` must include:
```html
<link rel="icon" href="icon.png" type="image/png">
```

`index.html` header must include the logo:
```html
<img src="icon.png" alt="locationjoystick icon" width="48" height="48">
```

HTML structure per page:
```html
<section id="screen-name">
  <img src="screenshots/XX_name.png" alt="Screen description">
  <ul>
    <li>What user can do — 2-3 bullets max</li>
  </ul>
</section>
```

No JS. No htmx needed — static content only. CSS: system font stack, max-width 900px, screenshots max-width 300px inline.

---

## Deliverable 4 — scripts/screenshot-gallery.sh expansion

Expand 6 → 15 canonical output files. New naming scheme (section-prefixed, sequential):

| # | Filename | How captured |
|---|----------|-------------|
| 01 | 01_idle | navigate to IdleScreen |
| 02 | 02_map | tap Map card |
| 03 | 03_routes | navigate to Routes |
| 04 | 04_favorites | navigate to Favorites |
| 05 | 05_settings | navigate to Settings |
| 06 | 06_map_routes_sheet | from Map, open routes bottom sheet |
| 07 | 07_map_favorites_sheet | from Map, open favorites bottom sheet |
| 08 | 08_map_roaming_sheet | from Map, open roaming bottom sheet |
| 09 | 09_route_creator | from Routes, tap Add → from map |
| 10 | 10_route_detail | from Routes, open existing route detail |
| 11 | 11_map_picker | from Favorites, tap Add → from map |
| 12 | 12_settings_scrolled | Settings scrolled down |
| 13 | 13_qr_share | Settings → export → QR |
| 14 | 14_joystick_overlay | manual pause_for_user step |
| 15 | 15_widget_overlay | manual pause_for_user step |

Keep all existing helpers (`tap_text`, `ui_dump`, `screenshot`, `pause_for_user`). Idempotent overwrite. Old filenames replaced — wiki references new names.

Navigation for new sheets: from Map screen, tap FABs by their verified content descriptions — `"open routes"`, `"open favorites"`, `"start roaming"` — not the bare label strings ("Routes" etc.), which won't match. Dismiss each sheet via `back`.

**Route detail prerequisite:** step 10 requires at least one saved route. Add a `pause_for_user` gate before step 10: "Ensure at least one route exists in the Routes list, then press ENTER." Also document this in the script header prerequisites.

**Nav card tap disambiguation:** `tap_text("Map")` / `tap_text("Routes")` from IdleScreen will ambiguously match both the IdleScreen card and the closed drawer item (drawer items stay in the semantics tree). Use coordinate-filtered taps (bottom half of screen) or tap unique card subtitles instead of destination-name text.

---

## Verification

1. **Screenshot 1:1 check** — every `<img src=>` in wiki HTML has a matching file in screenshots/
2. **Repo URL grep** — `grep -r "locationjoystick/locationjoystick" README.md docs/wiki/` → zero hits
3. **No-JS check** — `grep -r "<script" docs/wiki/` → zero hits
4. **No "no ads" claim** — `grep -ri "no ads\|ad-free" README.md docs/reddit-post.md` → zero hits
5. **Link check** — all `<a href>` in wiki point to valid anchors or external URLs
6. **Play Store placeholder** — grep confirms `[coming soon]` or equivalent, not a broken link
7. **Render check** — open index.html in browser, verify screenshots load and nav works

---

## Execution order

Steps 1 and 2 are independent — run in parallel.
Step 3 (screenshot script) must complete before step 4 (wiki references exact filenames).

1. Fix README.md badges + rewrite intro section
2. Write docs/reddit-post.md (full content)
3. Expand scripts/screenshot-gallery.sh (add 9 new steps, rename outputs)
4. Create docs/wiki/ structure (index + 7 pages + style.css + .nojekyll)
