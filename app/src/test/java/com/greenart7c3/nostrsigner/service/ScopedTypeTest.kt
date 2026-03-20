package com.greenart7c3.nostrsigner.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ScopedTypeTest {

    @Test
    fun withDTag_builds_correct_type_string() {
        assertEquals("sign_event:d=my-list", ScopedType.withDTag("sign_event", "my-list"))
        assertEquals("SIGN_EVENT:d=profile", ScopedType.withDTag("SIGN_EVENT", "profile"))
    }

    @Test
    fun withBlossomMethod_lowercases_method() {
        assertEquals("sign_event:t=upload", ScopedType.withBlossomMethod("sign_event", "Upload"))
        assertEquals("sign_event:t=get", ScopedType.withBlossomMethod("sign_event", "GET"))
    }

    @Test
    fun baseType_strips_scope_suffix() {
        assertEquals("sign_event", ScopedType.baseType("sign_event:d=my-list"))
        assertEquals("sign_event", ScopedType.baseType("sign_event:t=upload"))
        assertEquals("sign_event", ScopedType.baseType("sign_event"))
    }

    @Test
    fun dTagValue_extracts_dtag() {
        assertEquals("my-list", ScopedType.dTagValue("sign_event:d=my-list"))
        assertNull(ScopedType.dTagValue("sign_event"))
        assertNull(ScopedType.dTagValue("sign_event:t=upload"))
    }

    @Test
    fun blossomMethod_extracts_method() {
        assertEquals("upload", ScopedType.blossomMethod("sign_event:t=upload"))
        assertNull(ScopedType.blossomMethod("sign_event"))
        assertNull(ScopedType.blossomMethod("sign_event:d=my-list"))
    }

    @Test
    fun isAddressableKind_correct_range() {
        assertEquals(true, ScopedType.isAddressableKind(30000))
        assertEquals(true, ScopedType.isAddressableKind(30023))
        assertEquals(true, ScopedType.isAddressableKind(39999))
        assertEquals(false, ScopedType.isAddressableKind(1))
        assertEquals(false, ScopedType.isAddressableKind(29999))
        assertEquals(false, ScopedType.isAddressableKind(40000))
    }

    @Test
    fun lookupTypes_blossom_with_t_tag() {
        val tags = arrayOf(arrayOf("t", "upload"), arrayOf("server", "https://blossom.example"))
        val types = ScopedType.lookupTypes("sign_event", 24242, tags)
        assertEquals(listOf("sign_event:t=upload", "sign_event"), types)
    }

    @Test
    fun lookupTypes_blossom_without_t_tag() {
        val tags = arrayOf(arrayOf("server", "https://blossom.example"))
        val types = ScopedType.lookupTypes("sign_event", 24242, tags)
        assertEquals(listOf("sign_event"), types)
    }

    @Test
    fun lookupTypes_addressable_with_d_tag() {
        val tags = arrayOf(arrayOf("d", "my-community"), arrayOf("p", "abc123"))
        val types = ScopedType.lookupTypes("sign_event", 34550, tags)
        assertEquals(listOf("sign_event:d=my-community", "sign_event"), types)
    }

    @Test
    fun lookupTypes_addressable_empty_d_tag() {
        val tags = arrayOf(arrayOf("d", ""))
        val types = ScopedType.lookupTypes("sign_event", 30023, tags)
        assertEquals(listOf("sign_event"), types)
    }

    @Test
    fun lookupTypes_regular_event() {
        val tags = arrayOf(arrayOf("p", "abc123"))
        val types = ScopedType.lookupTypes("sign_event", 1, tags)
        assertEquals(listOf("sign_event"), types)
    }
}
