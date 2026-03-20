# Kind Renderers Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add rich signing-approval renderers for all 36 remaining Nostr event kinds defined in the signer-research design, so users see context-appropriate UI instead of raw JSON when approving events.

**Architecture:** Each kind gets a `@Composable` renderer file following the established pattern: label header, Surface cards with parsed tag data, optional relay-fetched context, and collapsible raw JSON. All renderers are registered in `KindRenderer.kt` (dispatcher) and `ApprovalScope.kt` (approval duration config). Kinds are grouped into batches by complexity and risk similarity.

**Tech Stack:** Kotlin, Jetpack Compose, Material3, Coil (avatars), Quartz relay client (event/profile fetching)

---

## Reference: Established Patterns

Every renderer follows this structure (see existing `Kind1NoteRenderer.kt`, `Kind6RepostRenderer.kt` as examples):

```kotlin
@Composable
fun Kind<N><Name>Renderer(
    content: String,
    account: Account,  // omit if not needed
    tags: Array<Array<String>>,
) {
    // 1. Parse tags with: tags.firstOrNull { it.size >= 2 && it[0] == "x" }?.get(1)
    // 2. Optional: LaunchedEffect to fetchEvent() or fetchAuthorProfile() via Dispatchers.IO
    // 3. Column(modifier = Modifier.fillMaxWidth()) {
    //      Text("LABEL", labelSmall, onSurfaceVariant)
    //      Surface(RoundedCornerShape(16.dp), surfaceContainer) { ... }
    //      CollapsibleRawJson(rawJson)
    //    }
}
```

**Key utilities** (from `KindRendererUtils.kt`):
- `fetchEvent(eventId): FetchedEvent?` — fetches event by ID from relays
- `fetchAuthorProfile(pubkeyHex): Pair<String?, String?>?` — returns (displayName, pictureUrl)
- `AuthorIdentityRow(displayName, npub, pictureUrl, avatarSize)` — avatar + name row
- `AuthorAvatar(pictureUrl, size)` — circular avatar with fallback
- `CollapsibleRawJson(rawJson)` — expandable raw event JSON
- `LoadingRow(text)` — spinner + text
- `hexToNpub(hex)` / `shortenNpub(npub)` — key formatting
- `buildRawEventJson(kind, content, tags)` — builds formatted JSON string

**Color conventions:**
- `MaterialTheme.colorScheme.surfaceContainer` — content cards
- `MaterialTheme.colorScheme.primaryContainer` — badges/pills
- `AmberColors.error()` / `AmberColors.errorBg()` — danger (red)
- `AmberColors.warning()` / `AmberColors.warningBg()` — caution (yellow)
- `AmberColors.success()` / `AmberColors.successBg()` — positive (green)

**Registration checklist (every renderer must do all 3):**
1. Create `Kind<N><Name>Renderer.kt` in `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/`
2. Add kind to `hasKindRenderer()` in `KindRenderer.kt`
3. Add kind to `RenderKindContent()` dispatcher in `KindRenderer.kt`
4. Add kind to `getApprovalConfig()` in `ApprovalScope.kt`

**Danger kinds** (5, 62) use `DangerAcceptRejectButtons` — check `isDangerKind()` in `AcceptRejectButtons.kt` if adding new danger kinds.

**Design reference:** Each kind's UI design lives in `/Users/flox/dev/nostr/signer-research/src/data/examples/kind-<N>-<name>.tsx`. The `renderContent()` function shows what to display. The `kind-meta.ts` file has risk tiers and scope defaults.

---

## Batch 1: Social Basics (simple content display)

These are straightforward content renderers — show text/media content with minimal tag parsing.

### Task 1: Kind 9 — Group Chat Message

**Files:**
- Create: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/Kind9ChatRenderer.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/KindRenderer.kt`
- Modify: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/ApprovalScope.kt`

**Design ref:** `signer-research/src/data/examples/kind-9-chat.tsx`

**Rendering logic:**
- Label: "CHAT MESSAGE"
- Parse `e` tag with marker `root` for room/channel reference
- Show message content in Surface card
- If replying (has `e` tag with `reply` marker), show parent context
- Scope: medium risk, 1-hour default

- [ ] **Step 1: Create renderer**

```kotlin
// Label: "CHAT MESSAGE"
// Parse root e-tag for channel context
// Show content in Surface card
// Optional: fetch reply parent via fetchEvent()
// CollapsibleRawJson at bottom
```

- [ ] **Step 2: Register in KindRenderer.kt**

Add `9 -> true` to `hasKindRenderer()` and `9 -> Kind9ChatRenderer(...)` to `RenderKindContent()`.

- [ ] **Step 3: Add approval scope**

```kotlin
9 -> KindApprovalConfig(
    scopes = listOf(
        ApprovalScopeOption("once", "This once"),
        ApprovalScopeOption("app_kind_1h", "This app, 1 hour", recommended = true),
        ApprovalScopeOption("app_kind_always", "Always for this app"),
    ),
    defaultScopeId = "app_kind_1h",
)
```

