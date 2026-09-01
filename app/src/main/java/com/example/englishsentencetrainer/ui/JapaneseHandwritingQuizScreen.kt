package com.example.englishsentencetrainer.ui

import android.content.pm.ActivityInfo
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.englishsentencetrainer.R
import com.example.englishsentencetrainer.japanese.HandwritingCanvasView
import com.example.englishsentencetrainer.japanese.JapaneseHandwritingManager
import com.example.englishsentencetrainer.japanese.JapaneseQuestion
import java.text.Normalizer

@Composable
fun JapaneseHandwritingQuizScreen(
    questions: List<JapaneseQuestion>, title: String, showTarget: Boolean, onBack: () -> Unit, onHome: () -> Unit
) {
    val activity = LocalActivity.current
    DisposableEffect(activity) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT }
    }
    val manager = remember { JapaneseHandwritingManager() }
    val canvasView = remember { mutableStateOf<HandwritingCanvasView?>(null) }
    var index by remember { mutableIntStateOf(0) }
    var checking by remember { mutableStateOf(false) }
    var correct by remember { mutableStateOf<Boolean?>(null) }
    var recognized by remember { mutableStateOf("") }
    var heroHp by remember { mutableIntStateOf(3) }
    var combo by remember { mutableIntStateOf(0) }
    var battleMessage by remember { mutableStateOf("손글씨 단어로 보스를 공격하세요!") }
    var completed by remember { mutableStateOf(false) }
    DisposableEffect(Unit) { onDispose { manager.close() } }

    if (completed) {
        CompletionBattleScreen(questions.size, combo, onBack, onHome)
        return
    }

    val question = questions[index]
    val bossHp = questions.size - index
    Column(
        Modifier.fillMaxSize().safeDrawingPadding().background(Color(0xFFFFFBFF))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            AppNavigationButtons(onBack, onHome)
            Text(title, fontSize = 19.sp)
            Spacer(Modifier.weight(1f))
            Text("영웅 ${"♥".repeat(heroHp)}", color = Color(0xFFC62828), fontSize = 18.sp)
            Spacer(Modifier.width(20.dp))
            Text("보스 HP $bossHp / ${questions.size}", fontSize = 18.sp)
            Spacer(Modifier.width(20.dp))
            Text("${index + 1} / ${questions.size}", fontSize = 18.sp)
        }
        Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(Modifier.weight(1.25f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (showTarget) "단어를 그대로 쓰세요" else "음에 맞는 글자를 쓰세요", fontSize = 15.sp)
                Text(question.prompt, fontSize = 32.sp, maxLines = 1)
                AndroidView(
                    factory = { HandwritingCanvasView(it).also { view -> canvasView.value = view } },
                    modifier = Modifier.fillMaxWidth().weight(1f)
                        .border(2.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium)
                )
            }
            Column(Modifier.weight(0.75f).fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                Text(battleMessage, fontSize = 17.sp, color = when (correct) {
                    true -> Color(0xFF2E7D32)
                    false -> MaterialTheme.colorScheme.error
                    null -> MaterialTheme.colorScheme.onSurfaceVariant
                })
                if (recognized.isNotBlank()) Text("인식: $recognized", fontSize = 15.sp)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            canvasView.value?.clearInk(); correct = null; recognized = ""; battleMessage = "다시 써 보세요."
                        }, modifier = Modifier.weight(1f).height(50.dp)
                    ) { Text("지우기") }
                    if (correct == true) {
                        Button(
                            onClick = {
                                if (index == questions.lastIndex) completed = true else {
                                    index++; canvasView.value?.clearInk(); correct = null; recognized = ""
                                    battleMessage = "다음 단어로 공격 준비!"
                                }
                            }, modifier = Modifier.weight(1.3f).height(50.dp)
                        ) { Text(if (index == questions.lastIndex) "보스 처치" else "다음 공격") }
                    } else {
                        Button(
                            onClick = {
                                val view = canvasView.value ?: return@Button
                                if (!view.hasInk()) return@Button
                                checking = true; battleMessage = "손글씨 분석 중…"
                                manager.recognize(
                                    view.ink(), view.width.toFloat(), view.height.toFloat(),
                                    onSuccess = { candidates ->
                                        checking = false
                                        recognized = candidates.firstOrNull().orEmpty()
                                        val matched = candidates.take(5).any { normalize(it) == normalize(question.answer) }
                                        correct = matched
                                        if (matched) {
                                            combo++; battleMessage = "정답! 단어 공격 성공 -1"
                                        } else {
                                            combo = 0; heroHp = (heroHp - 1).coerceAtLeast(0)
                                            battleMessage = if (heroHp == 0) "보스의 반격! 체력 회복 후 다시 도전" else "오답! 보스의 반격 -1"
                                            if (heroHp == 0) heroHp = 3
                                        }
                                    },
                                    onError = {
                                        checking = false; recognized = "모델 다운로드 실패"; correct = false
                                        battleMessage = "인터넷 연결 후 다시 시도하세요."
                                    }
                                )
                            }, enabled = !checking, modifier = Modifier.weight(1.3f).height(50.dp)
                        ) { Text(if (checking) "인식 중" else "단어 공격") }
                    }
                }
            }
        }
        Box(
            Modifier.fillMaxWidth().heightIn(min = 105.dp, max = 145.dp)
                .background(Color(0xFFF0E8F7), MaterialTheme.shapes.medium),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.japanese_battle_sprites),
                contentDescription = "도트 영웅과 책 보스의 전투",
                modifier = Modifier.fillMaxSize().padding(4.dp), contentScale = ContentScale.Fit
            )
            Text("COMBO $combo", modifier = Modifier.align(Alignment.TopCenter).padding(5.dp), color = Color(0xFF5E35B1), fontSize = 15.sp)
        }
    }
}

@Composable
private fun CompletionBattleScreen(total: Int, combo: Int, onBack: () -> Unit, onHome: () -> Unit) {
    Row(
        Modifier.fillMaxSize().safeDrawingPadding().background(Color(0xFFFFFBFF)).padding(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.japanese_battle_sprites), contentDescription = null,
            modifier = Modifier.weight(1.2f).fillMaxHeight(), contentScale = ContentScale.Fit
        )
        Column(Modifier.weight(0.8f), horizontalAlignment = Alignment.CenterHorizontally) {
            AppNavigationButtons(onBack, onHome)
            Text("보스 처치!", fontSize = 36.sp, color = Color(0xFF5E35B1))
            Text("총 ${total}문제 완료", fontSize = 21.sp)
            Text("마지막 콤보 $combo", fontSize = 18.sp)
            Spacer(Modifier.height(18.dp))
            Button(onClick = onHome, modifier = Modifier.fillMaxWidth().height(54.dp)) { Text("홈으로") }
        }
    }
}

private fun normalize(value: String): String =
    Normalizer.normalize(value, Normalizer.Form.NFKC).replace(" ", "").trim()
