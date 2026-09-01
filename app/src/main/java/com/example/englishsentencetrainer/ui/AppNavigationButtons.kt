package com.example.englishsentencetrainer.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AppNavigationButtons(onBack: () -> Unit, onHome: () -> Unit, color: Color = Color.Unspecified) {
    Row {
        TextButton(onClick = onBack) { Text("← 이전 단계", color = color) }
        Spacer(Modifier.width(4.dp))
        TextButton(onClick = onHome) { Text("⌂ 홈", color = color) }
    }
}
