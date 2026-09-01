package com.example.englishsentencetrainer.quiz

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.englishsentencetrainer.domain.SentenceParser

data class WordChoice(val id: Int, val text: String)
enum class AnswerState { NONE, WRONG, CORRECT }

class QuizViewModel : ViewModel() {
    var sentences by mutableStateOf(emptyList<String>()); private set
    var currentIndex by mutableStateOf(0); private set
    var available by mutableStateOf(emptyList<WordChoice>()); private set
    var selected by mutableStateOf(emptyList<WordChoice>()); private set
    var answerState by mutableStateOf(AnswerState.NONE); private set
    var completed by mutableStateOf(false); private set

    fun start(text: String): Boolean {
        sentences = SentenceParser.parse(text)
        currentIndex = 0; completed = false; answerState = AnswerState.NONE
        if (sentences.isNotEmpty()) loadQuestion()
        return sentences.isNotEmpty()
    }

    private fun loadQuestion() {
        val original = SentenceParser.tokenize(sentences[currentIndex]).mapIndexed(::WordChoice)
        var shuffled = original.shuffled()
        while (shuffled == original) shuffled = original.shuffled()
        available = shuffled; selected = emptyList(); answerState = AnswerState.NONE
    }

    fun select(word: WordChoice) {
        if (answerState == AnswerState.CORRECT) return
        available = available - word; selected = selected + word; answerState = AnswerState.NONE
    }

    fun unselect(word: WordChoice) {
        if (answerState == AnswerState.CORRECT) return
        selected = selected - word; available = available + word; answerState = AnswerState.NONE
    }

    fun check() {
        if (available.isNotEmpty()) return
        val expected = SentenceParser.tokenize(sentences[currentIndex])
        answerState = if (selected.map { it.text } == expected) AnswerState.CORRECT else AnswerState.WRONG
    }

    fun next() {
        if (answerState != AnswerState.CORRECT) return
        if (currentIndex == sentences.lastIndex) completed = true
        else { currentIndex++; loadQuestion() }
    }
}
