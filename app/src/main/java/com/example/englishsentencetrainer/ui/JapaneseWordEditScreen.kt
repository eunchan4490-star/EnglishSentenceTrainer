package com.example.englishsentencetrainer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.englishsentencetrainer.japanese.JapaneseQuestion
import com.example.englishsentencetrainer.japanese.JapaneseStudyData

@Composable
fun JapaneseWordEditScreen(initialText: String, onBack: () -> Unit, onHome: () -> Unit, onStart: (List<JapaneseQuestion>) -> Unit) {
    var text by remember(initialText) { mutableStateOf(initialText) }
    var error by remember { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(20.dp)) {
        AppNavigationButtons(onBack, onHome)
        Text("일본어 단어 확인", fontSize = 27.sp)
        Text("문제가 아닌 단어는 지우고, 연습할 단어만 남기세요.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(
            value = text, onValueChange = { text = it; error = null },
            modifier = Modifier.fillMaxWidth().heightIn(min = 280.dp, max = 450.dp),
            textStyle = LocalTextStyle.current.copy(fontSize = 22.sp),
            placeholder = { Text("ねこ\nテレビ\nがっこう") }
        )
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Spacer(Modifier.height(12.dp))
        Button(onClick = {
            val questions = JapaneseStudyData.textbookQuestions(text)
            if (questions.isEmpty()) error = "연습할 일본어 단어를 입력하세요." else onStart(questions)
        }, modifier = Modifier.fillMaxWidth().height(60.dp)) { Text("손글씨 학습 시작", fontSize = 20.sp) }
    }
}
