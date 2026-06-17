# Group Sync

Sync spoofed location across multiple devices on the same Wi-Fi network. No account required.

Key files: `:feature:group:impl/GroupSyncScreen.kt`, `:feature:group:impl/GroupSyncViewModel.kt`, `:core:data/GroupRepository.kt`

## Roles

- **Leader**: hosts the group. Starts a local server, generates a QR code, and broadcasts position to followers when sharing is enabled.
- **Follower**: joins via QR scan. Mirrors leader's spoofed location when follower mode is enabled.
- **None**: not in a group (default state).

## Flow

### Create a group (leader)
1. Open Group Sync screen → "Create group — I'm the leader".
2. `MockLocationService` starts a leader server (`ACTION_START_LEADER` + group UUID).
3. QR code generated encoding `locationjoystick://group?host=HOST&port=PORT&id=GROUP_ID`.
4. Toggle **Sharing** to broadcast location to followers.

### Join a group (follower)
1. Scan leader's QR code from the Group Sync screen (uses existing ZXing scanner).
2. `GroupRepository.joinGroup(invite)` stores connection details.
3. Toggle **Follow leader** to enable follower mode (`ACTION_ENTER_FOLLOWER`).
4. Follower mode survives app restart — resumes automatically on next launch.

### Leave
- Leader: `ACTION_EXIT_LEADER` → server stopped.
- Follower: `ACTION_EXIT_FOLLOWER` if active → group cleared.

## Deep Link / QR Format

```
locationjoystick://group?host=HOST&port=PORT&id=GROUP_ID
```

Scanned by the same QR scanner used for config transfer. Parsed via `GroupRepository.pendingGroupInvite`.

## State Machine

`GroupRepository` exposes `groupState: StateFlow<GroupState>`. Transitions:

```
NONE → LEADER   (createGroup)
NONE → FOLLOWER (joinGroup via QR)
LEADER → NONE   (leaveGroup)
FOLLOWER → NONE (leaveGroup)
LEADER → FOLLOWER (joinGroup while leading — exits leader first)
```

## Service Commands

All group state changes are sent as `Intent` actions to `MockLocationService`:

| Action | Extra keys |
|--------|-----------|
| `ACTION_START_LEADER` | `EXTRA_LEADER_GROUP_ID` |
| `ACTION_EXIT_LEADER` | — |
| `ACTION_ENTER_FOLLOWER` | `EXTRA_FOLLOWER_HOST`, `EXTRA_FOLLOWER_PORT`, `EXTRA_FOLLOWER_GROUP_ID` |
| `ACTION_EXIT_FOLLOWER` | — |

## Jitter

Followers apply independent per-device jitter to the received position, preserving the configured jitter profile without cloning the leader's exact coordinates.

## Edge Cases

- Joining while leading → leader exits first, then joins as follower.
- Network unavailable → follower silently disconnects; UI shows last known role.
- QR regeneration: leader can regenerate QR (new port/session) without changing the group ID.
