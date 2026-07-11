package com.focusreset.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
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

@Composable
fun FocusResetApp(state: AppUiState, model: AppViewModel, openUsageAccess: () -> Unit, share: (String) -> Unit) {
    Scaffold(
        bottomBar = { if (state.screen !in listOf(Screen.RUN, Screen.RESULT)) BottomNav(state.screen, model::navigate) }
    ) { padding ->
        Box(Modifier.fillMaxSize().background(Paper).padding(padding)) {
            when (state.screen) {
                Screen.HOME -> HomeScreen(state, model)
                Screen.PROGRAM -> ProgramScreen(state, model)
                Screen.RUN -> GameRunScreen(state, model::finishGame)
                Screen.RESULT -> ResultScreen(state, { model.navigate(Screen.HOME) }, share)
                Screen.PRACTICE -> PracticeScreen(state, model)
                Screen.SQUADS -> SquadsScreen(share)
                Screen.SETTINGS -> SettingsScreen(state, model, openUsageAccess)
            }
        }
    }
}

@Composable private fun BottomNav(screen: Screen, navigate: (Screen) -> Unit) {
    NavigationBar(containerColor = Color.White) {
        listOf(
            Triple(Screen.HOME, Icons.Outlined.Home, "Today"),
            Triple(Screen.PRACTICE, Icons.Outlined.Psychology, "Practice"),
            Triple(Screen.SQUADS, Icons.Outlined.Groups, "Squads"),
            Triple(Screen.SETTINGS, Icons.Outlined.Settings, "Settings")
        ).forEach { (target, icon, label) ->
            NavigationBarItem(selected = screen == target, onClick = { navigate(target) }, icon = { Icon(icon, null) }, label = { Text(label) })
        }
    }
}

@Composable private fun PageHeader(kicker: String, title: String, detail: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(kicker.uppercase(), color = Mint, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, fontSize = 12.sp)
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        detail?.let { Text(it, color = Slate, style = MaterialTheme.typography.bodyLarge) }
    }
}

@Composable private fun HomeScreen(state: AppUiState, model: AppViewModel) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        PageHeader("Today’s reset", "Make five minutes count.", "One finite run. No feed. No medical promises.")
        Card(colors = CardDefaults.cardColors(containerColor = Ink), shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(if (state.dailyCompleted) "Daily Focus Run complete" else "Daily Focus Run", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(if (state.dailyCompleted) "Your score is saved. Practice remains available." else "Three seeded mind games · about five minutes", color = SoftMint)
                Button(onClick = { model.startRun(false) }, enabled = !state.dailyCompleted, colors = ButtonDefaults.buttonColors(containerColor = Mint, contentColor = Ink)) {
                    Text(if (state.dailyCompleted) "Completed" else "Start today’s run")
                }
            }
        }
        if (state.usageAccess) MetricCard("Selected-app time", "${state.selectedUsageMinutes} min", "Stored on this device")
        else Card(colors = CardDefaults.cardColors(containerColor = SoftMint)) { Text("Usage tracking is optional. Enable it in Settings or continue with honest check-ins.", Modifier.padding(18.dp), color = Ink) }
        Text("Choose a program", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        ProgramCatalog.programs.forEach { program -> ProgramCard(program) { model.selectProgram(program) } }
    }
}

