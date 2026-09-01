package com.example.englishsentencetrainer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.englishsentencetrainer.quiz.AnswerState
import com.example.englishsentencetrainer.quiz.QuizViewModel
import com.example.englishsentencetrainer.translation.TranslationManager

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuizScreen(viewModel: QuizViewModel, onBack: () -> Unit, onHome: () -> Unit) {
    val translationManager = remember { TranslationManager() }
    DisposableEffect(Unit) { onDispose { translationManager.close() } }
    if (viewModel.completed) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp), Arrangement.Center, Alignment.CenterHorizontally) {
            Text("학습 완료", fontSize = 34.sp)
            Spacer(Modifier.height(24.dp))
            Text("총 문장 수: ${viewModel.sentences.size}", fontSize = 22.sp)
            Text("정답 완료: ${viewModel.sentences.size}", fontSize = 22.sp)
            Spacer(Modifier.height(36.dp))
            Button(onClick = onHome) { Text("처음으로", fontSize = 20.sp) }
        }
        return
    }

    var translation by remember(viewModel.currentIndex) { mutableStateOf<String?>(null) }
    var translationFailed by remember(viewModel.currentIndex) { mutableStateOf(false) }
    LaunchedEffect(viewModel.currentIndex) {
        translationManager.translate(
            viewModel.sentences[viewModel.currentIndex],
            onSuccess = { translation = it },
            onError = { translationFailed = true }
        )
    }

    Column(
        Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(20.dp)
    ) {
        AppNavigationButtons(onBack, onHome)
        Text("${viewModel.currentIndex + 1} / ${viewModel.sentences.size}", fontSize = 22.sp)
        Spacer(Modifier.height(12.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = MaterialTheme.shapes.medium
        ) {
            Column(Modifier.padding(14.dp)) {
                Text(
                    when {
                        translation != null -> translation!!
                        translationFailed -> "번역 모델을 받을 수 없습니다. 인터넷 연결을 확인하세요."
                        else -> "한국어 번역 준비 중…"
                    },
                    fontSize = 20.sp
                )
                if (translation != null) {
                    Spacer(Modifier.height(4.dp))
                    Text("Powered by Google Translate", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("답안", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Surface(
            modifier = Modifier.fillMaxWidth().heightIn(min = 130.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium
        ) {
            FlowRow(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                viewModel.selected.forEach { word ->
                    ElevatedButton(onClick = { viewModel.unselect(word) }) { Text(word.text, fontSize = 20.sp) }
                }
            }
        }
        when (viewModel.answerState) {
            AnswerState.CORRECT -> Text("정답!", color = MaterialTheme.colorScheme.primary, fontSize = 24.sp)
            AnswerState.WRONG -> Text("다시 시도", color = MaterialTheme.colorScheme.error, fontSize = 24.sp)
            else -> Text(
                if (viewModel.available.isEmpty()) "순서를 확인한 뒤 정답을 확인하세요."
                else "아래 단어를 눌러 문장을 완성하세요.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        if (viewModel.answerState == AnswerState.CORRECT) {
            Button(onClick = viewModel::next, modifier = Modifier.fillMaxWidth().height(60.dp)) {
                Text(
                    if (viewModel.currentIndex == viewModel.sentences.lastIndex) "학습 완료" else "다음 문장",
                    fontSize = 20.sp
                )
            }
        } else {
            Button(
                onClick = viewModel::check,
                enabled = viewModel.available.isEmpty(),
                modifier = Modifier.fillMaxWidth().height(60.dp)
            ) { Text("정답 확인", fontSize = 20.sp) }
        }
        HorizontalDivider(Modifier.padding(vertical = 20.dp))
        Text(
            if (viewModel.available.isEmpty()) "모든 단어를 선택했습니다." else "남은 단어 ${viewModel.available.size}개",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 18.sp
        )
        Spacer(Modifier.height(10.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            viewModel.available.forEach { word ->
                OutlinedButton(onClick = { viewModel.select(word) }) { Text(word.text, fontSize = 20.sp) }
            }
        }
    }
}
