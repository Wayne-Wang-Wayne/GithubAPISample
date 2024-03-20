package com.example.githubapisample.utils

import junit.framework.Assert.assertEquals
import org.junit.Test

class CountConverterImplTest {

    private val countConverter = CountConverterImpl()

    @Test
    fun countConverterImpl_givenCertainInt_shouldGetCorrectStringFormat() {
        assertEquals("3.3k", countConverter.convertCountToKString(3300))
        assertEquals("3k", countConverter.convertCountToKString(3020))
        assertEquals("3k", countConverter.convertCountToKString(3000))
        assertEquals("33.3k", countConverter.convertCountToKString(33300))
        assertEquals("33k", countConverter.convertCountToKString(33000))
        assertEquals("33.1k", countConverter.convertCountToKString(33098))
    }
}