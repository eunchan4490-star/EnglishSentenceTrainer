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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
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
    var message by remember { mutableStateOf("손글씨로 단어 공격을 준비하세요!") }
    var completed by remember { mutableStateOf(false) }
    DisposableEffect(Unit) { onDispose { manager.close() } }

    if (completed) {
        CompletionBattleScreen(questions.size, combo, onBack, onHome)
        return
    }

    val question = questions[index]
    fun clearWriting(newMessage: String = "다시 써 보세요.") {
        canvasView.value?.clearInk()
        correct = null
        recognized = ""
        message = newMessage
    }
    fun nextQuestion() {
        if (index == questions.lastIndex) completed = true
        else {
            index++
            clearWriting("다음 문제로 공격 준비!")
        }
    }
    fun attack() {
        val view = canvasView.value ?: return
        if (!view.hasInk() || checking) return
        checking = true
        message = "손글씨 분석 중…"
        manager.recognize(
            view.ink(),
            onSuccess = { candidates ->
                checking = false
                recognized = candidates.firstOrNull().orEmpty()
                val matched = candidates.take(5).any { normalize(it) == normalize(question.answer) }
                correct = matched
                if (matched) {
                    combo++
                    message = "정답! 단어 공격 성공 -1"
                } else {
                    combo = 0
                    heroHp = (heroHp - 1).coerceAtLeast(0)
                    message = if (heroHp == 0) "보스의 반격! 체력을 회복했습니다." else "오답! 보스의 반격 -1"
                    if (heroHp == 0) heroHp = 3
                }
            },
            onError = {
                checking = false
                recognized = "인식 모델 준비 실패"
                correct = false
                message = "인터넷 연결 후 다시 시도하세요."
            }
        )
    }

    Column(
        Modifier.fillMaxSize().safeDrawingPadding().background(Color(0xFFFFFBFF)).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppNavigationButtons(onBack, onHome)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text("${index + 1} / ${questions.size}", fontSize = 18.sp)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("영웅 ${"♥".repeat(heroHp)}", color = Color(0xFFC62828), fontSize = 17.sp)
            Text("보스 HP ${questions.size - index} / ${questions.size}", fontSize = 17.sp)
        }
        Spacer(Modifier.height(6.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.secondaryContainer,
            shape = MaterialTheme.shapes.medium
        ) {
            Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (showTarget) "아래 일본어 단어를 그대로 쓰세요" else "아래 음에 맞는 일본어 글자를 쓰세요", fontSize = 15.sp)
                Text(question.prompt, fontSize = 36.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 2)
            }
        }
        Spacer(Modifier.height(8.dp))
        AndroidView(
            factory = { HandwritingCanvasView(it).also { view -> canvasView.value = view } },
            modifier = Modifier.fillMaxWidth().weight(1f).heightIn(min = 210.dp, max = 330.dp)
                .border(2.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium)
        )
        Spacer(Modifier.height(6.dp))
        Text(message, fontSize = 16.sp, color = when (correct) {
            true -> Color(0xFF2E7D32)
            false -> MaterialTheme.colorScheme.error
            null -> MaterialTheme.colorScheme.onSurfaceVariant
        })
        if (recognized.isNotBlank()) Text("인식 결과: $recognized", fontSize = 14.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { clearWriting() }, modifier = Modifier.weight(1f).height(50.dp)) { Text("지우기") }
            Button(
                onClick = { if (correct == true) nextQuestion() else attack() },
                enabled = !checking,
                modifier = Modifier.weight(1.4f).height(50.dp)
            ) {
                Text(when {
                    checking -> "인식 중"
                    correct == true && index == questions.lastIndex -> "보스 처치"
                    correct == true -> "다음 문제"
                    else -> "단어 공격"
                })
            }
        }
        Box(
            Modifier.fillMaxWidth().height(105.dp).padding(top = 6.dp)
                .background(Color(0xFFF0E8F7), MaterialTheme.shapes.medium),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.japanese_battle_sprites),
                contentDescription = "도트 영웅과 책 보스의 전투",
                modifier = Modifier.fillMaxSize().padding(3.dp), contentScale = ContentScale.Fit
            )
            Text("COMBO $combo", modifier = Modifier.align(Alignment.TopCenter).padding(3.dp), color = Color(0xFF5E35B1), fontSize = 14.sp)
        }
    }
}

@Composable
private fun CompletionBattleScreen(total: Int, combo: Int, onBack: () -> Unit, onHome: () -> Unit) {
    Column(
        Modifier.fillMaxSize().safeDrawingPadding().background(Color(0xFFFFFBFF)).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
    ) {
        AppNavigationButtons(onBack, onHome)
        Image(
            painter = painterResource(R.drawable.japanese_battle_sprites), contentDescription = null,
            modifier = Modifier.fillMaxWidth().height(220.dp), contentScale = ContentScale.Fit
        )
        Text("보스 처치!", fontSize = 36.sp, color = Color(0xFF5E35B1))
        Text("총 ${total}문제 완료", fontSize = 21.sp)
        Text("마지막 콤보 $combo", fontSize = 18.sp)
        Spacer(Modifier.height(18.dp))
        Button(onClick = onHome, modifier = Modifier.fillMaxWidth().height(54.dp)) { Text("홈으로") }
    }
}

private fun normalize(value: String): String =
    Normalizer.normalize(value, Normalizer.Form.NFKC).replace(" ", "").trim()
