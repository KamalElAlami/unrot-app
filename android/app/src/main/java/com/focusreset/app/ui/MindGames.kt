/*
 * Game rules adapted from Braincup by Simon Schubert.
 * https://github.com/SimonSchubert/Braincup — Apache License 2.0.
 * Modified for Focus Reset deterministic sessions and scoring.
 */
package com.focusreset.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusreset.app.domain.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import kotlinx.coroutines.delay

@Composable
fun GameRunScreen(state: AppUiState, onComplete: (GameResult) -> Unit, onExit: () -> Unit) {
    val game = state.seed.games[state.gameIndex]
    var sessionPhase by remember(state.gameIndex, state.seed.seed) { mutableStateOf(FiniteSessionPhase.INTRO) }
    var roundNonce by remember(state.gameIndex, state.seed.seed) { mutableIntStateOf(0) }
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        sessionPhase = FiniteSessionPolicy.onAppPaused(sessionPhase)
    }
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(if (state.recoveryRun) "RESET ROUND" else if (state.practice) "FINITE PRACTICE" else "DAILY GAME MIX", color = Mint, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp, fontSize = 12.sp)
            Text("${state.gameIndex + 1} / ${state.seed.games.size}", color = Slate)
        }
        LinearProgressIndicator(progress = { (state.gameIndex + .15f) / state.seed.games.size }, modifier = Modifier.fillMaxWidth(), color = Mint, trackColor = RaisedNavy)
        Text(game.title(), fontSize = 30.sp, fontWeight = FontWeight.Black)
        Text(game.instructions(), color = Slate)
        Card(Modifier.fillMaxWidth().weight(1f), colors = CardDefaults.cardColors(containerColor = CardNavy), shape = RoundedCornerShape(26.dp)) {
            Box(Modifier.fillMaxSize().padding(18.dp), contentAlignment = Alignment.Center) {
                if (sessionPhase == FiniteSessionPhase.INTERRUPTED) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        Icon(Icons.Outlined.PauseCircle, null, tint = Amber, modifier = Modifier.size(66.dp))
                        Text("Round interrupted", fontSize = 24.sp, fontWeight = FontWeight.Black)
                        Text("Timing stopped when Focus Reset left the foreground. Restart this finite round for a fair result.", color = Slate, textAlign = TextAlign.Center)
                        Button(onClick = { roundNonce++; sessionPhase = FiniteSessionPhase.PLAYING }, modifier = Modifier.fillMaxWidth()) { Text("Restart round") }
                        TextButton(onClick = onExit, modifier = Modifier.fillMaxWidth()) { Text("Leave game") }
                    }
                } else if (sessionPhase == FiniteSessionPhase.INTRO) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        Surface(color = SoftMint, shape = CircleShape, modifier = Modifier.size(72.dp)) {
                            Box(contentAlignment = Alignment.Center) { Icon(game.icon(), null, tint = Mint, modifier = Modifier.size(36.dp)) }
                        }
                        Text("How to play", color = Mint, fontWeight = FontWeight.Bold)
                        Text(game.instructions(), fontSize = 21.sp, lineHeight = 29.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                        Text(game.skillTarget(), color = Slate, textAlign = TextAlign.Center)
                        AssistChip(onClick = {}, label = { Text("One finite round · no replay loop") })
                        Button(onClick = { sessionPhase = FiniteSessionPhase.PLAYING }, modifier = Modifier.fillMaxWidth().height(54.dp)) { Text("Start", fontWeight = FontWeight.Bold) }
                    }
                } else {
                    key(state.gameIndex, state.seed.seed, roundNonce) {
                        when (game) {
                            GameType.GHOST_GRID -> GhostGrid(state.seed.seed, onComplete)
                            GameType.FLASH_CROWD -> FlashCrowd(state.seed.seed, onComplete)
                            GameType.PATH_FINDER -> PathFinder(state.seed.seed, onComplete)
                            GameType.SCHULTE_TABLE -> SchulteTable(state.seed.seed, onComplete)
                            GameType.DIGIT_MEMORY -> DigitMemory(state.seed.seed, onComplete)
                        }
                    }
                }
            }
        }
        Text("Adapted from Braincup · game performance only", color = Slate, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}

