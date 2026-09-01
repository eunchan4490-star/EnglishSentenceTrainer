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

    @Test fun splitsLongSentenceAtColonSoTranslationAndWordChoicesMatch() {
        val text = "There are also two psychological theories that might explain why we cheer for a sports team: basking in reflected glory and optimal distinctiveness."
        assertEquals(
            listOf(
                "There are also two psychological theories that might explain why we cheer for a sports team:",
                "basking in reflected glory and optimal distinctiveness."
            ),
            SentenceParser.parse(text)
        )
    }

    @Test fun splitsLongSentenceAtSemicolon() {
        val text = "Many people support the same team throughout their entire lives; this shared loyalty can create a powerful sense of community."
        assertEquals(
            listOf(
                "Many people support the same team throughout their entire lives;",
                "this shared loyalty can create a powerful sense of community."
            ),
            SentenceParser.parse(text)
        )
    }

    @Test fun removesKoreanTextBeforeCreatingQuestions() {
        val text = "Many people 사람들은 support 응원한다 sports teams. 이것은 설명입니다."
        assertEquals(
            listOf("Many people support sports teams."),
            SentenceParser.parse(text)
        )
    }

    @Test fun removesRoundAndSquareBracketAnnotations() {
        val text = "People cheer for teams (bask in reflected glory) every day. They build [social identity theory] strong communities."
        assertEquals(
            listOf(
                "People cheer for teams every day.",
                "They build strong communities."
            ),
            SentenceParser.parse(text)
        )
    }

    @Test fun removesNestedAndFullWidthBracketAnnotations() {
        val text = "People (ignore [all of this]) support teams. Fans （한국어 설명） cheer loudly."
        assertEquals(
            listOf("People support teams.", "Fans cheer loudly."),
            SentenceParser.parse(text)
        )
    }
}
