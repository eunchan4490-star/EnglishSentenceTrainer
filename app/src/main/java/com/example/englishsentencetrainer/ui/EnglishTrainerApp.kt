package com.example.englishsentencetrainer.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.englishsentencetrainer.data.LocalTextRepository
import com.example.englishsentencetrainer.quiz.QuizViewModel

private enum class Screen { MAIN, CAMERA, EDIT, QUIZ }
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

    MaterialTheme {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            when (screen) {
                Screen.MAIN -> MainScreen(
                    onCamera = { screen = Screen.CAMERA },
                    hasSavedText = savedText.isNotBlank(),
                    onSavedText = { ocrText = savedText; screen = Screen.EDIT },
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
            }
        }
    }
}
