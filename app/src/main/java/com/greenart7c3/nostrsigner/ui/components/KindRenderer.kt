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
    4 -> true
    5 -> true
    6 -> true
    7 -> true
    8 -> true
    9 -> true
    13 -> true
    14 -> true
    20 -> true
    21 -> true
    62 -> true
    1018 -> true
    1059 -> true
    1063 -> true
    1068 -> true
    1111 -> true
    1311 -> true
    4550 -> true
    9802 -> true
    1984 -> true
    1985 -> true
    10000, 10002, 30000, 30003 -> true
    30008 -> true
    30009 -> true
    30023 -> true
    30315 -> true
    31234 -> true
    34550 -> true
    7375 -> true
    9041 -> true
    9734 -> true
    9735 -> true
    23194 -> true
    24242 -> true
    27235 -> true
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
        4 -> Kind4LegacyDmRenderer(content = content, account = account, tags = tags)
        5 -> Kind5DeletionRenderer(content = content, tags = tags)
        6 -> Kind6RepostRenderer(content = content, account = account, tags = tags)
        7 -> Kind7ReactionRenderer(content = content, account = account, tags = tags)
        8 -> Kind8BadgeAwardRenderer(content = content, tags = tags)
        9 -> Kind9ChatRenderer(content = content, account = account, tags = tags)
        13 -> Kind13SealRenderer(content = content, account = account, tags = tags)
        14 -> Kind14DmRenderer(content = content, account = account, tags = tags)
        20 -> Kind20PictureRenderer(content = content, tags = tags)
        21 -> Kind21VideoRenderer(content = content, tags = tags)
        62 -> Kind62VanishRenderer(content = content, tags = tags)
        1018 -> Kind1018PollResponseRenderer(content = content, tags = tags)
        1059 -> Kind1059GiftWrapRenderer(content = content, account = account, tags = tags)
        1063 -> Kind1063FileRenderer(content = content, tags = tags)
        1068 -> Kind1068PollRenderer(content = content, tags = tags)
        1111 -> Kind1111CommentRenderer(content = content, account = account, tags = tags)
        1311 -> Kind1311LiveChatRenderer(content = content, tags = tags)
        4550 -> Kind4550CommunityApprovedRenderer(content = content, tags = tags)
        9802 -> Kind9802HighlightRenderer(content = content, account = account, tags = tags)
        1984 -> Kind1984ReportRenderer(content = content, account = account, tags = tags)
        1985 -> Kind1985LabelRenderer(content = content, account = account, tags = tags)
        10000, 10002, 30000, 30003 -> ListOverwriteRenderer(kind = kind, content = content, tags = tags)
        30008 -> Kind30008ProfileBadgesRenderer(content = content, tags = tags)
        30009 -> Kind30009BadgeDefRenderer(content = content, tags = tags)
        30023 -> Kind30023ArticleRenderer(content = content, tags = tags)
        34550 -> Kind34550CommunityRenderer(content = content, tags = tags)
        30315 -> Kind30315StatusRenderer(content = content, tags = tags)
        31234 -> Kind31234DraftRenderer(content = content, tags = tags)
        7375 -> Kind7375CashuRenderer(content = content, tags = tags)
        9041 -> Kind9041ZapGoalRenderer(content = content, tags = tags)
        9734 -> Kind9734ZapRequestRenderer(content = content, account = account, tags = tags)
        9735 -> Kind9735ZapReceiptRenderer(content = content, account = account, tags = tags)
        23194 -> Kind23194NwcRenderer(content = content, tags = tags)
        24242 -> Kind24242BlossomRenderer(content = content, tags = tags)
        27235 -> Kind27235HttpAuthRenderer(content = content, tags = tags)
    }
}
