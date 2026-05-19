# Known Issues & Backlog

## Documentation Outdated Items

The following items in README.md and AGENTS.md are outdated and should be addressed:

### 1. `:feature:roaming` module does not exist

**Location**: README.md line 169, AGENTS.md line 169

**Issue**: Documentation lists `:feature:roaming:api` / `:impl` as a module with note "(scaffolding — not yet wired into build)".

**Reality**: The `:feature:roaming` module was never created. Roaming functionality is implemented in:
- `core:routing/RoamingEngine.kt` - Core engine
- `core:data/RoamingRepository.kt` - Repository
- The UI is integrated into Map screen's bottom sheet, not a separate feature module

**Action needed**: Remove the reference to `:feature:roaming` module from both README.md and AGENTS.md module tables, or clarify that roaming is accessed via Map screen.

---

## Backlog

_No open issues._
