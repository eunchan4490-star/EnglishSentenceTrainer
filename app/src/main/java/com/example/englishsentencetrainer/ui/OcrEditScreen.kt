package com.example.englishsentencetrainer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OcrEditScreen(initialText: String, onBack: () -> Unit, onStart: (String) -> Boolean) {
    var text by remember(initialText) { mutableStateOf(initialText) }
    var error by remember { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        TextButton(onClick = onBack) { Text("← 처음으로") }
        Text("OCR 결과 확인", fontSize = 26.sp)
        Text("잘못 인식된 부분을 수정하세요.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = text,
            onValueChange = { text = it; error = null },
            modifier = Modifier.fillMaxWidth().weight(1f),
            textStyle = LocalTextStyle.current.copy(fontSize = 20.sp),
            placeholder = { Text("영어 본문을 입력하세요.") }
        )
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { if (!onStart(text)) error = "두 단어 이상인 영어 문장을 입력하세요." },
            enabled = text.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(60.dp)
        ) { Text("학습 시작", fontSize = 20.sp) }
    }
}