- [ ] **Step 4: Run lint and tests**

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./gradlew ktlintCheck test --no-daemon
```

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/greenart7c3/nostrsigner/ui/components/Kind9ChatRenderer.kt \
       app/src/main/java/com/greenart7c3/nostrsigner/ui/components/KindRenderer.kt \
       app/src/main/java/com/greenart7c3/nostrsigner/ui/components/ApprovalScope.kt
git commit -m "feat: Add Kind 9 (group chat) renderer"
```

### Task 2: Kind 1311 — Live Chat Message

**Files:**
- Create: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/Kind1311LiveChatRenderer.kt`
- Modify: `KindRenderer.kt`, `ApprovalScope.kt`

**Design ref:** `signer-research/src/data/examples/kind-1311-live-chat.tsx`

**Rendering logic:**
- Label: "LIVE CHAT"
- Parse `a` tag for live event reference (show event title/ID)
- Show chat message content
- If `amount` tag present, show zap amount badge
- Scope: low risk, 1-hour default

- [ ] **Step 1: Create renderer**
- [ ] **Step 2: Register in KindRenderer.kt**
- [ ] **Step 3: Add approval scope** — low risk, 1h default (same pattern as Kind 1)
- [ ] **Step 4: Run lint and tests**
- [ ] **Step 5: Commit** — `"feat: Add Kind 1311 (live chat) renderer"`

### Task 3: Kind 1068 — Poll

**Files:**
- Create: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/Kind1068PollRenderer.kt`
- Modify: `KindRenderer.kt`, `ApprovalScope.kt`

**Design ref:** `signer-research/src/data/examples/kind-1068-poll.tsx`

**Rendering logic:**
- Label: "POLL"
- Content is the poll question (bold)
- Parse `option` tags: each is `["option", index, label]` — display as numbered list
- Parse `valueSetting` for single/multiple choice indicator
- Parse `closedAt` tag for poll end time
- Scope: low risk, 1-hour default

- [ ] **Step 1: Create renderer**
- [ ] **Step 2: Register in KindRenderer.kt**
- [ ] **Step 3: Add approval scope** — low risk, 1h default
- [ ] **Step 4: Run lint and tests**
- [ ] **Step 5: Commit** — `"feat: Add Kind 1068 (poll) renderer"`

### Task 4: Kind 1018 — Poll Response

**Files:**
- Create: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/Kind1018PollResponseRenderer.kt`
- Modify: `KindRenderer.kt`, `ApprovalScope.kt`

**Design ref:** `signer-research/src/data/examples/kind-1018-poll-response.tsx`

**Rendering logic:**
- Label: "POLL VOTE"
- Parse `e` tag for referenced poll event ID
- Parse `response` tags for selected option indices
- Show "Your vote: option X" for each response tag
- Note: "Your vote is public"
- Scope: low risk, 1-hour default

- [ ] **Step 1: Create renderer**
- [ ] **Step 2: Register in KindRenderer.kt**
- [ ] **Step 3: Add approval scope** — low risk, 1h default
- [ ] **Step 4: Run lint and tests**
- [ ] **Step 5: Commit** — `"feat: Add Kind 1018 (poll response) renderer"`

---

## Batch 2: Media & Content (show media metadata)

### Task 5: Kind 20 — Picture Post

**Files:**
- Create: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/Kind20PictureRenderer.kt`
- Modify: `KindRenderer.kt`, `ApprovalScope.kt`

**Design ref:** `signer-research/src/data/examples/kind-20-picture.tsx`

**Rendering logic:**
- Label: "PICTURE"
- Parse `imeta` tags for image URLs
- Show content as caption text
- If `content-warning` tag present, show CW badge
- If `location` or `geohash` tag present, show location warning
- Show tagged users from `p` tags
- Scope: low risk, 1-hour default

- [ ] **Step 1: Create renderer**
- [ ] **Step 2: Register in KindRenderer.kt**
- [ ] **Step 3: Add approval scope** — low risk, 1h default
- [ ] **Step 4: Run lint and tests**
- [ ] **Step 5: Commit** — `"feat: Add Kind 20 (picture) renderer"`

### Task 6: Kind 21 — Video Post

**Files:**
- Create: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/Kind21VideoRenderer.kt`
- Modify: `KindRenderer.kt`, `ApprovalScope.kt`

**Design ref:** `signer-research/src/data/examples/kind-21-video.tsx`

**Rendering logic:**
- Label: "VIDEO"
- Parse `title` tag for video title
- Parse `imeta` tags for thumbnail URL, duration, file size
- Show content as caption
- Show tagged users from `p` tags
- Scope: low risk, 1-hour default

- [ ] **Step 1: Create renderer**
- [ ] **Step 2: Register in KindRenderer.kt**
- [ ] **Step 3: Add approval scope** — low risk, 1h default
- [ ] **Step 4: Run lint and tests**
- [ ] **Step 5: Commit** — `"feat: Add Kind 21 (video) renderer"`

### Task 7: Kind 1063 — File Metadata

**Files:**
- Create: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/Kind1063FileRenderer.kt`
- Modify: `KindRenderer.kt`, `ApprovalScope.kt`

