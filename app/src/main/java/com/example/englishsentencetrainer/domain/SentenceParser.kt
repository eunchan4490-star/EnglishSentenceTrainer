package com.example.englishsentencetrainer.domain

object SentenceParser {
    private const val LONG_SENTENCE_WORDS = 15
    private const val MIN_COMMA_PART_WORDS = 3
    private val sentenceRegex = Regex("[^.!?\\n]+[.!?]+|[^.!?\\n]+(?:\\n|$)")
    private val whitespace = Regex("\\s+")

    fun parse(text: String): List<String> = sentenceRegex.findAll(markLikelyMissingPeriods(text))
        .map { it.value.trim().replace(whitespace, " ") }
        .filter { tokenize(it).size > 1 }
        .flatMap(::splitLongSentenceAtCommas)
        .toList()

    private fun markLikelyMissingPeriods(text: String): String {
        val lines = text.lines().map(String::trim).filter(String::isNotBlank)
        return buildString {
            lines.forEachIndexed { index, line ->
                append(line)
                val next = lines.getOrNull(index + 1)
                val nextStartsUppercase = next?.firstOrNull(Char::isLetter)?.isUpperCase() == true
                val likelySentenceEnd = line.lastOrNull() in listOf('.', '?', '!') ||
                    (tokenize(line).size >= 3 && nextStartsUppercase)
                if (index < lines.lastIndex) append(if (likelySentenceEnd) '\n' else ' ')
            }
        }
    }

    private fun splitLongSentenceAtCommas(sentence: String): Sequence<String> {
        if (tokenize(sentence).size <= LONG_SENTENCE_WORDS || ',' !in sentence) return sequenceOf(sentence)
        val parts = sentence.split(Regex("(?<=,)\\s+")).map(String::trim)
        return if (parts.size > 1 && parts.all { tokenize(it).size >= MIN_COMMA_PART_WORDS })
            parts.asSequence()
        else sequenceOf(sentence)
    }

    fun tokenize(sentence: String): List<String> = sentence.trim()
        .split(whitespace)
        .filter(String::isNotBlank)
}
