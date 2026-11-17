package com.sgh.wordwatch.core

/**
 * 붙여넣은 단어 리스트를 한 줄에 한 단어씩 [Word]로 바꾼다.
 *
 * 인식하는 형식 (한 줄 기준):
 *  - `apple\t사과`              (탭, 엑셀/구글시트 복사)
 *  - `apple - 사과`, `apple – 사과`
 *  - `apple: 사과`, `apple = 사과`, `apple, 사과`
 *  - `apple 사과`, `give up 포기하다`  (영어가 끝나고 한글/다른 글자가 시작하는 곳에서 나눔)
 *  - `1. apple 사과`, `2) apple - 사과` (앞 번호 제거)
 *  - `apple`                     (뜻 없이 영어만)
 */
object WordParser {

    private val numbering = Regex("""^\s*(\d+\s*[.)\]:-]|[-*•·]\s)\s*""")
    private val dashSeparator = Regex("""\s+[-–—]+\s+""")
    private val edgePunct = " \t:=,;-–—|/".toCharArray()
    private val simpleSeparators = listOf(":", "=", ",", ";", "|")

    fun parse(text: String, now: Long = 0L): List<Word> =
        text.lineSequence().mapNotNull { parseLine(it, now) }.toList()

    fun parseLine(rawLine: String, now: Long = 0L): Word? {
        var line = rawLine.replace(' ', ' ').trim()
        if (line.isEmpty()) return null
        line = numbering.replaceFirst(line, "").trim()
        if (line.isEmpty()) return null

        val (term, meaning) = split(line)
        val t = term.trim(*edgePunct).replace(Regex("""\s+"""), " ")
        val m = meaning.trim(*edgePunct).trim()
        if (t.isEmpty() || !t.any { it.isAsciiLetter() }) return null
        return Word(term = t, meaning = m, addedAt = now)
    }

    private fun split(line: String): Pair<String, String> {
        // 1) 탭
        line.indexOf('\t').takeIf { it > 0 }?.let { return line.substring(0, it) to line.substring(it + 1) }

        // 2) 띄어쓴 대시 " - "
        dashSeparator.find(line)?.let { return line.substring(0, it.range.first) to line.substring(it.range.last + 1) }

        // 3) 영어 다음에 한글 등 영어가 아닌 글자가 시작하는 지점
        val firstForeign = line.indexOfFirst { it.isLetter() && !it.isAsciiLetter() }
        if (firstForeign > 0) {
            // "able ~할 수 있는" 처럼 뜻이 기호로 시작하면 그 기호까지 뜻에 포함
            var cut = firstForeign
            while (cut > 0 && !line[cut - 1].isWhitespace() && !line[cut - 1].isLetterOrDigit()) cut--
            if (cut == 0) cut = firstForeign
            return line.substring(0, cut) to line.substring(cut)
        }

        // 4) 영어만 있는 줄: ":" "=" "," 등으로 나눔 (예: "apple: a fruit")
        for (sep in simpleSeparators) {
            val i = line.indexOf(sep)
            if (i > 0) return line.substring(0, i) to line.substring(i + sep.length)
        }

        // 5) 구분자 없음 → 전체가 단어 (구 동사 "give up" 같은 경우 포함)
        return line to ""
    }

    private fun Char.isAsciiLetter() = this in 'a'..'z' || this in 'A'..'Z'
}