**Design ref:** `signer-research/src/data/examples/kind-1063-file.tsx`

**Rendering logic:**
- Label: "FILE METADATA"
- Parse tags: `url`, `m` (MIME type), `x` (hash), `filename`, `size`, `dim`, `alt`
- Show file icon based on MIME prefix (image/video/audio/document)
- Display filename, type, size (formatted), hash (truncated)
- Scope: medium risk, once default

- [ ] **Step 1: Create renderer**
- [ ] **Step 2: Register in KindRenderer.kt**
- [ ] **Step 3: Add approval scope** — medium risk, once default
- [ ] **Step 4: Run lint and tests**
- [ ] **Step 5: Commit** — `"feat: Add Kind 1063 (file metadata) renderer"`

### Task 8: Kind 30023 — Long-form Article

**Files:**
- Create: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/Kind30023ArticleRenderer.kt`
- Modify: `KindRenderer.kt`, `ApprovalScope.kt`

**Design ref:** `signer-research/src/data/examples/kind-30023-article.tsx`

**Rendering logic:**
- Label: "ARTICLE"
- Parse `title` tag — show as bold header
- Parse `published_at` tag — show formatted date
- Show word count from content length
- Show content excerpt (first 300 chars of markdown content)
- Parse `image` tag for featured image reference (show URL, not image)
- Scope: medium risk, once default

- [ ] **Step 1: Create renderer**
- [ ] **Step 2: Register in KindRenderer.kt**
- [ ] **Step 3: Add approval scope** — medium risk, once default
- [ ] **Step 4: Run lint and tests**
- [ ] **Step 5: Commit** — `"feat: Add Kind 30023 (article) renderer"`

### Task 9: Kind 31234 — Draft

**Files:**
- Create: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/Kind31234DraftRenderer.kt`
- Modify: `KindRenderer.kt`, `ApprovalScope.kt`

**Design ref:** `signer-research/src/data/examples/kind-31234-draft.tsx`

**Rendering logic:**
- Label: "DRAFT"
- Parse `k` tag for target kind (what kind this draft will become)
- Parse `title` tag
- Show content preview (truncated to 300 chars) with word count
- Parse `d` tag for draft ID
- Show hashtags from `t` tags as pills
- Scope: low risk, 1-hour default

- [ ] **Step 1: Create renderer**
- [ ] **Step 2: Register in KindRenderer.kt**
- [ ] **Step 3: Add approval scope** — low risk, 1h default
- [ ] **Step 4: Run lint and tests**
- [ ] **Step 5: Commit** — `"feat: Add Kind 31234 (draft) renderer"`

---

## Batch 3: Private Messaging (encrypted content, show metadata only)

### Task 10: Kind 4 — Legacy Encrypted DM

**Files:**
- Create: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/Kind4LegacyDmRenderer.kt`
- Modify: `KindRenderer.kt`, `ApprovalScope.kt`

**Design ref:** `signer-research/src/data/examples/kind-4-legacy-dm.tsx`

**Rendering logic:**
- Label: "LEGACY DM"
- **Warning banner** (amber/yellow): "NIP-04 is deprecated. Metadata (who you message and when) is visible to relays."
- Parse `p` tag for recipient — show AuthorIdentityRow
- Show encrypted content indicator (lock icon + "Encrypted message")
- Do NOT show decrypted content
- Scope: high risk, once only

- [ ] **Step 1: Create renderer**
- [ ] **Step 2: Register in KindRenderer.kt**
- [ ] **Step 3: Add approval scope** — high risk, once only
- [ ] **Step 4: Run lint and tests**
- [ ] **Step 5: Commit** — `"feat: Add Kind 4 (legacy DM) renderer with deprecation warning"`

### Task 11: Kind 13 — Seal

**Files:**
- Create: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/Kind13SealRenderer.kt`
- Modify: `KindRenderer.kt`, `ApprovalScope.kt`

**Design ref:** `signer-research/src/data/examples/kind-13-seal.tsx`

**Rendering logic:**
- Label: "SEAL"
- Warning: "This event is signed with your real key and binds your identity to the enclosed message."
- If tags are non-empty, show warning: "Seal events should have empty tags."
- Parse `p` tag for recipient
- Show encrypted content indicator
- Scope: medium risk, 1-hour default

- [ ] **Step 1: Create renderer**
- [ ] **Step 2: Register in KindRenderer.kt**
- [ ] **Step 3: Add approval scope** — medium risk, 1h default
- [ ] **Step 4: Run lint and tests**
- [ ] **Step 5: Commit** — `"feat: Add Kind 13 (seal) renderer"`

### Task 12: Kind 14 — Direct Message (NIP-17)

