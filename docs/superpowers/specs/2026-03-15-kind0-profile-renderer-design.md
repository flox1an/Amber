# Kind 0 Profile Diff Renderer

**Date:** 2026-03-15
**Status:** Approved

## Summary

Add a kind-specific event renderer system to Amber's signing approval flow. Start with Kind 0 (Profile/User Metadata) which shows a field-by-field diff view instead of raw JSON, with warnings for cleared fields and payment address changes.

## Architecture

### KindRenderer Dispatcher

A `KindRenderer` composable checks `event.kind` and delegates to kind-specific renderers. Unrecognized kinds fall back to showing raw content (current behavior).

```
KindRenderer(event, account)
  ├── kind == 0 → Kind0ProfileRenderer(event, account)
  └── else     → null (caller shows raw content as before)
```

Returns `true` if it rendered rich content, `false` if the caller should show the default raw view. This lets callers wrap it: "if KindRenderer didn't handle it, show raw text."

### Kind0ProfileRenderer

Parses the Kind 0 event's JSON `content` field and displays a field-by-field diff against the user's current profile.

**Fields displayed:**
- Name
- Display Name
- About
- Picture (small thumbnail + URL)
- NIP-05
- Lightning Address (lud16)

**For each field:**
- Label in `labelSmall`, uppercase, muted color
- New value in `bodyLarge`
- If changed: "was: [old value]" in `bodySmall`, muted, strikethrough
- Changed fields get a subtle `primary`-tinted left border (4dp)
- Cleared fields get a `warning`-tinted left border

**Warning banner:**
If any field that exists in the current profile is missing/empty in the proposed update, show: "This update would clear: [field names]" using `AmberWarningCard` pattern (warning bg, warning border, warning icon).

**Payment warning:**
If `lud16` changes, the Lightning field row gets extra visual emphasis — warning-colored background tint.

**Layout:**
All field rows inside a `Surface(surfaceContainer, 16dp radius)` card. Standard 16dp internal padding. 8dp gap between field rows.

### Current Profile Data

`Account` has `name: MutableStateFlow<String>` and `picture: MutableStateFlow<String>`. For additional fields (about, nip05, lud16), we need to check what's stored. Options:

1. Load from the account's current Kind 0 event if cached anywhere
2. Read from SharedPreferences if stored there
3. Show "unknown" for fields we don't have

We'll use approach 3 as fallback — show the proposed values richly even if we can't diff all fields. The name and picture fields (which we do have) will still show diffs.

To improve this, add `about`, `nip05`, and `lud16` to the profile data stored in SharedPreferences (alongside the existing `name` and `picture`). This requires:
- Adding 3 new `PrefKeys` entries
- Updating `saveToEncryptedStorage` to persist them
- Updating `loadFromEncryptedStorage` to read them
- Adding fields to `Account` class

### Integration

The existing event rendering components (`EventData.kt`, `SignMessage.kt`, `EncryptDecryptData.kt`) currently show `LocalAppIcon` + raw content text. Modify them to:

1. Try `KindRenderer(event, account)` first
2. If it renders (returns true), show the kind-specific view
3. Always keep "Show raw event" toggle available below
4. If KindRenderer returns false, show raw content as before

### Files

| Action | File | Purpose |
|--------|------|---------|
| Create | `ui/components/KindRenderer.kt` | Dispatcher composable |
| Create | `ui/components/Kind0ProfileRenderer.kt` | Profile diff view |
| Modify | `ui/components/EventData.kt` | Call KindRenderer before raw content |
| Modify | `ui/components/SignMessage.kt` | Call KindRenderer before raw content |

### Visual Reference

Based on the signer-research project at `../signer-research/kinds/kind-0-profile.html`:

```
┌──────────────────────────────┐
│  UPDATED PROFILE             │  ← section header
│  ┌──────────────────────────┐│
│  │ NAME                     ││
│  │ Alice (updated)          ││  ← new value
│  │ was: Alice               ││  ← old value, strikethrough
│  ├──────────────────────────┤│
│  │ DISPLAY NAME             ││
│  │ Alice                    ││  ← unchanged, no "was"
│  ├──────────────────────────┤│
│  │ ABOUT                    ││
│  │ Nostr power user...      ││
│  │ was: This is my bio      ││
│  ├──────────────────────────┤│
│  │ PICTURE                  ││
│  │ [thumb] example.com/pic  ││
│  ├──────────────────────────┤│
│  │ ⚠ LIGHTNING              ││  ← warning tint if changed
│  │ new@walletofsatoshi.com  ││
│  │ was: me@getalby.com      ││
│  └──────────────────────────┘│
│                              │
│  ⚠ This update would clear:  │  ← only if fields missing
│    NIP-05                    │
│                              │
│  [▸ Show raw event]          │  ← collapsible
└──────────────────────────────┘
```

## What Stays the Same

- All existing approval functionality (accept/reject/remember)
- Raw JSON always accessible
- All other event kinds render as before
- No changes to the permission system or data flow
