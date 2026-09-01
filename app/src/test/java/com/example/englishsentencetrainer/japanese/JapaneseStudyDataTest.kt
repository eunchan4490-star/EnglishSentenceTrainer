package com.example.englishsentencetrainer.japanese

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JapaneseStudyDataTest {
    @Test
    fun `kana sets contain the complete basic rows and voiced sounds`() {
        assertEquals(71, JapaneseStudyData.hiragana.size)
        assertEquals(71, JapaneseStudyData.katakana.size)
        assertEquals(JapaneseStudyData.hiragana.size, JapaneseStudyData.hiragana.map { it.answer }.distinct().size)
        assertEquals(JapaneseStudyData.katakana.size, JapaneseStudyData.katakana.map { it.answer }.distinct().size)
        assertTrue(JapaneseStudyData.hiragana.any { it.answer == "あ" })
        assertTrue(JapaneseStudyData.hiragana.any { it.answer == "ぽ" })
        assertTrue(JapaneseStudyData.katakana.any { it.answer == "ア" })
        assertTrue(JapaneseStudyData.katakana.any { it.answer == "ポ" })
    }

    @Test
    fun `textbook parser keeps Japanese words and removes duplicates`() {
        val result = JapaneseStudyData.textbookQuestions("ねこ, cat 학교 テレビ ねこ。学校")

        assertEquals(listOf("ねこ", "テレビ", "学校"), result.map { it.answer })
    }
}
