package com.emm.mybest.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NoteValidatorTest {

    @Test
    fun `an empty or whitespace-only text is blank`() {
        assertEquals(NoteValidationError.Blank, NoteValidator.validate(""))
        assertEquals(NoteValidationError.Blank, NoteValidator.validate("   "))
        assertEquals(NoteValidationError.Blank, NoteValidator.validate(" \n\t "))
    }

    @Test
    fun `a text of one to 500 characters is accepted`() {
        assertNull(NoteValidator.validate("a"))
        assertNull(NoteValidator.validate("a".repeat(500)))
    }

    @Test
    fun `surrounding spaces do not count toward the limit`() {
        assertNull(NoteValidator.validate("  " + "a".repeat(500) + "  "))
    }

    @Test
    fun `a text of 501 characters is too long`() {
        assertEquals(NoteValidationError.TooLong, NoteValidator.validate("a".repeat(501)))
    }

    @Test
    fun `inner line breaks are accepted`() {
        assertNull(NoteValidator.validate("línea 1\nlínea 2"))
    }
}