**Files:**
- Create: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/Kind14DmRenderer.kt`
- Modify: `KindRenderer.kt`, `ApprovalScope.kt`

**Design ref:** `signer-research/src/data/examples/kind-14-dm.tsx`

**Rendering logic:**
- Label: "DIRECT MESSAGE"
- Parse `p` tag for recipient — show AuthorIdentityRow
- Show message preview (content is the message text inside the seal)
- Scope: medium risk, 1-hour default

- [ ] **Step 1: Create renderer**
- [ ] **Step 2: Register in KindRenderer.kt**
- [ ] **Step 3: Add approval scope** — medium risk, 1h default
- [ ] **Step 4: Run lint and tests**
- [ ] **Step 5: Commit** — `"feat: Add Kind 14 (DM) renderer"`

### Task 13: Kind 1059 — Gift Wrap

**Files:**
- Create: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/Kind1059GiftWrapRenderer.kt`
- Modify: `KindRenderer.kt`, `ApprovalScope.kt`

**Design ref:** `signer-research/src/data/examples/kind-1059-gift-wrap.tsx`

**Rendering logic:**
- Label: "GIFT WRAP"
- Explanation text: "This wraps a sealed message using a randomized throwaway key for metadata protection."
- Parse `p` tag for recipient — show AuthorIdentityRow
- Show encrypted content indicator
- Note: "Timestamp is randomized for privacy"
- Scope: medium risk, 1-hour default

- [ ] **Step 1: Create renderer**
- [ ] **Step 2: Register in KindRenderer.kt**
- [ ] **Step 3: Add approval scope** — medium risk, 1h default
- [ ] **Step 4: Run lint and tests**
- [ ] **Step 5: Commit** — `"feat: Add Kind 1059 (gift wrap) renderer"`

---

## Batch 4: Moderation & Labels

### Task 14: Kind 1984 — Report

**Files:**
- Create: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/Kind1984ReportRenderer.kt`
- Modify: `KindRenderer.kt`, `ApprovalScope.kt`

**Design ref:** `signer-research/src/data/examples/kind-1984-report.tsx`

**Rendering logic:**
- Label: "REPORT"
- Parse `p` or `e` tags — the second element of each may contain report type (spam, impersonation, nudity, illegal, profanity)
- Show report type as colored badge (use `AmberColors.warning()` background)
- Show reported entity: either AuthorIdentityRow (p tag) or event ID (e tag)
- Content is the report reason — display if non-empty
- Scope: medium risk, once default

- [ ] **Step 1: Create renderer**
- [ ] **Step 2: Register in KindRenderer.kt**
- [ ] **Step 3: Add approval scope** — medium risk, once default
- [ ] **Step 4: Run lint and tests**
- [ ] **Step 5: Commit** — `"feat: Add Kind 1984 (report) renderer"`

### Task 15: Kind 1985 — Label

**Files:**
- Create: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/Kind1985LabelRenderer.kt`
- Modify: `KindRenderer.kt`, `ApprovalScope.kt`

**Design ref:** `signer-research/src/data/examples/kind-1985-label.tsx`

**Rendering logic:**
- Label: "LABEL"
- Parse `L` tag for namespace
- Parse `l` tags for label values — show as badge pills
- Parse `e` or `p` tag for target (labeled event or user)
- Content is optional reason text
- Scope: medium risk, 1-hour default

- [ ] **Step 1: Create renderer**
- [ ] **Step 2: Register in KindRenderer.kt**
- [ ] **Step 3: Add approval scope** — medium risk, 1h default
- [ ] **Step 4: Run lint and tests**
- [ ] **Step 5: Commit** — `"feat: Add Kind 1985 (label) renderer"`

---

## Batch 5: Financial (show amounts, high friction)

### Task 16: Kind 9734 — Zap Request

**Files:**
- Create: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/Kind9734ZapRequestRenderer.kt`
- Modify: `KindRenderer.kt`, `ApprovalScope.kt`

**Design ref:** `signer-research/src/data/examples/kind-9734-zap-request.tsx`

**Rendering logic:**
- Label: "ZAP REQUEST"
- Parse `amount` tag — display in sats (divide by 1000 from millisats)
- Content is the zap message — show if non-empty
- Parse `p` tag for recipient — show AuthorIdentityRow with "Zapping" label
- Parse `e` tag for zapped event — show event ID reference
- Scope: financial risk, once default

- [ ] **Step 1: Create renderer**

Add a `formatSats(millisats: Long): String` helper to `KindRendererUtils.kt`:
```kotlin
internal fun formatSats(millisats: Long): String {
    val sats = millisats / 1000
    return when {
        sats >= 1_000_000 -> "${sats / 1_000_000}.${(sats % 1_000_000) / 10_000}M sats"
        sats >= 1_000 -> "${sats / 1_000}.${(sats % 1_000) / 10}k sats"
        else -> "$sats sats"
    }
}
```

- [ ] **Step 2: Register in KindRenderer.kt**
- [ ] **Step 3: Add approval scope** — financial risk, once only
- [ ] **Step 4: Run lint and tests**
- [ ] **Step 5: Commit** — `"feat: Add Kind 9734 (zap request) renderer with sat formatting"`

### Task 17: Kind 9735 — Zap Receipt

**Files:**
- Create: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/Kind9735ZapReceiptRenderer.kt`
- Modify: `KindRenderer.kt`, `ApprovalScope.kt`

