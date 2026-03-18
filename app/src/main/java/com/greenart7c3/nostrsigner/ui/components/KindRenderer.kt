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
    5 -> true
    6 -> true
    7 -> true
    9 -> true
    62 -> true
    1018 -> true
    1068 -> true
    1111 -> true
    1311 -> true
    9802 -> true
    10000, 10002, 30000, 30003 -> true
    30315 -> true
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
        5 -> Kind5DeletionRenderer(content = content, tags = tags)
        6 -> Kind6RepostRenderer(content = content, account = account, tags = tags)
        7 -> Kind7ReactionRenderer(content = content, account = account, tags = tags)
        9 -> Kind9ChatRenderer(content = content, account = account, tags = tags)
        62 -> Kind62VanishRenderer(content = content, tags = tags)
        1018 -> Kind1018PollResponseRenderer(content = content, tags = tags)
        1068 -> Kind1068PollRenderer(content = content, tags = tags)
        1111 -> Kind1111CommentRenderer(content = content, account = account, tags = tags)
        1311 -> Kind1311LiveChatRenderer(content = content, tags = tags)
        9802 -> Kind9802HighlightRenderer(content = content, account = account, tags = tags)
        10000, 10002, 30000, 30003 -> ListOverwriteRenderer(kind = kind, content = content, tags = tags)
        30315 -> Kind30315StatusRenderer(content = content, tags = tags)
    }
}
