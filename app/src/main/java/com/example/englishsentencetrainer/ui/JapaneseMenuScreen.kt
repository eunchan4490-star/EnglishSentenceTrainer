package com.example.englishsentencetrainer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun JapaneseMenuScreen(onBack: () -> Unit, onCamera: () -> Unit, onHiragana: () -> Unit, onKatakana: () -> Unit) {
    Column(Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp)) {
        TextButton(onClick = onBack) { Text("← 처음으로") }
        Text("일본어 손글씨 암기", fontSize = 30.sp)
        Text("음을 보고 화면에 직접 써 보세요.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(36.dp))
        Button(onClick = onCamera, modifier = Modifier.fillMaxWidth().height(64.dp)) { Text("교과서 단어 촬영", fontSize = 20.sp) }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onHiragana, modifier = Modifier.fillMaxWidth().height(60.dp)) { Text("히라가나 전체 연습", fontSize = 20.sp) }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onKatakana, modifier = Modifier.fillMaxWidth().height(60.dp)) { Text("가타카나 전체 연습", fontSize = 20.sp) }
    }
}
