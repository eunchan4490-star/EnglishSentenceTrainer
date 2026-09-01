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
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth().height(190.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AndroidView(
                factory = { HandwritingCanvasView(it).also { view -> canvasView.value = view } },
                modifier = Modifier.weight(1.05f).fillMaxHeight()
                    .border(2.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium)
            )
            Column(
                Modifier.weight(0.95f).fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (showTarget) "이 단어를 쓰세요" else "제시 음", fontSize = 14.sp)
                        Text(
                            displayPrompt(question.prompt, showTarget),
                            fontSize = if (showTarget) 27.sp else 31.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                    }
                }
                Spacer(Modifier.height(5.dp))
                Text(message, fontSize = 14.sp, textAlign = TextAlign.Center, color = when (correct) {
                    true -> Color(0xFF2E7D32)
                    false -> MaterialTheme.colorScheme.error
                    null -> MaterialTheme.colorScheme.onSurfaceVariant
                })
                if (recognized.isNotBlank()) Text("인식: $recognized", fontSize = 13.sp, maxLines = 1)
                Spacer(Modifier.weight(1f))
                OutlinedButton(onClick = { clearWriting() }, modifier = Modifier.fillMaxWidth().height(43.dp)) {
                    Text("지우기")
                }
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = { if (correct == true) nextQuestion() else attack() },
                    enabled = !checking,
                    modifier = Modifier.fillMaxWidth().height(43.dp)
                ) {
                    Text(when {
                        checking -> "인식 중"
                        correct == true && index == questions.lastIndex -> "보스 처치"
                        correct == true -> "다음 문제"
                        else -> "단어 공격"
                    })
                }
            }
        }
        Box(
            Modifier.fillMaxWidth().height(125.dp).padding(top = 4.dp)
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

private fun displayPrompt(prompt: String, showTarget: Boolean): String {
    if (showTarget) return prompt
    val korean = mapOf(
        "a" to "아", "i" to "이", "u" to "우", "e" to "에", "o" to "오",
        "ka" to "카", "ki" to "키", "ku" to "쿠", "ke" to "케", "ko" to "코",
        "sa" to "사", "shi" to "시", "su" to "스", "se" to "세", "so" to "소",
        "ta" to "타", "chi" to "치", "tsu" to "쓰", "te" to "테", "to" to "토",
        "na" to "나", "ni" to "니", "nu" to "누", "ne" to "네", "no" to "노",
        "ha" to "하", "hi" to "히", "fu" to "후", "he" to "헤", "ho" to "호",
        "ma" to "마", "mi" to "미", "mu" to "무", "me" to "메", "mo" to "모",
        "ya" to "야", "yu" to "유", "yo" to "요", "ra" to "라", "ri" to "리",
        "ru" to "루", "re" to "레", "ro" to "로", "wa" to "와", "wo" to "오", "n" to "응",
        "ga" to "가", "gi" to "기", "gu" to "구", "ge" to "게", "go" to "고",
        "za" to "자", "ji" to "지", "zu" to "즈", "ze" to "제", "zo" to "조",
        "da" to "다", "de" to "데", "do" to "도", "ba" to "바", "bi" to "비",
        "bu" to "부", "be" to "베", "bo" to "보", "pa" to "파", "pi" to "피",
        "pu" to "푸", "pe" to "페", "po" to "포"
    )[prompt] ?: prompt
    return "$korean ($prompt)"
}