@Composable private fun GhostGrid(seed: Long, done: (GameResult) -> Unit) {
    val round = remember(seed) { BraincupGameEngines.ghostGrid(seed, round = 2) }
    var showingIndex by remember { mutableIntStateOf(-1) }
    var accepting by remember { mutableStateOf(false) }
    var tapIndex by remember { mutableIntStateOf(0) }
    val responseTimes = remember { mutableStateListOf<Long>() }
    var responseStarted by remember { mutableLongStateOf(0L) }
    val started = remember { System.currentTimeMillis() }

    LaunchedEffect(round) {
        delay(450)
        round.sequence.forEachIndexed { index, _ ->
            showingIndex = index
            delay(if (index == 0) round.flashDurationMs + 300 else round.flashDurationMs)
            showingIndex = -1
            delay(180)
        }
        accepting = true
        responseStarted = System.currentTimeMillis()
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text(if (accepting) "Repeat the sequence" else "Watch the tiles", fontWeight = FontWeight.Bold, color = if (accepting) Mint else Slate)
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            repeat(round.gridSize) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    repeat(round.gridSize) { col ->
                        val cell = row * round.gridSize + col
                        val active = showingIndex >= 0 && round.sequence[showingIndex] == cell
                        val completed = accepting && cell in round.sequence.take(tapIndex)
                        Box(
                            Modifier.size(55.dp).background(when { active -> Mint; completed -> SoftMint; else -> RaisedNavy }, RoundedCornerShape(13.dp)).clickable(enabled = accepting) {
                                responseTimes += System.currentTimeMillis() - responseStarted
                                responseStarted = System.currentTimeMillis()
                                if (cell != round.sequence[tapIndex]) {
                                    accepting = false
                                    done(GameResult(GameType.GHOST_GRID, tapIndex, 1, round.sequence.size - tapIndex - 1, System.currentTimeMillis() - started, responseTimes.toList()))
                                } else if (tapIndex == round.sequence.lastIndex) {
                                    accepting = false
                                    done(GameResult(GameType.GHOST_GRID, round.sequence.size, 0, 0, System.currentTimeMillis() - started, responseTimes.toList()))
                                } else tapIndex++
                            }.semantics {
                                contentDescription = "Grid tile ${cell + 1}"
                                role = Role.Button
                                stateDescription = when { active -> "Lit"; completed -> "Repeated"; accepting -> "Available"; else -> "Waiting" }
                            }
                        )
                    }
                }
            }
        }
        Text(if (accepting) "${tapIndex + 1} / ${round.sequence.size}" else "Sequence length ${round.sequence.size}", color = Slate)
    }
}

@Composable private fun FlashCrowd(seed: Long, done: (GameResult) -> Unit) {
    var roundIndex by remember { mutableIntStateOf(0) }
    var visible by remember { mutableStateOf(true) }
    var correct by remember { mutableIntStateOf(0) }
    var wrong by remember { mutableIntStateOf(0) }
    val times = remember { mutableStateListOf<Long>() }
    var answerStarted by remember { mutableLongStateOf(0L) }
    val started = remember { System.currentTimeMillis() }
    val round = remember(seed, roundIndex) { BraincupGameEngines.flashCrowd(seed, roundIndex + 1) }

    LaunchedEffect(roundIndex) {
        visible = true
        delay(850)
        visible = false
        answerStarted = System.currentTimeMillis()
    }

    fun answer(left: Boolean) {
        if (visible) return
        val isCorrect = left == (round.leftCount > round.rightCount)
        if (isCorrect) correct++ else wrong++
        times += System.currentTimeMillis() - answerStarted
        if (roundIndex == 5) done(GameResult(GameType.FLASH_CROWD, correct, wrong, 0, System.currentTimeMillis() - started, times.toList())) else roundIndex++
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text(if (visible) "See the crowd" else "Which side had more?", color = if (visible) Slate else Mint, fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth().height(230.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DotField(if (visible) round.leftDots else emptyList(), Modifier.weight(1f).fillMaxHeight())
            DotField(if (visible) round.rightDots else emptyList(), Modifier.weight(1f).fillMaxHeight())
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { answer(true) }, enabled = !visible, modifier = Modifier.weight(1f)) { Text("LEFT") }
            Button(onClick = { answer(false) }, enabled = !visible, modifier = Modifier.weight(1f)) { Text("RIGHT") }
        }
        Text("Round ${roundIndex + 1} of 6", color = Slate)
    }
}

@Composable private fun DotField(dots: List<BraincupGameEngines.Dot>, modifier: Modifier = Modifier) {
    Canvas(modifier.semantics { contentDescription = if (dots.isEmpty()) "Dot crowd hidden" else "Brief dot crowd" }.background(RaisedNavy, RoundedCornerShape(18.dp))) {
        dots.forEachIndexed { index, dot ->
            drawCircle(
                color = if (index % 4 == 0) Amber else Mint,
                radius = dot.radius * size.minDimension,
                center = Offset(dot.x * size.width, dot.y * size.height)
            )
        }
    }
}

