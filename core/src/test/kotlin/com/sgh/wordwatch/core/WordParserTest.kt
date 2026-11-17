package com.sgh.wordwatch.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WordParserTest {
    private fun p(s: String) = WordParser.parseLine(s)?.let { it.term to it.meaning }

    @Test fun tab() = assertEquals("apple" to "사과", p("apple\t사과"))
    @Test fun dash() = assertEquals("apple" to "사과", p("apple - 사과"))
    @Test fun enDash() = assertEquals("apple" to "사과", p("apple – 사과"))
    @Test fun colon() = assertEquals("apple" to "사과", p("apple: 사과"))
    @Test fun comma() = assertEquals("apple" to "사과", p("apple, 사과"))
    @Test fun equals() = assertEquals("apple" to "사과", p("apple = 사과"))
    @Test fun space() = assertEquals("apple" to "사과", p("apple 사과"))
    @Test fun noSpace() = assertEquals("apple" to "사과", p("apple사과"))
    @Test fun phrase() = assertEquals("give up" to "포기하다", p("give up 포기하다"))
    @Test fun hyphenWord() = assertEquals("well-known" to "잘 알려진", p("well-known 잘 알려진"))
    @Test fun meaningWithComma() = assertEquals("run" to "달리다, 운영하다", p("run 달리다, 운영하다"))
    @Test fun tildeMeaning() = assertEquals("able" to "~할 수 있는", p("able ~할 수 있는"))
    @Test fun tildeColon() = assertEquals("about" to "~에 대해", p("about: ~에 대해"))
    @Test fun numbered() = assertEquals("apple" to "사과", p("1. apple 사과"))
    @Test fun numberedParen() = assertEquals("apple" to "사과", p("12) apple - 사과"))
    @Test fun bullet() = assertEquals("apple" to "사과", p("- apple 사과"))
    @Test fun termOnly() = assertEquals("apple" to "", p("  apple  "))
    @Test fun englishMeaning() = assertEquals("apple" to "a fruit", p("apple: a fruit"))
    @Test fun blank() = assertNull(p("   "))
    @Test fun koreanOnly() = assertNull(p("사과"))

    @Test fun multiLine() {
        val words = WordParser.parse("apple 사과\n\nbanana - 바나나\r\ncherry\t체리\n")
        assertEquals(listOf("apple", "banana", "cherry"), words.map { it.term })
    }
}
