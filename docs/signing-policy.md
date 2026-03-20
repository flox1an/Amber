# Signing Policy Documentation

This document describes the current signing policy behavior in Amber as of March 2026.

## Overview

Amber has three signing policies that control how permission requests from apps are handled. The policy is set at two levels:

1. **Global default** (`account.signPolicy`) — applied when a new app connects
2. **Per-app** — each app inherits the global default but can be overridden during connection

The policies are stored as integers: `0` = Basic, `1` = Manual, `2` = Fully Trust.

## Policy 0: "Approve basic actions" (Default)

**UI Label:** "Approve basic actions"
**UI Description:** "Auto-approve most common requests."

When an app connects with this policy, a fixed set of permissions (`basicPermissions`) are auto-approved **permanently** (`acceptUntil = Long.MAX_VALUE`). Any request matching these permissions is signed silently without showing the approval UI.

### Auto-approved non-event operations

| Permission | Description |
|---|---|
| `get_public_key` | Read your public key |
| `nip04_encrypt` | Encrypt data (NIP-04) |
| `nip04_decrypt` | Decrypt data (NIP-04) |
| `nip44_encrypt` | Encrypt data (NIP-44) |
| `nip44_decrypt` | Decrypt data (NIP-44) |
| `decrypt_zap_event` | Decrypt private zaps |

### Auto-approved event kinds

| Kind | NIP | Description | Risk |
|---|---|---|---|
| 0 | NIP-01 | Profile metadata | **High** — can redirect lightning payments |
| 1 | NIP-01 | Short text note | Low |
| 3 | NIP-02 | Follow/contact list | **Medium** — replaces entire follow list |
| 4 | NIP-04 | Legacy encrypted DM | Medium |
| 5 | NIP-09 | Event deletion | Low |
| 6 | NIP-18 | Repost/boost | Low |
| 7 | NIP-25 | Reaction (like) | Low |
| 9734 | NIP-57 | Zap request | **Medium** — initiates payments |
| 9735 | NIP-57 | Zap receipt | Low |
| 10000 | NIP-51 | Mute list | Low |
| 10002 | NIP-65 | Relay list metadata | Medium |
| 10003 | NIP-51 | Bookmark list | Low |
| 10013 | NIP-51 | User emoji list | Low |
| 22242 | NIP-42 | Client authentication (relay auth) | Low |
| 27235 | NIP-98 | HTTP authentication | Medium |
| 30023 | NIP-23 | Long-form article | Low |
| 30078 | NIP-78 | Application-specific data | Low |
| 31234 | — | Draft | Low |

### Everything else requires manual approval

Any event kind not in the list above will trigger the approval UI every time. This includes:

- Kind 14 (NIP-17 DMs)
- Kind 13 (Seal)
- Kind 1059 (Gift wrap)
- Kind 24242 (Blossom file storage)
- Kind 30311 (Live event)
- All replaceable/parameterized replaceable events not listed
- Any new/unknown event kinds

### How it works (code path)

When `configureSignPolicy(application, 0, ...)` is called:
1. Each permission in `basicPermissions` is added to the app's permission list
2. `acceptable = true`, `rememberType = ALWAYS`, `acceptUntil = Long.MAX_VALUE / 1000`
3. On subsequent requests, `isRemembered()` checks if a matching permission exists with `acceptUntil > now` — if so, the request is auto-signed

Source: `AmberUtils.configureSignPolicy()` in `AmberUtils.kt`, `basicPermissions` in `BasicPermissions.kt`

---

## Policy 1: "Manually approve each permission" (App-specific only)

**UI Label:** "Manually approve each permission"
**UI Description:** "Ask me to approve each request."

Only available in the per-app sign policy chooser (not in global settings). The user is shown a checklist of permissions during app connection and can select which ones to auto-approve.

### How it works

- `configureSignPolicy(application, 1, ...)` iterates only the permissions the user checked
- Each checked permission is added with `acceptable = true`, `rememberType = ALWAYS`, `acceptUntil = Long.MAX_VALUE / 1000`
- Unchecked permissions will prompt the user every time

---

## Policy 2: "I fully trust this application" (App-specific only)

**UI Label:** "I fully trust this application"
**UI Description:** "Sign automatically every request"

Only available in the per-app sign policy chooser (not in global settings).

### How it works

In `isRemembered()`:
```kotlin
if (signPolicy == 2) {
    return true  // Always auto-approve
}
```

This bypasses **all** permission checks — every request from this app is auto-signed without any UI, regardless of event kind. No permissions are even stored in the database; the policy flag alone controls behavior.

**Exception:** The `ForcePromptChecker` can still override this for dangerous events (checked before the `signPolicy == 2` shortcut).

---

## Global Settings vs Per-App Policies

| Setting | Available Policies | Where Set |
|---|---|---|
| Global default | 0 (Basic), 1 (Manual) | Settings > Signing Policy |
| Per-app | 0 (Basic), 1 (Manual), 2 (Fully Trust) | App connection screen |

The global setting only offers policies 0 and 1. Policy 2 ("Fully Trust") is intentionally only available per-app, since it auto-signs everything.

---

## Potential Concerns

### Kind 0 (Profile) auto-approved in Basic policy

Profile updates (kind 0) can redirect lightning payments (`lud16`/`lud06` fields) and change identity (`name`, `nip05`). Auto-approving these permanently means a compromised app could silently redirect a user's lightning tips. Consider:
- Removing kind 0 from `basicPermissions`, or
- Using the `ForcePromptChecker` to always prompt when `lud16`/`lud06` fields change

### Kind 3 (Follow list) auto-approved in Basic policy

Follow list updates replace the **entire** contact list. A misbehaving app could silently remove follows. Consider:
- Removing kind 3 from `basicPermissions`, or
- Always prompting (our Kind 3 renderer already shows a diff)

### Kind 9734 (Zap request) auto-approved in Basic policy

Zap requests initiate lightning payments. Auto-approving means an app can trigger payments without confirmation. Consider:
- Removing from `basicPermissions`, or
- Adding amount-based thresholds

### Policy 2 bypasses everything

"Fully trust" auto-signs every event kind with no UI. While the label is clear, a user who sets this may not understand the full implications. The `ForcePromptChecker` provides some safety net, but it only covers specific dangerous patterns.

### NIP-04 encryption auto-approved

Both NIP-04 encrypt and decrypt are auto-approved in Basic policy. Since NIP-04 is considered deprecated in favor of NIP-44, and decryption gives apps access to private message content, this could be reconsidered.

---

## ForcePromptChecker

Regardless of sign policy, the `ForcePromptChecker` can force the approval UI for events it deems dangerous. When `ForcePromptChecker.shouldForcePrompt(event)` returns `force = true`, `isRemembered()` returns `null` (show UI), even for policy 2.

This is the safety net that prevents auto-signing of events that match dangerous patterns (e.g., profile updates that change payment fields).