**Design ref:** `signer-research/src/data/examples/kind-9735-zap-receipt.tsx`

**Rendering logic:**
- Label: "ZAP RECEIPT"
- Success badge: "Payment Confirmed" (green)
- Parse `bolt11` tag — show truncated invoice reference
- Parse `description` tag (JSON string) for zap request details (amount, message)
- Parse `p` tag (recipient) and `P` tag (sender) — show both identity rows
- Scope: low risk (confirmational), 1-hour default

- [ ] **Step 1: Create renderer** (reuses `formatSats` from Task 16)
- [ ] **Step 2: Register in KindRenderer.kt**
- [ ] **Step 3: Add approval scope** — low risk, 1h default
- [ ] **Step 4: Run lint and tests**
- [ ] **Step 5: Commit** — `"feat: Add Kind 9735 (zap receipt) renderer"`

### Task 18: Kind 9041 — Zap Goal

**Files:**
- Create: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/Kind9041ZapGoalRenderer.kt`
- Modify: `KindRenderer.kt`, `ApprovalScope.kt`

**Design ref:** `signer-research/src/data/examples/kind-9041-zap-goal.tsx`

**Rendering logic:**
- Label: "ZAP GOAL"
- Content is the goal description
- Parse `amount` tag — show target amount in sats
- Parse `closed_at` tag — show closing time if present
- Parse `zap` tags for beneficiary list with percentage splits
- Scope: low risk, once default

- [ ] **Step 1: Create renderer**
- [ ] **Step 2: Register in KindRenderer.kt**
- [ ] **Step 3: Add approval scope** — low risk, once default
- [ ] **Step 4: Run lint and tests**
- [ ] **Step 5: Commit** — `"feat: Add Kind 9041 (zap goal) renderer"`

### Task 19: Kind 23194 — NWC Wallet Request

**Files:**
- Create: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/Kind23194NwcRenderer.kt`
- Modify: `KindRenderer.kt`, `ApprovalScope.kt`

**Design ref:** `signer-research/src/data/examples/kind-23194-nwc-request.tsx`

**Rendering logic:**
- Label: "WALLET REQUEST"
- **NWC content is encrypted** — we can only show metadata
- Parse `p` tag for wallet service pubkey
- Show encrypted content indicator
- Warning: "This authorizes a wallet operation. The content is encrypted to the wallet service."
- Scope: financial risk, once default

- [ ] **Step 1: Create renderer**
- [ ] **Step 2: Register in KindRenderer.kt**
- [ ] **Step 3: Add approval scope** — financial risk, once only
- [ ] **Step 4: Run lint and tests**
- [ ] **Step 5: Commit** — `"feat: Add Kind 23194 (NWC request) renderer"`

### Task 20: Kind 7375 — Cashu Wallet Tokens

**Files:**
- Create: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/Kind7375CashuRenderer.kt`
- Modify: `KindRenderer.kt`, `ApprovalScope.kt`

**Design ref:** `signer-research/src/data/examples/kind-7375-cashu.tsx`

**Rendering logic:**
- Label: "CASHU TOKENS"
- **Content is encrypted** — show warning about wallet token update
- Warning: "This updates your Cashu wallet tokens. The content is encrypted."
- Show wallet indicator icon
- Scope: financial risk, once default

- [ ] **Step 1: Create renderer**
- [ ] **Step 2: Register in KindRenderer.kt**
- [ ] **Step 3: Add approval scope** — financial risk, once only
- [ ] **Step 4: Run lint and tests**
- [ ] **Step 5: Commit** — `"feat: Add Kind 7375 (Cashu tokens) renderer"`

---

## Batch 6: Server Auth (show target server + action)

### Task 21: Kind 27235 — HTTP Auth

**Files:**
- Create: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/Kind27235HttpAuthRenderer.kt`
- Modify: `KindRenderer.kt`, `ApprovalScope.kt`

**Design ref:** `signer-research/src/data/examples/kind-27235-http-auth.tsx`

**Rendering logic:**
- Label: "HTTP AUTH"
- Parse `u` tag for target URL
- Parse `method` tag for HTTP method (GET/POST/DELETE) — show as colored badge
- Extract domain from URL for display
- Scope: server-scoped, 1-hour default

- [ ] **Step 1: Create renderer**
- [ ] **Step 2: Register in KindRenderer.kt**
- [ ] **Step 3: Add approval scope** — medium risk, 1h default
- [ ] **Step 4: Run lint and tests**
- [ ] **Step 5: Commit** — `"feat: Add Kind 27235 (HTTP auth) renderer"`

### Task 22: Kind 24242 — Blossom Auth

