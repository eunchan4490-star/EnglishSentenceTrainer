package com.example.englishsentencetrainer.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import com.example.englishsentencetrainer.quiz.QuizViewModel

private enum class Screen { MAIN, CAMERA, EDIT, QUIZ }
const val SAMPLE_TEXT = """Many people use smartphones every day.
They help us communicate with other people.
However, excessive smartphone use can cause problems."""

@Composable
fun EnglishTrainerApp(quizViewModel: QuizViewModel = viewModel()) {
    var screen by remember { mutableStateOf(Screen.MAIN) }
    var ocrText by remember { mutableStateOf("") }

    MaterialTheme {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            when (screen) {
                Screen.MAIN -> MainScreen(
                    onCamera = { screen = Screen.CAMERA },
                    onSample = { ocrText = SAMPLE_TEXT; screen = Screen.EDIT }
                )
                Screen.CAMERA -> CameraScreen(
                    onRecognized = { ocrText = it; screen = Screen.EDIT },
                    onBack = { screen = Screen.MAIN }
                )
                Screen.EDIT -> OcrEditScreen(
                    initialText = ocrText,
                    onBack = { screen = Screen.MAIN },
                    onStart = { text ->
                        if (quizViewModel.start(text)) screen = Screen.QUIZ
                        quizViewModel.sentences.isNotEmpty()
                    }
                )
                Screen.QUIZ -> QuizScreen(
                    viewModel = quizViewModel,
                    onHome = { screen = Screen.MAIN }
                )
            }
        }
    }
}
