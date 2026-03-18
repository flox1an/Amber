package com.greenart7c3.nostrsigner.ui.components

data class ApprovalScopeOption(
    val id: String,
    val label: String,
    val recommended: Boolean = false,
)

data class KindApprovalConfig(
    val scopes: List<ApprovalScopeOption>,
    val defaultScopeId: String,
)

/**
 * Returns kind-specific approval scope configuration.
 * Null means fall back to the existing RememberMyChoice behavior.
 */
fun getApprovalConfig(kind: Int): KindApprovalConfig? = when (kind) {
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

    // Kind 6: Repost — low risk, often done in bursts
    6 -> KindApprovalConfig(
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

    // List kinds — medium/high risk, replaceable events
    10000, 10002, 30000, 30003 -> KindApprovalConfig(
        scopes = listOf(
            ApprovalScopeOption("once", "This once", recommended = true),
        ),
        defaultScopeId = "once",
    )

    else -> null
}
