package com.example.englishsentencetrainer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MainScreen(hasSavedText: Boolean, onCamera: () -> Unit, onSavedText: () -> Unit, onSample: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("영어 본문 암기", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(48.dp))
        Button(onClick = onCamera, modifier = Modifier.fillMaxWidth().height(64.dp)) {
            Text("본문 촬영하기", fontSize = 20.sp)
        }
        if (hasSavedText) {
            Spacer(Modifier.height(16.dp))
            Button(onClick = onSavedText, modifier = Modifier.fillMaxWidth().height(64.dp)) {
                Text("저장된 본문 다시 학습", fontSize = 20.sp)
            }
        }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onSample, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("샘플 본문으로 테스트", fontSize = 18.sp)
        }
    }
}
