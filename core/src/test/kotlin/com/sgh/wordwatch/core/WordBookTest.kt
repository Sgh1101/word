package com.sgh.wordwatch.core

import org.junit.Assert.assertEquals
import org.junit.Test

class WordBookTest {
    private val existing = listOf(Word("apple", "사과"), Word("Banana", ""))

    @Test fun mergeSortsAndDedupes() {
        val merged = WordBook.merge(existing, listOf(Word("cherry", "체리"), Word("APPLE", ""), Word("banana", "바나나")))
        assertEquals(listOf("apple", "Banana", "cherry"), merged.map { it.term })
        assertEquals("사과", merged[0].meaning)      // 빈 뜻은 기존 뜻 유지
        assertEquals("바나나", merged[1].meaning)    // 새 뜻으로 갱신
    }

    @Test fun preview() {
        val pv = WordBook.preview(existing, listOf(Word("apple", "사과"), Word("banana", "바나나"), Word("kiwi", ""), Word("kiwi", "키위")))
        assertEquals(listOf(WordBook.Status.SAME, WordBook.Status.UPDATE, WordBook.Status.NEW), pv.map { it.status })
        assertEquals("키위", pv[2].word.meaning)
    }
}
