package com.example.englishsentencetrainer.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class SentenceParserTest {
    @Test fun splitsAndRemovesSingleWordSentences() {
        val text = "Many people use smartphones every day. Alone! How are you?"
        assertEquals(listOf("Many people use smartphones every day.", "How are you?"), SentenceParser.parse(text))
    }

    @Test fun keepsContractionsAndPunctuationAttached() {
        assertEquals(listOf("Hello,", "I'm", "Tom."), SentenceParser.tokenize("Hello, I'm Tom."))
    }
}
