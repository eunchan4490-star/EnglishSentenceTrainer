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
        val result = JapaneseStudyData.textbookQuestions("ねこ\ncat 학교\nテレビ\nねこ\n学校")

        assertEquals(listOf("ねこ", "テレビ", "学校"), result.map { it.answer })
    }

    @Test
    fun `textbook parser removes Korean and bracketed meanings`() {
        val text = "あう(会う) 만나다\nあさ[朝] 아침\nアニメ(動畫) 애니메이션"

        assertEquals(
            listOf("あう", "あさ", "アニメ"),
            JapaneseStudyData.textbookQuestions(text).map { it.answer }
        )
    }

    @Test
    fun `textbook parser keeps only leading Japanese headword from OCR noise`() {
        val text = "あがる上がる)己フに\n+あさく_さ浅草)0A早!"

        assertEquals(
            listOf("あがる", "あさく"),
            JapaneseStudyData.textbookQuestions(text).map { it.answer }
        )
    }
}
