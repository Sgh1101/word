package com.sgh.wordwatch.core

/** 단어장 하나에 대한 순수 로직 (저장 X). 항상 알파벳순으로 유지. */
object WordBook {

    enum class Status { NEW, UPDATE, SAME }

    data class PreviewItem(val word: Word, val status: Status)

    /** 추가하기 전에 새 단어/뜻 갱신/이미 있음을 보여주기 위한 미리보기. */
    fun preview(existing: List<Word>, incoming: List<Word>): List<PreviewItem> {
        val byKey = existing.associateBy { it.key }.toMutableMap()
        val seen = HashMap<String, Int>()
        val result = ArrayList<PreviewItem>()
        for (w in incoming) {
            val idx = seen[w.key]
            if (idx != null) { // 같은 붙여넣기 안에서 중복 → 뒤에 나온 것으로 덮기
                result[idx] = result[idx].copy(word = w)
                continue
            }
            val old = byKey[w.key]
            val status = when {
                old == null -> Status.NEW
                w.meaning.isNotBlank() && w.meaning != old.meaning -> Status.UPDATE
                else -> Status.SAME
            }
            seen[w.key] = result.size
            result += PreviewItem(w, status)
        }
        return result
    }

    /** 단어들을 합친다. 같은 단어면 새 뜻이 있을 때만 뜻을 갱신. */
    fun merge(existing: List<Word>, incoming: List<Word>): List<Word> {
        val map = LinkedHashMap<String, Word>()
        existing.forEach { map[it.key] = it }
        for (w in incoming) {
            val old = map[w.key]
            map[w.key] = when {
                old == null -> w
                w.meaning.isNotBlank() -> old.copy(meaning = w.meaning)
                else -> old
            }
        }
        return sort(map.values)
    }

    fun sort(words: Collection<Word>): List<Word> =
        words.sortedWith(compareBy<Word>({ it.key }, { it.term }))
}
