package com.sgh.wordwatch.core

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** 단어 하나. [term] = 영어, [meaning] = 뜻(없어도 됨). */
@Serializable
data class Word(
    val term: String,
    val meaning: String = "",
    val addedAt: Long = 0L,
) {
    /** 중복 판단/정렬에 쓰는 키 (대소문자 무시). */
    val key: String get() = term.trim().lowercase()
}

/** 폰↔시계 사이에 주고받고, 파일로 저장하는 형식. */
object WordCodec {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = false }

    fun encode(words: List<Word>): String = json.encodeToString(words)

    fun decode(text: String): List<Word> =
        if (text.isBlank()) emptyList() else json.decodeFromString(text)
}
