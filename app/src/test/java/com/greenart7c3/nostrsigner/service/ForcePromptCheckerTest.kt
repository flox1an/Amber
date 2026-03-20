package com.greenart7c3.nostrsigner.service

import com.greenart7c3.nostrsigner.service.model.AmberEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ForcePromptCheckerTest {

    private fun event(
        kind: Int,
        content: String = "",
        tags: Array<Array<String>> = emptyArray(),
    ) = AmberEvent(
        id = "test",
        pubKey = "testpub",
        createdAt = 0L,
        kind = kind,
        tags = tags,
        content = content,
        sig = "",
    )

    // --- Rule 1: Kind 0 with payment/identity fields ---

    @Test
    fun `kind 0 with lud16 forces prompt`() {
        val e = event(0, content = """{"name":"Alice","lud16":"alice@walletofsatoshi.com"}""")
        val result = ForcePromptChecker.shouldForcePrompt(e)
        assertTrue(result.force)
        assertEquals("profile-payment-identity", result.rule)
    }

    @Test
    fun `kind 0 with lud06 forces prompt`() {
        val e = event(0, content = """{"name":"Alice","lud06":"lnurl1dp68gurn8ghj7..."}""")
        val result = ForcePromptChecker.shouldForcePrompt(e)
        assertTrue(result.force)
        assertEquals("profile-payment-identity", result.rule)
    }

    @Test
    fun `kind 0 with nip05 forces prompt`() {
        val e = event(0, content = """{"name":"Alice","nip05":"alice@nostr.com"}""")
        val result = ForcePromptChecker.shouldForcePrompt(e)
        assertTrue(result.force)
        assertEquals("profile-payment-identity", result.rule)
    }

    @Test
    fun `kind 0 without sensitive fields does not force prompt`() {
        val e = event(0, content = """{"name":"Alice","about":"Just vibes","picture":"https://example.com/pic.jpg"}""")
        val result = ForcePromptChecker.shouldForcePrompt(e)
        assertFalse(result.force)
    }

    @Test
    fun `kind 0 with empty content does not force prompt`() {
        val e = event(0, content = "")
        val result = ForcePromptChecker.shouldForcePrompt(e)
        assertFalse(result.force)
    }

    // --- Rule 2: Kind 5 deletion ---

    @Test
    fun `kind 5 always forces prompt`() {
        val e = event(5, content = "deleting stuff", tags = arrayOf(arrayOf("e", "abc123")))
        val result = ForcePromptChecker.shouldForcePrompt(e)
        assertTrue(result.force)
        assertEquals("deletion", result.rule)
    }

    // --- Rule 3: Kind 62 vanish ---

    @Test
    fun `kind 62 always forces prompt`() {
        val e = event(62, tags = arrayOf(arrayOf("relay", "ALL_RELAYS")))
        val result = ForcePromptChecker.shouldForcePrompt(e)
        assertTrue(result.force)
        assertEquals("vanish", result.rule)
    }

    // --- Rule 4: List overwrite detection ---

    @Test
    fun `kind 3 with empty tags forces prompt`() {
        val e = event(3, tags = emptyArray())
        val result = ForcePromptChecker.shouldForcePrompt(e)
        assertTrue(result.force)
        assertEquals("list-overwrite", result.rule)
    }

    @Test
    fun `kind 3 with 1 p-tag forces prompt`() {
        val e = event(3, tags = arrayOf(arrayOf("p", "abc123")))
        val result = ForcePromptChecker.shouldForcePrompt(e)
        assertTrue(result.force)
        assertEquals("list-overwrite", result.rule)
    }

    @Test
    fun `kind 3 with 2 p-tags forces prompt`() {
        val e = event(3, tags = arrayOf(arrayOf("p", "abc"), arrayOf("p", "def")))
        val result = ForcePromptChecker.shouldForcePrompt(e)
        assertTrue(result.force)
        assertEquals("list-overwrite", result.rule)
    }

    @Test
    fun `kind 3 with 3 p-tags does not force prompt`() {
        val e = event(3, tags = arrayOf(arrayOf("p", "a"), arrayOf("p", "b"), arrayOf("p", "c")))
        val result = ForcePromptChecker.shouldForcePrompt(e)
        assertFalse(result.force)
    }

    @Test
    fun `kind 3 with many p-tags does not force prompt`() {
        val tags = (1..50).map { arrayOf("p", "pubkey$it") }.toTypedArray()
        val e = event(3, tags = tags)
        val result = ForcePromptChecker.shouldForcePrompt(e)
        assertFalse(result.force)
    }

    @Test
    fun `kind 10000 mute list with 0 p-tags forces prompt`() {
        val e = event(10000, tags = emptyArray())
        val result = ForcePromptChecker.shouldForcePrompt(e)
        assertTrue(result.force)
        assertEquals("list-overwrite", result.rule)
    }

    @Test
    fun `kind 10002 relay list with 1 r-tag forces prompt`() {
        val e = event(10002, tags = arrayOf(arrayOf("r", "wss://relay.damus.io")))
        val result = ForcePromptChecker.shouldForcePrompt(e)
        assertTrue(result.force)
    }

    @Test
    fun `kind 10002 relay list with many r-tags does not force prompt`() {
        val tags = arrayOf(
            arrayOf("r", "wss://relay.damus.io"),
            arrayOf("r", "wss://nos.lol"),
            arrayOf("r", "wss://relay.nostr.band"),
        )
        val e = event(10002, tags = tags)
        val result = ForcePromptChecker.shouldForcePrompt(e)
        assertFalse(result.force)
    }

    @Test
    fun `kind 30003 bookmark set with 0 e-tags forces prompt`() {
        val e = event(30003, tags = arrayOf(arrayOf("d", "my-bookmarks")))
        val result = ForcePromptChecker.shouldForcePrompt(e)
        assertTrue(result.force)
    }

    // --- Non-dangerous kinds ---

    @Test
    fun `kind 1 does not force prompt`() {
        val e = event(1, content = "hello world")
        val result = ForcePromptChecker.shouldForcePrompt(e)
        assertFalse(result.force)
    }

    @Test
    fun `kind 7 does not force prompt`() {
        val e = event(7, content = "+")
        val result = ForcePromptChecker.shouldForcePrompt(e)
        assertFalse(result.force)
    }

    @Test
    fun `null event does not force prompt`() {
        val nullEvent: AmberEvent? = null
        val result = ForcePromptChecker.shouldForcePrompt(nullEvent)
        assertFalse(result.force)
    }
}
