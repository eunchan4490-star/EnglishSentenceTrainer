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

    @Test fun treatsUppercaseNewLineAsLikelyMissingPeriod() {
        val text = "Many people use smartphones every day\nThey are very useful"
        assertEquals(
            listOf("Many people use smartphones every day", "They are very useful"),
            SentenceParser.parse(text)
        )
    }

    @Test fun splitsLongSentenceAtComma() {
        val text = "Many students carry their smartphones to school every single day, teachers sometimes use these devices for useful classroom activities."
        assertEquals(
            listOf(
                "Many students carry their smartphones to school every single day,",
                "teachers sometimes use these devices for useful classroom activities."
            ),
            SentenceParser.parse(text)
        )
    }
}