**Files:**
- Create: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/Kind24242BlossomRenderer.kt`
- Modify: `KindRenderer.kt`, `ApprovalScope.kt`

**Design ref:** `signer-research/src/data/examples/kind-24242-blossom.tsx`

**Rendering logic:**
- Label: "BLOSSOM AUTH"
- Parse `t` tag for action (upload, delete, list)
- Parse `server` tag for target server domain
- If no `server` tag, show warning: "No server specified — this token could be used on ANY server"
- Parse `x` tag for file hash (truncated)
- Parse `expiration` tag for token expiry
- Scope: medium risk, 1-hour default

- [ ] **Step 1: Create renderer**
- [ ] **Step 2: Register in KindRenderer.kt**
- [ ] **Step 3: Add approval scope** — medium risk, 1h default
- [ ] **Step 4: Run lint and tests**
- [ ] **Step 5: Commit** — `"feat: Add Kind 24242 (Blossom auth) renderer"`

---

## Batch 7: Badges & Communities

### Task 23: Kind 8 — Badge Award

**Files:**
- Create: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/Kind8BadgeAwardRenderer.kt`
- Modify: `KindRenderer.kt`, `ApprovalScope.kt`

**Design ref:** `signer-research/src/data/examples/kind-8-badge-award.tsx`

**Rendering logic:**
- Label: "BADGE AWARD"
- Parse `a` tag for badge definition reference (kind:pubkey:d-tag)
- Parse `p` tags for recipients — show count + list of identity rows (cap at 10)
- Scope: medium risk, once default

- [ ] **Step 1: Create renderer**
- [ ] **Step 2: Register in KindRenderer.kt**
- [ ] **Step 3: Add approval scope** — medium risk, once default
- [ ] **Step 4: Run lint and tests**
- [ ] **Step 5: Commit** — `"feat: Add Kind 8 (badge award) renderer"`

### Task 24: Kind 30008 — Profile Badges

**Files:**
- Create: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/Kind30008ProfileBadgesRenderer.kt`
- Modify: `KindRenderer.kt`, `ApprovalScope.kt`

**Design ref:** `signer-research/src/data/examples/kind-30008-profile-badges.tsx`

**Rendering logic:**
- Label: "PROFILE BADGES"
- Warning: "This replaces your entire displayed badge list."
- Parse `a` tags for badge references — list badge identifiers
- Parse `e` tags for corresponding award events
- If no `a` tags, show clearing warning: "This will remove all badges from your profile."
- Scope: medium risk, once default

- [ ] **Step 1: Create renderer**
- [ ] **Step 2: Register in KindRenderer.kt**
- [ ] **Step 3: Add approval scope** — medium risk, once default
- [ ] **Step 4: Run lint and tests**
- [ ] **Step 5: Commit** — `"feat: Add Kind 30008 (profile badges) renderer"`

### Task 25: Kind 30009 — Badge Definition

**Files:**
- Create: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/Kind30009BadgeDefRenderer.kt`
- Modify: `KindRenderer.kt`, `ApprovalScope.kt`

**Design ref:** `signer-research/src/data/examples/kind-30009-badge-definition.tsx`

**Rendering logic:**
- Label: "BADGE DEFINITION"
- Parse `name` tag for badge name
- Parse `description` tag for badge description
- Parse `image`/`thumb` tag for badge image URL (display URL text, not load image)
- Parse `d` tag for badge identifier
- Scope: medium risk, once default

- [ ] **Step 1: Create renderer**
- [ ] **Step 2: Register in KindRenderer.kt**
- [ ] **Step 3: Add approval scope** — medium risk, once default
- [ ] **Step 4: Run lint and tests**
- [ ] **Step 5: Commit** — `"feat: Add Kind 30009 (badge definition) renderer"`

### Task 26: Kind 34550 — Community Definition

**Files:**
- Create: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/Kind34550CommunityRenderer.kt`
- Modify: `KindRenderer.kt`, `ApprovalScope.kt`

**Design ref:** `signer-research/src/data/examples/kind-34550-community.tsx`

**Rendering logic:**
- Label: "COMMUNITY"
- Parse `d` tag for community name
- Parse `description` tag
- Parse `rules` tag — show if present
- Parse `p` tags for moderators with roles
- Parse `relay` tags for relay configuration
- Scope: medium risk, once default

- [ ] **Step 1: Create renderer**
- [ ] **Step 2: Register in KindRenderer.kt**
- [ ] **Step 3: Add approval scope** — medium risk, once default
- [ ] **Step 4: Run lint and tests**
- [ ] **Step 5: Commit** — `"feat: Add Kind 34550 (community) renderer"`

### Task 27: Kind 4550 — Community Approved Post

**Files:**
- Create: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/Kind4550CommunityApprovedRenderer.kt`
- Modify: `KindRenderer.kt`, `ApprovalScope.kt`

**Design ref:** `signer-research/src/data/examples/kind-4550-community-approved.tsx`

**Rendering logic:**
- Label: "COMMUNITY APPROVAL"
- Moderator banner at top
- Parse `a` tag for community reference
- Parse `e` tag for approved event ID
- Parse `p` tag for original author
- Parse `k` tag for event kind
- Scope: medium risk, 1-hour default

- [ ] **Step 1: Create renderer**
- [ ] **Step 2: Register in KindRenderer.kt**
- [ ] **Step 3: Add approval scope** — medium risk, 1h default
- [ ] **Step 4: Run lint and tests**
- [ ] **Step 5: Commit** — `"feat: Add Kind 4550 (community approval) renderer"`

