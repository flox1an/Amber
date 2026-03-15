package com.greenart7c3.nostrsigner.ui.components

import androidx.compose.runtime.Composable
import com.greenart7c3.nostrsigner.models.Account

/**
 * Returns true if the given kind has a rich renderer available.
 */
fun hasKindRenderer(kind: Int): Boolean = when (kind) {
    0 -> true
    1 -> true
    3 -> true
    6 -> true
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
    tags: Array<Array<String>> = emptyArray(),
) {
    when (kind) {
        0 -> Kind0ProfileRenderer(content = content, account = account)
        1 -> Kind1NoteRenderer(content = content, account = account, tags = tags)
        3 -> Kind3FollowListRenderer(content = content, account = account, tags = tags)
        6 -> Kind6RepostRenderer(content = content, account = account, tags = tags)
    }
}