@Composable private fun PathFinder(seed: Long, done: (GameResult) -> Unit) {
    val round = remember(seed) { BraincupGameEngines.pathFinder(seed, round = 2) }
    val arrows = listOf("↑", "→", "↓", "←")
    val started = remember { System.currentTimeMillis() }
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(17.dp)) {
        Text("Begin at S and follow every arrow mentally", color = Slate, textAlign = TextAlign.Center)
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            round.directions.forEach { direction ->
                Surface(color = SoftMint, shape = RoundedCornerShape(12.dp), modifier = Modifier.size(43.dp)) {
                    Box(contentAlignment = Alignment.Center) { Text(arrows[direction], color = Mint, fontSize = 24.sp, fontWeight = FontWeight.Black) }
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            repeat(round.gridSize) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    repeat(round.gridSize) { col ->
                        val cell = row * round.gridSize + col
                        val start = cell == round.startIndex
                        Box(
                            Modifier.size(58.dp).background(if (start) Amber else RaisedNavy, RoundedCornerShape(13.dp)).clickable {
                                val correct = cell == round.destination
                                done(GameResult(GameType.PATH_FINDER, if (correct) 1 else 0, if (correct) 0 else 1, 0, System.currentTimeMillis() - started))
                            }.semantics {
                                contentDescription = if (start) "Starting square, row ${row + 1}, column ${col + 1}" else "Square, row ${row + 1}, column ${col + 1}"
                                role = Role.Button
                            },
                            contentAlignment = Alignment.Center
                        ) { if (start) Text("S", color = Paper, fontWeight = FontWeight.Black) }
                    }
                }
            }
        }
        Text("Tap the destination", color = Mint, fontWeight = FontWeight.Bold)
    }
}

@Composable private fun SchulteTable(seed: Long, done: (GameResult) -> Unit) {
    val numbers = remember(seed) { BraincupGameEngines.schulteTable(seed) }
    var expected by remember { mutableIntStateOf(1) }
    var mistakes by remember { mutableIntStateOf(0) }
    var wrongCell by remember { mutableIntStateOf(-1) }
    val responseTimes = remember { mutableStateListOf<Long>() }
    var responseStarted by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val started = remember { System.currentTimeMillis() }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(17.dp)) {
        Text("Find $expected", color = Mint, fontSize = 23.sp, fontWeight = FontWeight.Black)
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            repeat(4) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    repeat(4) { col ->
                        val index = row * 4 + col
                        val number = numbers[index]
                        val cleared = number < expected
                        Box(
                            Modifier.size(62.dp).background(when { index == wrongCell -> Coral; cleared -> SoftMint; else -> RaisedNavy }, RoundedCornerShape(13.dp)).clickable(enabled = !cleared) {
                                if (number == expected) {
                                    responseTimes += System.currentTimeMillis() - responseStarted
                                    responseStarted = System.currentTimeMillis()
                                    wrongCell = -1
                                    if (expected == 16) done(GameResult(GameType.SCHULTE_TABLE, 16, mistakes, 0, System.currentTimeMillis() - started, responseTimes.toList())) else expected++
                                } else {
                                    mistakes++
                                    wrongCell = index
                                }
                            }.semantics {
                                contentDescription = "Number $number"
                                role = Role.Button
                                stateDescription = when { cleared -> "Completed"; index == wrongCell -> "Incorrect selection"; else -> "Find number $expected" }
                            },
                            contentAlignment = Alignment.Center
                        ) { Text(number.toString(), color = if (cleared) Slate else Ink, fontSize = 21.sp, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
        Text("Tap 1 through 16 in order", color = Slate)
    }
}

private enum class DigitPhase { SHOWING, SOLVING, RECALL }

@Composable private fun DigitMemory(seed: Long, done: (GameResult) -> Unit) {
    val round = remember(seed) { BraincupGameEngines.digitMemory(seed, round = 2) }
    var phase by remember { mutableStateOf(DigitPhase.SHOWING) }
    var input by remember { mutableStateOf("") }
    var mathMistakes by remember { mutableIntStateOf(0) }
    val started = remember { System.currentTimeMillis() }
    val responseTimes = remember { mutableStateListOf<Long>() }
    var phaseStarted by remember { mutableLongStateOf(started) }

    LaunchedEffect(round) {
        delay((800L * round.sequence.length).coerceIn(2_500L, 6_000L))
        phase = DigitPhase.SOLVING
        input = ""
        phaseStarted = System.currentTimeMillis()
    }

    fun submit() {
        when (phase) {
            DigitPhase.SHOWING -> Unit
            DigitPhase.SOLVING -> {
                if (input == round.answer) {
                    responseTimes += System.currentTimeMillis() - phaseStarted
                    phase = DigitPhase.RECALL
                    input = ""
                    phaseStarted = System.currentTimeMillis()
                } else {
                    mathMistakes++
                    input = ""
                }
            }
            DigitPhase.RECALL -> {
                responseTimes += System.currentTimeMillis() - phaseStarted
                val correct = input == round.sequence
                done(GameResult(GameType.DIGIT_MEMORY, if (correct) 1 else 0, (if (correct) 0 else 1) + mathMistakes, 0, System.currentTimeMillis() - started, responseTimes.toList()))
            }
        }
    }

    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(22.dp)) {
        when (phase) {
            DigitPhase.SHOWING -> {
                Icon(Icons.Outlined.Visibility, null, tint = Mint, modifier = Modifier.size(38.dp))
                Text("Memorize", color = Slate, fontWeight = FontWeight.Bold)
                Text(round.sequence, fontSize = 50.sp, letterSpacing = 6.sp, fontWeight = FontWeight.Black)
            }
            DigitPhase.SOLVING -> {
                Text("Hold the digits. Solve this first.", color = Slate, textAlign = TextAlign.Center)
                Text(round.problem, fontSize = 43.sp, fontWeight = FontWeight.Black)
                NumericEntry(input, { input = it }, ::submit, "Answer")
                if (mathMistakes > 0) Text("Try the calculation again", color = Coral)
            }
            DigitPhase.RECALL -> {
                Text("Now recall the digits", color = Slate)
                NumericEntry(input, { input = it.take(round.sequence.length) }, ::submit, "Digit sequence")
                Text("${input.length} / ${round.sequence.length}", color = Mint)
            }
        }
    }
}

@Composable private fun NumericEntry(value: String, onChange: (String) -> Unit, submit: () -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = { onChange(it.filter(Char::isDigit)) },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { submit() }),
        modifier = Modifier.fillMaxWidth()
    )
    Button(onClick = submit, enabled = value.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Continue") }
}

