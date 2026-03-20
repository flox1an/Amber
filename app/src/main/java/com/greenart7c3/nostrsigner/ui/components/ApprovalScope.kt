package com.greenart7c3.nostrsigner.ui.components

data class ApprovalScopeOption(
    val id: String,
    val label: String,
    val recommended: Boolean = false,
    /** Optional scoped type suffix. When set, the permission is saved with this type instead of the base type. */
    val scopedTypeSuffix: String? = null,
)

data class KindApprovalConfig(
    val scopes: List<ApprovalScopeOption>,
    val defaultScopeId: String,
)

/**
 * Returns kind-specific approval scope configuration.
 * Null means fall back to the existing RememberMyChoice behavior.
 *
 * @param tags Optional event tags used to derive scope labels for
 *             addressable events (d-tag) and Blossom auth (t-tag).
 */
fun getApprovalConfig(kind: Int, tags: Array<Array<String>>? = null): KindApprovalConfig? = when (kind) {
    // Kind 0: Profile updates — high risk, always require explicit approval
    0 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once", recommended = true),
        ),
        defaultScopeId = "once",
    )

    // Kind 1: Notes — low risk, high frequency
    1 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once"),
            ApprovalScopeOption("app_kind_1h", "This app, 1 hour", recommended = true),
            ApprovalScopeOption("app_kind_always", "Always for this app"),
        ),
        defaultScopeId = "app_kind_1h",
    )

    // Kind 4: Legacy encrypted DM — high risk, NIP-04 deprecated, metadata visible to relays
    4 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once", recommended = true),
        ),
        defaultScopeId = "once",
    )

    // Kind 3: Follow list — medium risk, replaces entire contact list
    3 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once", recommended = true),
        ),
        defaultScopeId = "once",
    )

    // Kind 5: Deletion — high risk, irreversible
    5 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once", recommended = true),
        ),
        defaultScopeId = "once",
    )

    // Kind 20: Picture post — low risk, high frequency
    20 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once"),
            ApprovalScopeOption("app_kind_1h", "This app, 1 hour", recommended = true),
            ApprovalScopeOption("app_kind_always", "Always for this app"),
        ),
        defaultScopeId = "app_kind_1h",
    )

    // Kind 21: Video post — low risk, high frequency
    21 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once"),
            ApprovalScopeOption("app_kind_1h", "This app, 1 hour", recommended = true),
            ApprovalScopeOption("app_kind_always", "Always for this app"),
        ),
        defaultScopeId = "app_kind_1h",
    )

    // Kind 6: Repost — low risk, often done in bursts
    6 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once"),
            ApprovalScopeOption("app_kind_1h", "This app, 1 hour", recommended = true),
            ApprovalScopeOption("app_kind_always", "Always for this app"),
        ),
        defaultScopeId = "app_kind_1h",
    )

    // Kind 1063: File metadata — medium risk
    1063 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once", recommended = true),
        ),
        defaultScopeId = "once",
    )

    // Kind 30018: Product Listing — financial risk, once default
    30018 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once", recommended = true),
        ),
        defaultScopeId = "once",
    )

    // Kind 30020: Product Sold / Auction — financial risk, permanent transaction record
    30020 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once", recommended = true),
        ),
        defaultScopeId = "once",
    )

    // Kind 30023: Long-form article — medium risk
    30023 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once", recommended = true),
        ),
        defaultScopeId = "once",
    )

    // Kind 31234: Draft — low risk, frequent saves
    31234 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once"),
            ApprovalScopeOption("app_kind_1h", "This app, 1 hour", recommended = true),
            ApprovalScopeOption("app_kind_always", "Always for this app"),
        ),
        defaultScopeId = "app_kind_1h",
    )

    // Kind 62: Vanish — CRITICAL risk, only "This once" ever
    62 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once", recommended = true),
        ),
        defaultScopeId = "once",
    )

    // Kind 9: Group chat message — medium risk, ongoing participation
    9 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once"),
            ApprovalScopeOption("app_kind_1h", "This app, 1 hour", recommended = true),
            ApprovalScopeOption("app_kind_always", "Always for this app"),
        ),
        defaultScopeId = "app_kind_1h",
    )

    // Kind 7: Reaction — low risk, very high frequency
    7 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once"),
            ApprovalScopeOption("app_kind_1h", "This app, 1 hour", recommended = true),
            ApprovalScopeOption("app_kind_always", "Always for this app"),
        ),
        defaultScopeId = "app_kind_1h",
    )

    // Kind 1018: Poll response — low risk, public vote
    1018 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once"),
            ApprovalScopeOption("app_kind_1h", "This app, 1 hour", recommended = true),
            ApprovalScopeOption("app_kind_always", "Always for this app"),
        ),
        defaultScopeId = "app_kind_1h",
    )

    // Kind 1068: Poll — low risk, creating a poll
    1068 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once"),
            ApprovalScopeOption("app_kind_1h", "This app, 1 hour", recommended = true),
            ApprovalScopeOption("app_kind_always", "Always for this app"),
        ),
        defaultScopeId = "app_kind_1h",
    )

    // Kind 1111: Comment — low risk, similar to notes
    1111 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once"),
            ApprovalScopeOption("app_kind_1h", "This app, 1 hour", recommended = true),
            ApprovalScopeOption("app_kind_always", "Always for this app"),
        ),
        defaultScopeId = "app_kind_1h",
    )

    // Kind 1311: Live chat message — low risk, real-time chat
    1311 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once"),
            ApprovalScopeOption("app_kind_1h", "This app, 1 hour", recommended = true),
            ApprovalScopeOption("app_kind_always", "Always for this app"),
        ),
        defaultScopeId = "app_kind_1h",
    )

    // Kind 9802: Highlight — low risk, reading session activity
    9802 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once"),
            ApprovalScopeOption("app_kind_1h", "This app, 1 hour", recommended = true),
            ApprovalScopeOption("app_kind_always", "Always for this app"),
        ),
        defaultScopeId = "app_kind_1h",
    )

    // Kind 30315: User status — low risk, often automatic (e.g. music players)
    30315 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once"),
            ApprovalScopeOption("app_kind_4h", "This app, 4 hours", recommended = true),
            ApprovalScopeOption("app_kind_always", "Always for this app"),
        ),
        defaultScopeId = "app_kind_4h",
    )

    // Kind 13: Seal — medium risk, binds real identity to message
    13 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once"),
            ApprovalScopeOption("app_kind_1h", "This app, 1 hour", recommended = true),
            ApprovalScopeOption("app_kind_always", "Always for this app"),
        ),
        defaultScopeId = "app_kind_1h",
    )

    // Kind 14: Direct message (NIP-17) — medium risk, private messaging
    14 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once"),
            ApprovalScopeOption("app_kind_1h", "This app, 1 hour", recommended = true),
            ApprovalScopeOption("app_kind_always", "Always for this app"),
        ),
        defaultScopeId = "app_kind_1h",
    )

    // Kind 1059: Gift wrap — medium risk, metadata protection via throwaway key
    1059 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once"),
            ApprovalScopeOption("app_kind_1h", "This app, 1 hour", recommended = true),
            ApprovalScopeOption("app_kind_always", "Always for this app"),
        ),
        defaultScopeId = "app_kind_1h",
    )

    // Kind 1984: Report — medium risk, one-off moderation action
    1984 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once", recommended = true),
        ),
        defaultScopeId = "once",
    )

    // Kind 1985: Label — medium risk, labeling content or users
    1985 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once"),
            ApprovalScopeOption("app_kind_1h", "This app, 1 hour", recommended = true),
            ApprovalScopeOption("app_kind_always", "Always for this app"),
        ),
        defaultScopeId = "app_kind_1h",
    )

    // Kind 10003: Bookmark List — low risk, 1-hour default
    10003 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once"),
            ApprovalScopeOption("app_kind_1h", "This app, 1 hour", recommended = true),
            ApprovalScopeOption("app_kind_always", "Always for this app"),
        ),
        defaultScopeId = "app_kind_1h",
    )

    // Kind 10013: Custom Emoji List — low risk, 1-hour default
    10013 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once"),
            ApprovalScopeOption("app_kind_1h", "This app, 1 hour", recommended = true),
            ApprovalScopeOption("app_kind_always", "Always for this app"),
        ),
        defaultScopeId = "app_kind_1h",
    )

    // Kind 30078: App-Specific Data — low risk, 1-hour default
    30078 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once"),
            ApprovalScopeOption("app_kind_1h", "This app, 1 hour", recommended = true),
            ApprovalScopeOption("app_kind_always", "Always for this app"),
        ),
        defaultScopeId = "app_kind_1h",
    )

    // List kinds — medium/high risk, replaceable events
    10000, 10002, 30000, 30003 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once", recommended = true),
        ),
        defaultScopeId = "once",
    )

    // Kind 9735: Zap Receipt — low risk, confirming received payment
    9735 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once"),
            ApprovalScopeOption("app_kind_1h", "This app, 1 hour", recommended = true),
            ApprovalScopeOption("app_kind_always", "Always for this app"),
        ),
        defaultScopeId = "app_kind_1h",
    )

    // Kind 9041: Zap Goal — low risk, publishing a fundraising goal
    9041 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once", recommended = true),
        ),
        defaultScopeId = "once",
    )

    // Kind 9734: Zap Request — financial risk, initiates Lightning payment
    9734 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once", recommended = true),
        ),
        defaultScopeId = "once",
    )

    // Kind 23194: NWC Wallet Request — financial risk, wallet operation
    23194 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once", recommended = true),
        ),
        defaultScopeId = "once",
    )

    // Kind 7375: Cashu Wallet Tokens — financial risk, updates wallet state
    7375 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once", recommended = true),
        ),
        defaultScopeId = "once",
    )

    // Kind 24242: Blossom Auth — medium risk, server + method scoped
    24242 -> {
        val method = tags?.firstOrNull { it.size >= 2 && it[0] == "t" }?.get(1)?.lowercase()
        val methodLabel = method?.replaceFirstChar { it.uppercase() } ?: "this method"
        val serverUrl = tags?.firstOrNull { it.size >= 2 && it[0] == "server" }?.get(1)
        val serverDomain = serverUrl?.let { url ->
            try {
                java.net.URI(url).host?.removePrefix("www.") ?: url
            } catch (_: Exception) {
                url
            }
        }
        val hasServer = serverDomain != null
        KindApprovalConfig(
            scopes = listOfNotNull(
                ApprovalScopeOption("once", "This once"),
                // Server-scoped options only when a server is specified
                if (hasServer && method != null) {
                    ApprovalScopeOption(
                        "app_method_1h",
                        "$serverDomain \u2014 $methodLabel only, 1 hour",
                        recommended = true,
                        scopedTypeSuffix = ":t=$method",
                    )
                } else {
                    null
                },
                if (hasServer) {
                    ApprovalScopeOption(
                        "app_kind_1h",
                        "$serverDomain \u2014 any method, 1 hour",
                        recommended = !hasServer || method == null,
                    )
                } else {
                    null
                },
                // No-server fallback: method-scoped but any server
                if (!hasServer && method != null) {
                    ApprovalScopeOption(
                        "app_method_1h",
                        "Any server \u2014 $methodLabel only, 1 hour",
                        recommended = true,
                        scopedTypeSuffix = ":t=$method",
                    )
                } else {
                    null
                },
                ApprovalScopeOption(
                    "app_kind_always",
                    "Any server, any method, always",
                    recommended = !hasServer && method == null,
                ),
            ),
            defaultScopeId = when {
                hasServer && method != null -> "app_method_1h"
                hasServer -> "app_kind_1h"
                method != null -> "app_method_1h"
                else -> "once"
            },
        )
    }

    // Kind 27235: HTTP Auth — medium risk, 1-hour default
    27235 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once"),
            ApprovalScopeOption("app_kind_1h", "This app, 1 hour", recommended = true),
            ApprovalScopeOption("app_kind_always", "Always for this app"),
        ),
        defaultScopeId = "app_kind_1h",
    )

    // Kind 8: Badge Award — medium risk, one-off action
    8 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once", recommended = true),
        ),
        defaultScopeId = "once",
    )

    // Kind 30008: Profile Badges — medium risk, replaces displayed badge list
    30008 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once", recommended = true),
        ),
        defaultScopeId = "once",
    )

    // Kind 30009: Badge Definition — medium risk, publishing a badge
    30009 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once", recommended = true),
        ),
        defaultScopeId = "once",
    )

    // Kind 34550: Community Definition — medium risk, creating/updating a community
    34550 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once", recommended = true),
        ),
        defaultScopeId = "once",
    )

    // Kind 4550: Community Approved Post — medium risk, moderator approval action, 1-hour default
    4550 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once"),
            ApprovalScopeOption("app_kind_1h", "This app, 1 hour", recommended = true),
            ApprovalScopeOption("app_kind_always", "Always for this app"),
        ),
        defaultScopeId = "app_kind_1h",
    )

    // Kind 30311: Live Event — low risk, 1-hour default
    30311 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once"),
            ApprovalScopeOption("app_kind_1h", "This app, 1 hour", recommended = true),
            ApprovalScopeOption("app_kind_always", "Always for this app"),
        ),
        defaultScopeId = "app_kind_1h",
    )

    // Kind 31923: Calendar Event — low risk, 1-hour default
    31923 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once"),
            ApprovalScopeOption("app_kind_1h", "This app, 1 hour", recommended = true),
            ApprovalScopeOption("app_kind_always", "Always for this app"),
        ),
        defaultScopeId = "app_kind_1h",
    )

    // Kind 30382: Handler Declaration — high risk, once only
    30382 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once", recommended = true),
        ),
        defaultScopeId = "once",
    )

    // Kind 31990: Handler Recommendation — high risk, once only
    31990 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once", recommended = true),
        ),
        defaultScopeId = "once",
    )

    else -> null
}
