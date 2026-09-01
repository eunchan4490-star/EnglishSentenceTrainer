package com.example.englishsentencetrainer.japanese

object JapaneseTextSanitizer {
    private val bracketed = Regex("\\([^()]*\\)|\\[[^\\[\\]]*]|（[^（）]*）|［[^［］]*］")
    private val korean = Regex("[\\u1100-\\u11FF\\u3130-\\u318F\\uAC00-\\uD7AF]+")
    private val leadingJapanese = Regex("^[^ぁ-ゖァ-ヺ一-龯々〆ヵヶ]*([ぁ-ゖー]+|[ァ-ヺー]+|[一-龯々〆ヵヶ]+)")

    fun cleanLines(lines: List<String>): String = lines.mapNotNull(::cleanHeadword).joinToString("\n")

    fun cleanHeadword(line: String): String? {
        var cleaned = line
        do {
            val previous = cleaned
            cleaned = cleaned.replace(bracketed, " ")
        } while (cleaned != previous)
        cleaned = cleaned.replace(korean, " ").trim()
        return leadingJapanese.find(cleaned)?.groupValues?.get(1)?.takeIf(String::isNotBlank)
    }
}
