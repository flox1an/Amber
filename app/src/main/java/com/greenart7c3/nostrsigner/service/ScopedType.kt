package com.greenart7c3.nostrsigner.service

/**
 * Builds and parses scoped permission type strings.
 *
 * Convention:
 * - `sign_event` → broad grant for any d-tag / any method
 * - `sign_event:d=<value>` → scoped to a specific d-tag (addressable events, kind 30000+)
 * - `sign_event:t=<method>` → scoped to a specific Blossom HTTP method (kind 24242)
 *
 * Each scope dimension produces a separate permission row.
 * Lookup checks specific type first, then falls back to the base type.
 */
object ScopedType {

    /** Known Blossom methods (per BUD-01/BUD-11). */
    val BLOSSOM_METHODS = listOf("get", "upload", "delete", "list", "media")

    /** Build a d-tag-scoped type string. */
    fun withDTag(baseType: String, dTag: String): String = "$baseType:d=$dTag"

    /** Build a Blossom method-scoped type string. */
    fun withBlossomMethod(baseType: String, method: String): String = "$baseType:t=${method.lowercase()}"

    /** Extract the base type (e.g., "sign_event" from "sign_event:d=my-list"). */
    fun baseType(scopedType: String): String = scopedType.substringBefore(':')

    /** Extract the d-tag value, or null if not d-tag scoped. */
    fun dTagValue(scopedType: String): String? {
        val prefix = ":d="
        val idx = scopedType.indexOf(prefix)
        if (idx < 0) return null
        return scopedType.substring(idx + prefix.length)
    }

    /** Extract the Blossom method, or null if not method scoped. */
    fun blossomMethod(scopedType: String): String? {
        val prefix = ":t="
        val idx = scopedType.indexOf(prefix)
        if (idx < 0) return null
        return scopedType.substring(idx + prefix.length)
    }

    /** True if the kind is an addressable event (NIP-33: 30000-39999). */
    fun isAddressableKind(kind: Int): Boolean = kind in 30000..39999

    /**
     * Returns a list of type strings to check, most specific first.
     * Callers should check each in order and use the first match.
     *
     * For kind 24242 with a Blossom t-tag:
     *   ["sign_event:t=upload", "sign_event"]
     *
     * For addressable events with a d-tag:
     *   ["sign_event:d=my-list", "sign_event"]
     *
     * For everything else:
     *   ["sign_event"]
     */
    fun lookupTypes(
        baseType: String,
        kind: Int,
        tags: Array<Array<String>>,
    ): List<String> {
        val types = mutableListOf<String>()

        if (kind == 24242) {
            // Blossom: check for t-tag method scope
            val method = tags.firstOrNull { it.size >= 2 && it[0] == "t" }?.get(1)
            if (method != null) {
                types.add(withBlossomMethod(baseType, method))
            }
        } else if (isAddressableKind(kind)) {
            // Addressable event: check for d-tag scope
            val dTag = tags.firstOrNull { it.size >= 2 && it[0] == "d" }?.get(1)
            if (!dTag.isNullOrBlank()) {
                types.add(withDTag(baseType, dTag))
            }
        }

        // Always fall back to broad type
        types.add(baseType)
        return types
    }

    /**
     * Checks if a stored permission type matches a request.
     * A stored "SIGN_EVENT:t=upload" matches a request for "SIGN_EVENT" with t=upload tag.
     * A stored "SIGN_EVENT" (broad) matches any request for "SIGN_EVENT".
     */
    fun permissionTypeMatches(
        storedType: String,
        requestBaseType: String,
        kind: Int,
        tags: Array<Array<String>>,
    ): Boolean {
        // Exact base type match (broad permission)
        if (storedType == requestBaseType) return true

        // Check if stored type is one of the scoped lookup types for this request
        val validTypes = lookupTypes(requestBaseType, kind, tags)
        return storedType in validTypes
    }
}