---

## Batch 8: Lists & App Data

### Task 28: Kind 10003 — Bookmark List

**Files:**
- Create: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/Kind10003BookmarkRenderer.kt`
- Modify: `KindRenderer.kt`, `ApprovalScope.kt`

**Design ref:** `signer-research/src/data/examples/kind-10003-bookmarks.tsx`

**Rendering logic:**
- Label: "BOOKMARKS"
- Count `e` tags (notes), `a` tags (articles), `t` tags (hashtags)
- Show total bookmark count
- Show first 5 entries (shortened IDs)
- Scope: low risk, 1-hour default
- Note: Could reuse `ListOverwriteRenderer` pattern but bookmarks are lower risk than mute/relay lists

- [ ] **Step 1: Create renderer**
- [ ] **Step 2: Register in KindRenderer.kt**
- [ ] **Step 3: Add approval scope** — low risk, 1h default
- [ ] **Step 4: Run lint and tests**
- [ ] **Step 5: Commit** — `"feat: Add Kind 10003 (bookmarks) renderer"`

### Task 29: Kind 10013 — Custom Emoji List

**Files:**
- Create: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/Kind10013EmojiRenderer.kt`
- Modify: `KindRenderer.kt`, `ApprovalScope.kt`

**Design ref:** `signer-research/src/data/examples/kind-10013-emoji.tsx`

**Rendering logic:**
- Label: "CUSTOM EMOJI"
- Parse `emoji` tags: `["emoji", shortcode, url]`
- Show emoji count
- List emoji shortcodes (`:shortcode:` format)
- Scope: low risk, 1-hour default

- [ ] **Step 1: Create renderer**
- [ ] **Step 2: Register in KindRenderer.kt**
- [ ] **Step 3: Add approval scope** — low risk, 1h default
- [ ] **Step 4: Run lint and tests**
- [ ] **Step 5: Commit** — `"feat: Add Kind 10013 (custom emoji) renderer"`

### Task 30: Kind 30078 — App-Specific Data

**Files:**
- Create: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/Kind30078AppDataRenderer.kt`
- Modify: `KindRenderer.kt`, `ApprovalScope.kt`

**Design ref:** `signer-research/src/data/examples/kind-30078-app-data.tsx`

**Rendering logic:**
- Label: "APP DATA"
- Parse `d` tag for app namespace identifier
- Show content size (bytes/KB)
- Show content preview (first 200 chars, monospace) — may be JSON
- Scope: low risk, 1-hour default

- [ ] **Step 1: Create renderer**
- [ ] **Step 2: Register in KindRenderer.kt**
- [ ] **Step 3: Add approval scope** — low risk, 1h default
- [ ] **Step 4: Run lint and tests**
- [ ] **Step 5: Commit** — `"feat: Add Kind 30078 (app data) renderer"`

---

## Batch 9: Commerce

### Task 31: Kind 30018 — Product Listing

**Files:**
- Create: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/Kind30018ProductRenderer.kt`
- Modify: `KindRenderer.kt`, `ApprovalScope.kt`

**Design ref:** `signer-research/src/data/examples/kind-30018-product-listing.tsx`

**Rendering logic:**
- Label: "PRODUCT LISTING"
- Parse `title` tag for product name
- Parse `price` tag for price (format: `["price", amount, currency, frequency]`)
- Parse `summary` tag for description excerpt
- Parse `image` tag for product image URL (show URL text)
- Parse `d` tag for product ID
- Scope: financial risk, once default

- [ ] **Step 1: Create renderer**
- [ ] **Step 2: Register in KindRenderer.kt**
- [ ] **Step 3: Add approval scope** — financial risk, once only
- [ ] **Step 4: Run lint and tests**
- [ ] **Step 5: Commit** — `"feat: Add Kind 30018 (product listing) renderer"`

### Task 32: Kind 30020 — Product Sold / Auction

**Files:**
- Create: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/Kind30020ProductSoldRenderer.kt`
- Modify: `KindRenderer.kt`, `ApprovalScope.kt`

**Design ref:** `signer-research/src/data/examples/kind-30020-product-sold.tsx`

**Rendering logic:**
- Label: "PRODUCT SOLD"
- Parse `status` tag — show as badge (sold/closed)
- Parse `a` tag for product listing reference
- Parse `p` tag for buyer identity
- Parse `amount` tag for sale amount
- Content is optional transaction message
- Warning: "This creates a permanent transaction record."
- Scope: financial risk, once default

- [ ] **Step 1: Create renderer**
- [ ] **Step 2: Register in KindRenderer.kt**
- [ ] **Step 3: Add approval scope** — financial risk, once only
- [ ] **Step 4: Run lint and tests**
- [ ] **Step 5: Commit** — `"feat: Add Kind 30020 (product sold) renderer"`

---

## Batch 10: Events & Handlers

### Task 33: Kind 30311 — Live Event

**Files:**
- Create: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/Kind30311LiveEventRenderer.kt`
- Modify: `KindRenderer.kt`, `ApprovalScope.kt`

