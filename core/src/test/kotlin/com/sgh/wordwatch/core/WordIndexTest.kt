package com.sgh.wordwatch.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WordIndexTest {
    private val words = listOf("apple", "Apply", "area", "a", "boat", "zoo", "3D", "able").map { Word(it) }

    @Test fun firstGroups() {
        assertEquals(
            listOf("A" to 5, "B" to 1, "Z" to 1, "#" to 1),
            WordIndex.firstGroups(words).map { it.key to it.count },
        )
    }

    @Test fun secondGroups() {
        assertEquals(
            listOf("A·" to 1, "Ab" to 1, "Ap" to 2, "Ar" to 1),
            WordIndex.secondGroups(words, "A").map { it.key to it.count },
        )
    }

    @Test fun filterByFirst() =
        assertEquals(listOf("a", "able", "apple", "Apply", "area"), WordIndex.filter(words, "A").map { it.term })

    @Test fun filterBySecond() =
        assertEquals(listOf("apple", "Apply"), WordIndex.filter(words, "Ap").map { it.term })

    @Test fun filterShort() = assertEquals(listOf("a"), WordIndex.filter(words, "A·").map { it.term })

    @Test fun filterOther() = assertEquals(listOf("3D"), WordIndex.filter(words, "#").map { it.term })

    @Test fun filterAll() = assertEquals(8, WordIndex.filter(words, WordIndex.ALL).size)

    @Test fun split() {
        assertTrue(WordIndex.shouldSplit(WordIndex.Group("A", 1)))   // 기본: 항상
        assertTrue(WordIndex.shouldSplit(WordIndex.Group("A", 15), threshold = 15))
        assertFalse(WordIndex.shouldSplit(WordIndex.Group("A", 14), threshold = 15))
        assertFalse(WordIndex.shouldSplit(WordIndex.Group("#", 99)))
    }
}
