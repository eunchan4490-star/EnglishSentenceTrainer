package com.example.englishsentencetrainer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.englishsentencetrainer.quiz.AnswerState
import com.example.englishsentencetrainer.quiz.QuizViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuizScreen(viewModel: QuizViewModel, onHome: () -> Unit) {
    if (viewModel.completed) {
        Column(Modifier.fillMaxSize().padding(24.dp), Arrangement.Center, Alignment.CenterHorizontally) {
            Text("학습 완료", fontSize = 34.sp)
            Spacer(Modifier.height(24.dp))
            Text("총 문장 수: ${viewModel.sentences.size}", fontSize = 22.sp)
            Text("정답 완료: ${viewModel.sentences.size}", fontSize = 22.sp)
            Spacer(Modifier.height(36.dp))
            Button(onClick = onHome) { Text("처음으로", fontSize = 20.sp) }
        }
        return
    }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("${viewModel.currentIndex + 1} / ${viewModel.sentences.size}", fontSize = 22.sp)
        Spacer(Modifier.height(20.dp))
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
            else -> Spacer(Modifier.height(30.dp))
        }
        HorizontalDivider(Modifier.padding(vertical = 12.dp))
        FlowRow(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            viewModel.available.forEach { word ->
                OutlinedButton(onClick = { viewModel.select(word) }) { Text(word.text, fontSize = 20.sp) }
            }
        }
        if (viewModel.answerState == AnswerState.CORRECT) {
            Button(onClick = viewModel::next, modifier = Modifier.fillMaxWidth().height(60.dp)) {
                Text("다음 문장", fontSize = 20.sp)
            }
        } else {
            Button(
                onClick = viewModel::check,
                enabled = viewModel.available.isEmpty(),
                modifier = Modifier.fillMaxWidth().height(60.dp)
            ) { Text("정답 확인", fontSize = 20.sp) }
        }
    }
}
