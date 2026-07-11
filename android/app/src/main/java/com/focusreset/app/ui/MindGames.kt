package com.focusreset.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusreset.app.domain.*
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue
import kotlin.random.Random

@Composable
fun GameRunScreen(state: AppUiState, onComplete: (GameResult) -> Unit) {
    val game = state.seed.games[state.gameIndex]
    Column(Modifier.fillMaxSize().padding(22.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(if (state.recoveryRun) "RESET ROUND" else if (state.practice) "PRACTICE" else "DAILY FOCUS RUN", color = Mint, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp, fontSize = 12.sp)
            Text("${state.gameIndex + 1} / ${state.seed.games.size}", color = Slate)
        }
        LinearProgressIndicator(progress = { (state.gameIndex + .2f) / state.seed.games.size }, modifier = Modifier.fillMaxWidth(), color = Mint, trackColor = Color(0xFFE2EAE7))
        Text(game.displayName(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        Text(game.instructions(), color = Slate)
        Card(Modifier.fillMaxWidth().weight(1f), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(24.dp)) {
            Box(Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
                key(state.gameIndex, state.seed.seed) {
                    when (game) {
                        GameType.COLOR_CLASH -> ColorClash(state.seed.seed, onComplete)
                        GameType.MEMORY_GRID -> MemoryGrid(state.seed.seed, onComplete)
                        GameType.SIGNAL_WATCH -> SignalWatch(state.seed.seed, onComplete)
                        GameType.RULE_SHIFT -> RuleShift(state.seed.seed, onComplete)
                        GameType.STORY_RECALL -> StoryRecall(state.seed.seed, onComplete)
                    }
                }
            }
        }
        Text("Game performance only—not a medical or intelligence measurement.", color = Slate, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}

@Composable private fun ColorClash(seed: Long, done: (GameResult) -> Unit) {
    val colors = listOf(Color(0xFFE95656), Color(0xFF4C78E8), Color(0xFF36A86B), Color(0xFFE6A82D))
    val labels = listOf("RED", "BLUE", "GREEN", "AMBER")
    var round by remember { mutableIntStateOf(0) }
    var correct by remember { mutableIntStateOf(0) }
    var wrong by remember { mutableIntStateOf(0) }
    val times = remember { mutableStateListOf<Long>() }
    var shownAt by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val data = GameSeeds.colorRound(seed, round)
    fun answer(index: Int) {
        times += System.currentTimeMillis() - shownAt
        if (index == data.second) correct++ else wrong++
        if (round == 7) done(GameResult(GameType.COLOR_CLASH, correct, wrong, 0, times.sum(), times.toList()))
        else { round++; shownAt = System.currentTimeMillis() }
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(28.dp)) {
        Text("Choose the INK color", color = Slate)
        Text(data.first, color = colors[data.second], fontSize = 48.sp, fontWeight = FontWeight.Black)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            labels.chunked(2).forEachIndexed { row, chunk ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    chunk.forEachIndexed { col, label ->
                        val index = row * 2 + col
                        OutlinedButton(onClick = { answer(index) }, modifier = Modifier.width(120.dp)) { Text(label) }
                    }
                }
            }
        }
    }
}

@Composable private fun MemoryGrid(seed: Long, done: (GameResult) -> Unit) {
    val target = remember(seed) { GameSeeds.memoryCells(seed) }
    var reveal by remember { mutableStateOf(true) }
    val selected = remember { mutableStateListOf<Int>() }
    val started = remember { System.currentTimeMillis() }
    LaunchedEffect(Unit) { delay(1800); reveal = false }
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text(if (reveal) "Memorize the highlighted cells" else "Rebuild the pattern", color = Slate)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(4) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(4) { col ->
                        val index = row * 4 + col
                        val active = (reveal && index in target) || (!reveal && index in selected)
                        Box(Modifier.size(50.dp).background(if (active) Mint else Color(0xFFE6ECE9), RoundedCornerShape(10.dp)).clickable(enabled = !reveal) {
                            if (index in selected) selected.remove(index) else selected.add(index)
                        })
                    }
                }
            }
        }
        Button(onClick = {
            val hits = selected.count { it in target }
            val falseHits = selected.count { it !in target }
            done(GameResult(GameType.MEMORY_GRID, hits, falseHits, target.count { it !in selected }, System.currentTimeMillis() - started))
        }, enabled = !reveal) { Text("Submit pattern") }
    }
}

@Composable private fun SignalWatch(seed: Long, done: (GameResult) -> Unit) {
    var round by remember { mutableIntStateOf(0) }
    var visible by remember { mutableStateOf(false) }
    var correct by remember { mutableIntStateOf(0) }
    var premature by remember { mutableIntStateOf(0) }
    var signalAt by remember { mutableLongStateOf(0L) }
    val times = remember { mutableStateListOf<Long>() }
    val started = remember { System.currentTimeMillis() }
    LaunchedEffect(round) {
        visible = false
        delay(850L + Random(seed + round).nextLong(1200L))
        visible = true
        signalAt = System.currentTimeMillis()
    }
    fun next(hit: Boolean) {
        if (hit) { correct++; times += System.currentTimeMillis() - signalAt } else premature++
        if (round == 5) done(GameResult(GameType.SIGNAL_WATCH, correct, premature, 0, System.currentTimeMillis() - started, times.toList())) else round++
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(28.dp)) {
        Text("Wait for the signal. Early taps count as mistakes.", textAlign = TextAlign.Center, color = Slate)
        Box(Modifier.size(150.dp).background(if (visible) Mint else Color(0xFFE6ECE9), CircleShape).clickable { next(visible) }, contentAlignment = Alignment.Center) {
            Text(if (visible) "NOW" else "WAIT", fontWeight = FontWeight.Black, fontSize = 24.sp, color = Ink)
        }
        Text("Round ${round + 1} of 6", color = Slate)
    }
}

