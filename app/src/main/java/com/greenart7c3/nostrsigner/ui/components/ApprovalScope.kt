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

    else -> null
}