@Composable private fun ProgramCard(program: ChallengeProgram, onClick: () -> Unit) {
    Card(onClick = onClick, colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.fillMaxWidth().padding(18.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(program.title, fontWeight = FontWeight.Bold)
                Text(program.summary, color = Slate)
                Text("${program.length.days} days · ${program.dailyBudgetMinutes} min target", color = Mint, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            if (program.entitlement == Entitlement.PREMIUM) AssistChip(onClick = {}, label = { Text("Premium") })
        }
    }
}

@Composable private fun ProgramScreen(state: AppUiState, model: AppViewModel) {
    val program = state.selectedProgram
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        TextButton(onClick = { model.navigate(Screen.HOME) }) { Text("← Back") }
        PageHeader("${program.length.days}-day program", program.title, program.summary)
        MetricCard("Daily target", "${program.dailyBudgetMinutes} min", "Selected social apps")
        Text("Program map", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        ProgramCatalog.days(program).take(7).forEach { day ->
            ListItem(headlineContent = { Text(day.title, fontWeight = FontWeight.SemiBold) }, supportingContent = { Text(day.instruction) }, leadingContent = { Icon(Icons.Outlined.RadioButtonUnchecked, null, tint = Mint) }, colors = ListItemDefaults.colors(containerColor = Color.Transparent))
        }
        if (program.length.days > 7) Text("+ ${program.length.days - 7} more structured days", color = Slate)
        Button(onClick = { model.startRun(false) }, enabled = program.entitlement == Entitlement.FREE && !state.dailyCompleted, modifier = Modifier.fillMaxWidth()) {
            Text(if (program.entitlement == Entitlement.PREMIUM) "Unlock premium program" else "Begin with today’s Focus Run")
        }
    }
}

@Composable private fun PracticeScreen(state: AppUiState, model: AppViewModel) {
    val remaining = ((15 * 60_000L - state.practiceUsedMs).coerceAtLeast(0) / 60_000L)
    Column(Modifier.fillMaxSize().padding(22.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        PageHeader("Practice lab", "Train deliberately.", "Practice is capped at 15 minutes daily so the cure never becomes another feed.")
        MetricCard("Time remaining", "$remaining min", "Resets tomorrow")
        GameType.entries.forEach { type ->
            Card(onClick = { model.startRun(true) }, colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Extension, null, tint = Mint); Spacer(Modifier.width(14.dp))
                    Column { Text(type.label(), fontWeight = FontWeight.Bold); Text(type.skill(), color = Slate) }
                }
            }
        }
    }
}

@Composable private fun SquadsScreen(share: (String) -> Unit) {
    Column(Modifier.fillMaxSize().padding(22.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        PageHeader("Private competition", "Reset together.", "Only scores, completion, and voluntary milestones are shared. Screen time stays private.")
        Card(colors = CardDefaults.cardColors(containerColor = Ink)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Create your first squad", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("Google sign-in is requested only when you create or join a squad.", color = SoftMint)
                Button(onClick = { share("Join my Focus Reset squad: https://focusreset.example/challenge/demo7") }, colors = ButtonDefaults.buttonColors(containerColor = Mint, contentColor = Ink)) { Text("Preview invitation") }
            }
        }
        Text("Sample leaderboard", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        listOf("Sara" to 88, "Adam" to 82, "You" to 79, "Lina" to 74).forEachIndexed { index, item ->
            ListItem(headlineContent = { Text("${index + 1}. ${item.first}") }, trailingContent = { Text(item.second.toString(), fontWeight = FontWeight.Bold, color = Mint) }, colors = ListItemDefaults.colors(containerColor = Color.Transparent))
        }
    }
}

@Composable private fun SettingsScreen(state: AppUiState, model: AppViewModel, openUsageAccess: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(22.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        PageHeader("Control", "Settings", "Private by default. No Accessibility Service, VPN, or hard blocking.")
        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
            ListItem(headlineContent = { Text("Optional usage access") }, supportingContent = { Text(if (state.usageAccess) "Enabled · detailed records stay local" else "Disabled · manual check-ins remain available") }, trailingContent = { Button(onClick = openUsageAccess) { Text(if (state.usageAccess) "Manage" else "Enable") } })
        }
        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
            ListItem(headlineContent = { Text("Reduced motion") }, supportingContent = { Text("Restrains nonessential transitions and effects") }, trailingContent = { Switch(checked = state.reducedMotion, onCheckedChange = { model.toggleReducedMotion() }) })
        }
        Text("Focus Reset is a game and routine tool. It does not diagnose, treat, heal, or measure intelligence.", color = Slate, fontSize = 13.sp)
    }
}

@Composable private fun MetricCard(label: String, value: String, detail: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.fillMaxWidth().padding(18.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column { Text(label, color = Slate); Text(detail, color = Slate, fontSize = 12.sp) }
            Text(value, fontWeight = FontWeight.Bold, fontSize = 26.sp, color = Ink)
        }
    }
}

private fun GameType.label() = name.lowercase().split('_').joinToString(" ") { it.replaceFirstChar(Char::uppercase) }
private fun GameType.skill() = when(this) { GameType.COLOR_CLASH -> "Response inhibition"; GameType.MEMORY_GRID -> "Spatial working memory"; GameType.SIGNAL_WATCH -> "Sustained attention"; GameType.RULE_SHIFT -> "Flexible switching"; GameType.STORY_RECALL -> "Delayed comprehension" }
