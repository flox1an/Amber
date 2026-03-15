package com.greenart7c3.nostrsigner.ui.components

import androidx.compose.runtime.Composable
import com.greenart7c3.nostrsigner.models.Account

/**
 * Returns true if the given kind has a rich renderer available.
 */
fun hasKindRenderer(kind: Int): Boolean = when (kind) {
    0 -> true
    else -> false
}

/**
 * Renders kind-specific content. Call only when [hasKindRenderer] returns true.
 */
@Composable
fun RenderKindContent(
    kind: Int,
    content: String,
    account: Account,
) {
    when (kind) {
        0 -> Kind0ProfileRenderer(content = content, account = account)
    }
}
