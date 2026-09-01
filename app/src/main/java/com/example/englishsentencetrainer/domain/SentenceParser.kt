package com.example.englishsentencetrainer.domain

object SentenceParser {
    private val sentenceRegex = Regex("[^.!?]+[.!?]+|[^.!?]+$")
    private val whitespace = Regex("\\s+")

    fun parse(text: String): List<String> = sentenceRegex.findAll(text)
        .map { it.value.trim().replace(whitespace, " ") }
        .filter { tokenize(it).size > 1 }
        .toList()

    fun tokenize(sentence: String): List<String> = sentence.trim()
        .split(whitespace)
        .filter(String::isNotBlank)
}