@Composable private fun RuleShift(seed: Long, done: (GameResult) -> Unit) {
    var round by remember { mutableIntStateOf(0) }
    var correct by remember { mutableIntStateOf(0) }
    var wrong by remember { mutableIntStateOf(0) }
    val started = remember { System.currentTimeMillis() }
    val byColor = round % 2 == 0
    val random = Random(seed + round * 31L)
    val green = random.nextBoolean()
    val circle = random.nextBoolean()
    val expectedLeft = if (byColor) green else circle
    fun answer(left: Boolean) {
        if (left == expectedLeft) correct++ else wrong++
        if (round == 7) done(GameResult(GameType.RULE_SHIFT, correct, wrong, 0, System.currentTimeMillis() - started)) else round++
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Text(if (byColor) "RULE: GREEN goes LEFT" else "RULE: CIRCLE goes LEFT", fontWeight = FontWeight.Bold, color = Mint)
        Box(Modifier.size(120.dp).background(if (green) Color(0xFF36A86B) else Color(0xFF4C78E8), if (circle) CircleShape else RoundedCornerShape(12.dp)))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = { answer(true) }, modifier = Modifier.width(120.dp)) { Text("LEFT") }
            OutlinedButton(onClick = { answer(false) }, modifier = Modifier.width(120.dp)) { Text("RIGHT") }
        }
    }
}

@Composable private fun StoryRecall(seed: Long, done: (GameResult) -> Unit) {
    val variants = listOf(
        Triple("Mara left the library at 4:20 carrying a green notebook. She stopped at the bakery before taking bus 12 home.", "Which bus did Mara take?", listOf("8", "12", "20")),
        Triple("Elias watered three balcony plants before breakfast. The smallest pot held rosemary and sat beside the blue chair.", "What grew in the smallest pot?", listOf("Basil", "Rosemary", "Mint")),
        Triple("On Tuesday, Noor repaired a silver bicycle and returned it to Sam near the north gate just before noon.", "Where was the bicycle returned?", listOf("North gate", "Station", "Library"))
    )
    val item = variants[(seed.absoluteValue % variants.size).toInt()]
    var reading by remember { mutableStateOf(true) }
    val started = remember { System.currentTimeMillis() }
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(22.dp)) {
        if (reading) {
            Text(item.first, style = MaterialTheme.typography.titleLarge, lineHeight = 32.sp, textAlign = TextAlign.Center)
            Button(onClick = { reading = false }) { Text("I’ve read it once") }
        } else {
            Text(item.second, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            item.third.forEachIndexed { index, answer ->
                OutlinedButton(onClick = { done(GameResult(GameType.STORY_RECALL, if (index == 1 || (seed.absoluteValue % 3 == 2L && index == 0)) 1 else 0, if (index == 1 || (seed.absoluteValue % 3 == 2L && index == 0)) 0 else 1, 0, System.currentTimeMillis() - started)) }, modifier = Modifier.fillMaxWidth()) { Text(answer) }
            }
        }
    }
}

@Composable
fun ResultScreen(state: AppUiState, home: () -> Unit, share: (String) -> Unit) {
    val score = state.latestScore ?: return
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(if (state.recoveryRun) "RECOVERY DAY SAVED" else if (state.practice) "PRACTICE COMPLETE" else "TODAY’S CLARITY SCORE", color = Mint, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(18.dp))
        Text(score.total.toString(), fontSize = 84.sp, fontWeight = FontWeight.Black, color = Ink)
        Text("out of 100", color = Slate)
        Spacer(Modifier.height(28.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            ScorePart("Accuracy", score.accuracy); ScorePart("Consistency", score.consistency); ScorePart("Control", score.impulseControl)
        }
        Spacer(Modifier.height(32.dp))
        Card(colors = CardDefaults.cardColors(containerColor = SoftMint)) { Text("You’re done. Leave your phone better than you found it.", Modifier.padding(18.dp), textAlign = TextAlign.Center, color = Ink) }
        Spacer(Modifier.height(22.dp))
        if (!state.recoveryRun) {
            Button(onClick = { share("My Focus Reset Clarity Score is ${score.total}. Can your focus survive five minutes? https://focusreset.example/challenge/daily") }, modifier = Modifier.fillMaxWidth()) { Text("Challenge a friend") }
        }
        TextButton(onClick = home) { Text("Return home") }
    }
}

@Composable private fun ScorePart(label: String, value: Int) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(value.toString(), fontWeight = FontWeight.Bold, fontSize = 22.sp); Text(label, color = Slate, fontSize = 12.sp) } }
private fun GameType.displayName() = name.lowercase().split('_').joinToString(" ") { it.replaceFirstChar(Char::uppercase) }
private fun GameType.instructions() = when(this) { GameType.COLOR_CLASH -> "Resist the word. Respond to the ink."; GameType.MEMORY_GRID -> "Hold a spatial pattern through a short delay."; GameType.SIGNAL_WATCH -> "Stay ready without acting early."; GameType.RULE_SHIFT -> "Switch rules without carrying the old one forward."; GameType.STORY_RECALL -> "Read once, then retrieve a detail." }
