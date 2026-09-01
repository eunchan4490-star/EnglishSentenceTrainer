package com.example.englishsentencetrainer.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.englishsentencetrainer.data.LocalTextRepository
import com.example.englishsentencetrainer.japanese.JapaneseQuestion
import com.example.englishsentencetrainer.japanese.JapaneseStudyData
import com.example.englishsentencetrainer.ocr.OcrMode
import com.example.englishsentencetrainer.quiz.QuizViewModel

private enum class Screen { MAIN, CAMERA, EDIT, QUIZ, JAPANESE_MENU, JAPANESE_CAMERA, JAPANESE_EDIT, JAPANESE_QUIZ }
const val SAMPLE_TEXT = """Many people use smartphones every day.
They help us communicate with other people.
However, excessive smartphone use can cause problems."""

@Composable
fun EnglishTrainerApp(quizViewModel: QuizViewModel = viewModel()) {
    val context = LocalContext.current
    val textRepository = remember { LocalTextRepository(context.applicationContext) }
    var screen by remember { mutableStateOf(Screen.MAIN) }
    var ocrText by remember { mutableStateOf("") }
    var savedText by remember { mutableStateOf(textRepository.load()) }
    var japaneseText by remember { mutableStateOf("") }
    var japaneseQuestions by remember { mutableStateOf<List<JapaneseQuestion>>(emptyList()) }
    var japaneseTitle by remember { mutableStateOf("") }
    var showJapaneseTarget by remember { mutableStateOf(false) }

    MaterialTheme {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            when (screen) {
                Screen.MAIN -> MainScreen(
                    onCamera = { screen = Screen.CAMERA },
                    hasSavedText = savedText.isNotBlank(),
                    onSavedText = { ocrText = savedText; screen = Screen.EDIT },
                    onSample = { ocrText = SAMPLE_TEXT; screen = Screen.EDIT },
                    onJapanese = { screen = Screen.JAPANESE_MENU }
                )
                Screen.CAMERA -> CameraScreen(
                    onRecognized = { ocrText = it; screen = Screen.EDIT },
                    onBack = { screen = Screen.MAIN }
                )
                Screen.EDIT -> OcrEditScreen(
                    initialText = ocrText,
                    onBack = { screen = Screen.MAIN },
                    onStart = { text ->
                        val started = quizViewModel.start(text)
                        if (started) {
                            textRepository.save(text)
                            savedText = text.trim()
                            screen = Screen.QUIZ
                        }
                        started
                    }
                )
                Screen.QUIZ -> QuizScreen(
                    viewModel = quizViewModel,
                    onHome = { screen = Screen.MAIN }
                )
                Screen.JAPANESE_MENU -> JapaneseMenuScreen(
                    onBack = { screen = Screen.MAIN },
                    onCamera = { screen = Screen.JAPANESE_CAMERA },
                    onHiragana = {
                        japaneseQuestions = JapaneseStudyData.hiragana.shuffled()
                        japaneseTitle = "히라가나"
                        showJapaneseTarget = false
                        screen = Screen.JAPANESE_QUIZ
                    },
                    onKatakana = {
                        japaneseQuestions = JapaneseStudyData.katakana.shuffled()
                        japaneseTitle = "가타카나"
                        showJapaneseTarget = false
                        screen = Screen.JAPANESE_QUIZ
                    }
                )
                Screen.JAPANESE_CAMERA -> CameraScreen(
                    onRecognized = { japaneseText = it; screen = Screen.JAPANESE_EDIT },
                    onBack = { screen = Screen.JAPANESE_MENU },
                    mode = OcrMode.JAPANESE
                )
                Screen.JAPANESE_EDIT -> JapaneseWordEditScreen(
                    initialText = japaneseText,
                    onBack = { screen = Screen.JAPANESE_MENU },
                    onStart = {
                        japaneseQuestions = it.shuffled()
                        japaneseTitle = "교과서 단어"
                        showJapaneseTarget = true
                        screen = Screen.JAPANESE_QUIZ
                    }
                )
                Screen.JAPANESE_QUIZ -> JapaneseHandwritingQuizScreen(
                    questions = japaneseQuestions,
                    title = japaneseTitle,
                    showTarget = showJapaneseTarget,
                    onHome = { screen = Screen.JAPANESE_MENU }
                )
            }
        }
    }
}
