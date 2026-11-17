package com.sgh.wordwatch.core

/**
 * 첫 글자 / 두 번째 글자 분류.
 *
 * 키 규칙:
 *  - "A" ~ "Z" : 첫 글자
 *  - "#"       : 영어 글자로 시작하지 않는 단어
 *  - "Ap"      : 첫 두 글자 (2단계)
 *  - "A·"      : 한 글자짜리 단어 (예: "a") 또는 두 번째가 영어 글자가 아님 (예: "a-ha")
 */
object WordIndex {

    /** 한 글자 그룹에 이 개수 이상이면 두 번째 글자 단계를 보여준다. */
    const val DEFAULT_SPLIT_THRESHOLD = 15

    const val ALL = "*"
    const val OTHER = "#"
    const val SHORT_MARK = "·"

    data class Group(val key: String, val count: Int)

    fun firstKey(term: String): String {
        val c = term.trim().firstOrNull() ?: return OTHER
        return if (c.isAsciiLetter()) c.uppercaseChar().toString() else OTHER
    }

    fun secondKey(term: String): String {
        val first = firstKey(term)
        if (first == OTHER) return OTHER
        val c = term.trim().getOrNull(1)
        return if (c != null && c.isAsciiLetter()) first + c.lowercaseChar() else first + SHORT_MARK
    }

    /** 키 길이에 따라 맞는 분류 키를 계산. */
    fun keyFor(term: String, key: String): String = when {
        key == OTHER || key.length == 1 -> firstKey(term)
        else -> secondKey(term)
    }

    /** 첫 글자 그룹 목록 (A..Z 순, # 은 맨 뒤). */
    fun firstGroups(words: List<Word>): List<Group> =
        words.groupingBy { firstKey(it.term) }.eachCount()
            .map { Group(it.key, it.value) }
            .sortedWith(compareBy({ it.key == OTHER }, { it.key }))

    /** 특정 첫 글자 안의 두 번째 글자 그룹 목록. */
    fun secondGroups(words: List<Word>, firstKey: String): List<Group> =
        words.filter { firstKey(it.term) == firstKey }
            .groupingBy { secondKey(it.term) }.eachCount()
            .map { Group(it.key, it.value) }
            .sortedWith(compareBy({ !it.key.endsWith(SHORT_MARK) }, { it.key.lowercase() }))

    /** 이 첫 글자 그룹을 2단계로 나눌지. */
    fun shouldSplit(group: Group, threshold: Int = DEFAULT_SPLIT_THRESHOLD): Boolean =
        group.key != OTHER && group.count >= threshold

    /** 키에 해당하는 단어만 알파벳순으로. */
    fun filter(words: List<Word>, key: String): List<Word> {
        val list = if (key == ALL) words else words.filter { keyFor(it.term, key) == key }
        return WordBook.sort(list)
    }

    private fun Char.isAsciiLetter() = this in 'a'..'z' || this in 'A'..'Z'
}