@Composable
fun ResultScreen(state: AppUiState, home: () -> Unit, share: (String) -> Unit) {
    val score = state.latestScore ?: return
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(score.total) { haptic.performHapticFeedback(HapticFeedbackType.LongPress) }
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(if (state.recoveryRun) "RECOVERY SAVED" else if (state.practice) "FINITE GAME COMPLETE" else "TODAY’S GAME SCORE", color = Mint, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(18.dp))
        Text(score.total.toString(), fontSize = 84.sp, fontWeight = FontWeight.Black)
        Text("game performance · out of 100", color = Slate)
        Spacer(Modifier.height(28.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            ScorePart("Accuracy", score.accuracy)
            ScorePart("Consistency", score.consistency)
            ScorePart("Control", score.impulseControl)
        }
        Spacer(Modifier.height(30.dp))
        Card(colors = CardDefaults.cardColors(containerColor = CardNavy)) {
            Text("Session finished. Close the app instead of starting another loop.", Modifier.padding(18.dp), textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(20.dp))
        if (!state.recoveryRun) Button(onClick = { share("I finished a Focus Reset game instead of opening Reels. https://focusreset.example/challenge/daily") }, modifier = Modifier.fillMaxWidth()) { Text("Challenge a friend") }
        TextButton(onClick = home) { Text("Return to challenge") }
    }
}

@Composable private fun ScorePart(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(value.toString(), fontWeight = FontWeight.Bold, fontSize = 22.sp); Text(label, color = Slate, fontSize = 12.sp) }
}

private fun GameType.title() = when (this) {
    GameType.GHOST_GRID -> "Ghost Grid"
    GameType.FLASH_CROWD -> "Flash Crowd"
    GameType.PATH_FINDER -> "Path Finder"
    GameType.SCHULTE_TABLE -> "Schulte Table"
    GameType.DIGIT_MEMORY -> "Digit Memory"
}

private fun GameType.instructions() = when (this) {
    GameType.GHOST_GRID -> "Watch unique tiles light up, then repeat them in the same order."
    GameType.FLASH_CROWD -> "See two crowds briefly and choose the side with more dots."
    GameType.PATH_FINDER -> "Follow a bounded arrow path from the marked starting square."
    GameType.SCHULTE_TABLE -> "Find shuffled numbers from 1 to 16 as steadily as possible."
    GameType.DIGIT_MEMORY -> "Remember digits through a short calculation, then recall them."
}

private fun GameType.skillTarget() = when (this) {
    GameType.GHOST_GRID -> "Sequence memory · difficulty grows with the number of tiles"
    GameType.FLASH_CROWD -> "Rapid visual estimation · dot counts become more similar"
    GameType.PATH_FINDER -> "Mental tracking · follow five bounded direction changes"
    GameType.SCHULTE_TABLE -> "Visual search · mistakes are recorded, not punished with restarts"
    GameType.DIGIT_MEMORY -> "Working memory · protect five digits through distraction"
}

private fun GameType.icon() = when (this) {
    GameType.GHOST_GRID -> Icons.Outlined.GridView
    GameType.FLASH_CROWD -> Icons.Outlined.Grain
    GameType.PATH_FINDER -> Icons.Outlined.Route
    GameType.SCHULTE_TABLE -> Icons.Outlined.Dialpad
    GameType.DIGIT_MEMORY -> Icons.Outlined.Password
}