**Design ref:** `signer-research/src/data/examples/kind-30311-live-event.tsx`

**Rendering logic:**
- Label: "LIVE EVENT"
- Parse `title` tag
- Parse `status` tag — show as colored badge (live=green, ended=gray)
- Parse `streaming` tag for stream URL
- Parse `p` tags with roles (host, speaker, participant)
- Parse `current_participants` tag for viewer count
- Scope: low risk, 1-hour default

- [ ] **Step 1: Create renderer**
- [ ] **Step 2: Register in KindRenderer.kt**
- [ ] **Step 3: Add approval scope** — low risk, 1h default
- [ ] **Step 4: Run lint and tests**
- [ ] **Step 5: Commit** — `"feat: Add Kind 30311 (live event) renderer"`

### Task 34: Kind 31923 — Calendar Event

**Files:**
- Create: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/Kind31923CalendarRenderer.kt`
- Modify: `KindRenderer.kt`, `ApprovalScope.kt`

**Design ref:** `signer-research/src/data/examples/kind-31923-calendar.tsx`

**Rendering logic:**
- Label: "CALENDAR EVENT"
- Parse `title` tag
- Parse `start` and `end` tags — format as readable date/time
- Parse `start_tzid` for timezone
- Parse `location` tag — show with location icon
- If `g` (geohash) tag present, show location warning
- Parse `summary` or content for description
- Parse `p` tags for participants (with RSVP status if present)
- Scope: low risk, 1-hour default

- [ ] **Step 1: Create renderer**
- [ ] **Step 2: Register in KindRenderer.kt**
- [ ] **Step 3: Add approval scope** — low risk, 1h default
- [ ] **Step 4: Run lint and tests**
- [ ] **Step 5: Commit** — `"feat: Add Kind 31923 (calendar event) renderer"`

### Task 35: Kind 30382 — Handler Declaration

**Files:**
- Create: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/Kind30382HandlerRenderer.kt`
- Modify: `KindRenderer.kt`, `ApprovalScope.kt`

**Design ref:** `signer-research/src/data/examples/kind-30382-handler-declaration.tsx`

**Rendering logic:**
- Label: "HANDLER DECLARATION"
- **Warning if sensitive kinds handled** (0, 3, 4, 5, 13, 14, 62, etc.): "This handler registers to process sensitive event kinds."
- Parse `d` tag for handler identifier
- Parse `k` tags for handled event kinds — list them, highlight sensitive ones in red
- Parse `web` tag for handler URL template
- Scope: high risk, once default

- [ ] **Step 1: Create renderer**
- [ ] **Step 2: Register in KindRenderer.kt**
- [ ] **Step 3: Add approval scope** — high risk, once only
- [ ] **Step 4: Run lint and tests**
- [ ] **Step 5: Commit** — `"feat: Add Kind 30382 (handler declaration) renderer"`

### Task 36: Kind 31990 — Handler Recommendation

**Files:**
- Create: `app/src/main/java/com/greenart7c3/nostrsigner/ui/components/Kind31990HandlerRecRenderer.kt`
- Modify: `KindRenderer.kt`, `ApprovalScope.kt`

**Design ref:** `signer-research/src/data/examples/kind-31990-handler-recommendation.tsx`

**Rendering logic:**
- Label: "HANDLER RECOMMENDATION"
- Warning: "This publicly recommends an app to handle events for other users."
- Parse `a` tag for handler reference (kind:pubkey:d-tag)
- Parse `d` tag for recommendation identifier
- Content is recommendation text
- Scope: high risk, once default

- [ ] **Step 1: Create renderer**
- [ ] **Step 2: Register in KindRenderer.kt**
- [ ] **Step 3: Add approval scope** — high risk, once only
- [ ] **Step 4: Run lint and tests**
- [ ] **Step 5: Commit** — `"feat: Add Kind 31990 (handler recommendation) renderer"`

---

## Final Task: Verification

### Task 37: Final lint, test, and review

- [ ] **Step 1: Run full lint**

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./gradlew ktlintCheck
```

- [ ] **Step 2: Run full tests**

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./gradlew test --no-daemon
```

- [ ] **Step 3: Verify all kinds registered**

Check that `hasKindRenderer()` in `KindRenderer.kt` includes all 50 kinds:
0, 1, 3, 4, 5, 6, 7, 8, 9, 13, 14, 20, 21, 62, 1018, 1059, 1063, 1068, 1111, 1311, 1984, 1985, 4550, 7375, 9041, 9734, 9735, 9802, 10000, 10002, 10003, 10013, 22242 (has own screen), 23194, 24242, 27235, 30000, 30003, 30008, 30009, 30018, 30020, 30023, 30078, 30311, 30315, 30382, 31234, 31923, 31990, 34550

Note: Kind 22242 already has `BunkerRelayAuthScreen` — it does NOT need a kind renderer.

- [ ] **Step 4: Build debug APK**

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./gradlew assembleDebug
```
