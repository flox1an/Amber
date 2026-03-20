package com.greenart7c3.nostrsigner.service

import com.greenart7c3.nostrsigner.service.model.AmberEvent
import com.vitorpamplona.quartz.nip01Core.core.Event

/**
 * Danger Zone: force-prompt detection for dangerous events.
 *
 * Determines whether an event should bypass auto-approve and always
 * show the approval UI, even when the user has granted blanket permission.
 * Uses ONLY event data — no relay lookups needed.
 *
 * Rules:
 * 1. Kind 0 with payment/identity fields (lud16, lud06, nip05)
 * 2. Kind 5 — event deletion (irreversible)
 * 3. Kind 62 — vanish request (permanently deletes all content)
 * 4. List kinds (3, 10000, 30000, 30003, 10002) with suspiciously few entries
 */
object ForcePromptChecker {

    data class ForcePromptResult(
        val force: Boolean,
        val rule: String? = null,
        val reason: String? = null,
    )

    private val PASS = ForcePromptResult(force = false)

    /** List kinds and their primary tag letter + human-readable label. */
    private data class ListKindMeta(val tag: String, val label: String)

    private val LIST_KINDS = mapOf(
        3 to ListKindMeta("p", "Follow list"),
        10000 to ListKindMeta("p", "Mute list"),
        30000 to ListKindMeta("p", "Follow set"),
        30003 to ListKindMeta("e", "Bookmark set"),
        10002 to ListKindMeta("r", "Relay list"),
    )

    /** Minimum number of primary tags before a list replacement is considered suspicious. */
    private const val LIST_MIN_TAGS = 2

    /** Fields in Kind 0 content that indicate payment or identity changes. */
    private val SENSITIVE_PROFILE_FIELDS = listOf("lud16", "lud06", "nip05")

    /** Check an AmberEvent. */
    fun shouldForcePrompt(event: AmberEvent?): ForcePromptResult {
        if (event == null) return PASS
        return shouldForcePrompt(event.kind, event.content, event.tags)
    }

    /** Check a Quartz Event. */
    fun shouldForcePrompt(event: Event?): ForcePromptResult {
        if (event == null) return PASS
        return shouldForcePrompt(event.kind, event.content, event.tags)
    }

    /**
     * Core check using raw event fields.
     * Both AmberEvent and Quartz Event share the same field types.
     */
    fun shouldForcePrompt(kind: Int, content: String, tags: Array<Array<String>>): ForcePromptResult = when (kind) {
        0 -> checkProfilePaymentIdentity(content)
        5 -> ForcePromptResult(
            force = true,
            rule = "deletion",
            reason = "Event deletion is irreversible",
        )
        62 -> ForcePromptResult(
            force = true,
            rule = "vanish",
            reason = "Vanish request permanently deletes all content",
        )
        else -> checkListOverwrite(kind, tags)
    }

    private fun checkProfilePaymentIdentity(content: String): ForcePromptResult {
        if (content.isBlank()) return PASS

        return try {
            // Simple check: look for sensitive field keys in the JSON string.
            // This avoids pulling in a full JSON parser for a security gate —
            // false positives (field name appears in a value) are acceptable
            // because they just show the approval UI, never skip it.
            val hasSensitiveField = SENSITIVE_PROFILE_FIELDS.any { field ->
                content.contains("\"$field\"")
            }
            if (hasSensitiveField) {
                ForcePromptResult(
                    force = true,
                    rule = "profile-payment-identity",
                    reason = "Changes payment address or identity verification",
                )
            } else {
                PASS
            }
        } catch (_: Exception) {
            // Malformed content — force prompt for safety
            ForcePromptResult(
                force = true,
                rule = "profile-malformed",
                reason = "Profile update has invalid content",
            )
        }
    }

    private fun checkListOverwrite(kind: Int, tags: Array<Array<String>>): ForcePromptResult {
        val listMeta = LIST_KINDS[kind] ?: return PASS

        val tagCount = tags.count { it.isNotEmpty() && it[0] == listMeta.tag }
        if (tagCount <= LIST_MIN_TAGS) {
            return ForcePromptResult(
                force = true,
                rule = "list-overwrite",
                reason = "${listMeta.label} has only $tagCount entries — possible accidental overwrite",
            )
        }

        return PASS
    }
}
