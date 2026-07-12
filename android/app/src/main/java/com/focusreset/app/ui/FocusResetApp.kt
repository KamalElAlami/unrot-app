package com.focusreset.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusreset.app.domain.*

@Composable
fun FocusResetApp(state: AppUiState, model: AppViewModel, openUsageAccess: () -> Unit, requestReminderPermission: () -> Unit, share: (String) -> Unit) {
    if (!state.initialized) {
        Box(Modifier.fillMaxSize().background(Paper), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Mint)
        }
        return
    }
    BackHandler(enabled = AppNavigation.backDestination(state) != null) {
        model.goBack()
    }
    Scaffold(
        containerColor = Paper,
        bottomBar = {
            if (state.screen !in listOf(Screen.ONBOARDING, Screen.RUN, Screen.RESULT)) {
                BottomNav(state.screen, model::navigate)
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().background(Paper).padding(padding)) {
            when (state.screen) {
                Screen.ONBOARDING -> OnboardingScreen(model::completeOnboarding, openUsageAccess)
                Screen.HOME -> HomeScreen(state, model, share)
                Screen.HISTORY -> HistoryScreen(state) { model.navigate(Screen.HOME) }
                Screen.PROGRAM -> ProgramScreen(state, model)
                Screen.RUN -> GameRunScreen(state, model::finishGame, model::goBack)
                Screen.RESULT -> ResultScreen(state, { model.navigate(Screen.HOME) }, share)
                Screen.PRACTICE -> PracticeScreen(state, model)
                Screen.SQUADS -> SquadsScreen(share)
                Screen.SETTINGS -> SettingsScreen(state, model, openUsageAccess, requestReminderPermission)
                Screen.PRIVACY -> PrivacyScreen(model)
            }
        }
    }
}

@Composable private fun OnboardingScreen(continueSolo: () -> Unit, openUsageAccess: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(26.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Spacer(Modifier.height(24.dp))
            Surface(color = SoftMint, shape = CircleShape, modifier = Modifier.size(64.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Block, null, tint = Mint, modifier = Modifier.size(32.dp)) }
            }
            Text("Stop feeding the feed.", fontSize = 42.sp, lineHeight = 46.sp, fontWeight = FontWeight.Black)
            Text("Start with 7 days without Reels, Shorts, or TikTok. Check in honestly once each day.", color = Slate, fontSize = 19.sp, lineHeight = 28.sp)
            InfoPanel(
                title = "The challenge comes first",
                detail = "Mind games are optional replacements for the moment you feel like opening a short-video feed."
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Lock, null, tint = Mint)
                Spacer(Modifier.width(12.dp))
                Text("No account needed. Your daily history stays on this device.", color = Slate)
            }
            TextButton(onClick = openUsageAccess) { Text("Optional: review Android usage access") }
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = continueSolo, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(18.dp)) {
                Text("Start my 7-day reset", fontWeight = FontWeight.Bold)
            }
            Text("For ages 13+. This is a habit challenge, not medical treatment.", color = Slate, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable private fun BottomNav(screen: Screen, navigate: (Screen) -> Unit) {
    NavigationBar(containerColor = CardNavy, tonalElevation = 0.dp) {
        listOf(
            Triple(Screen.HOME, Icons.Outlined.Today, "Today"),
            Triple(Screen.PRACTICE, Icons.Outlined.Extension, "Mind games"),
            Triple(Screen.SQUADS, Icons.Outlined.Groups, "Squads"),
            Triple(Screen.SETTINGS, Icons.Outlined.Settings, "Settings")
        ).forEach { (target, icon, label) ->
            NavigationBarItem(
                selected = screen == target,
                onClick = { navigate(target) },
                icon = { Icon(icon, null) },
                label = { Text(label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Paper,
                    selectedTextColor = Ink,
                    indicatorColor = Mint,
                    unselectedIconColor = Slate,
                    unselectedTextColor = Slate
                )
            )
        }
    }
}

@Composable private fun HomeScreen(state: AppUiState, model: AppViewModel, share: (String) -> Unit) {
    val program = state.activeProgram
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("TODAY", color = Mint, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.8.sp)
                Text("Focus Reset", fontSize = 30.sp, fontWeight = FontWeight.Black)
            }
            Surface(color = CardNavy, shape = RoundedCornerShape(18.dp)) {
                Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocalFireDepartment, null, tint = Coral)
                    Spacer(Modifier.width(6.dp))
                    Text(state.challengeStreak.toString(), fontWeight = FontWeight.Bold)
                }
            }
        }

        if (program == null) {
            InfoPanel("Choose your reset", "Start with seven days without short-video feeds.")
            ProgramCatalog.programs.forEach { item -> ProgramCard(item) { model.selectProgram(item) } }
            return@Column
        }

        WeekStrip(program.length.days, state.challengeDay, state.challengeOutcomes) { model.navigate(Screen.HISTORY) }

        Card(colors = CardDefaults.cardColors(containerColor = CardNavy), shape = RoundedCornerShape(28.dp)) {
            Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("NO REELS", color = Mint, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.6.sp)
                        Text("Day ${state.challengeDay} of ${program.length.days}", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                    Icon(Icons.Outlined.Block, null, tint = Mint, modifier = Modifier.size(34.dp))
                }
                Spacer(Modifier.height(24.dp))
                Box(Modifier.size(220.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { state.completedChallengeDays.toFloat() / program.length.days },
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 18.dp,
                        color = Mint,
                        trackColor = RaisedNavy
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${state.completedChallengeDays}", fontSize = 62.sp, fontWeight = FontWeight.Black)
                        Text("clean days", color = Slate)
                    }
                }
                Spacer(Modifier.height(18.dp))
                Text("Avoid Instagram Reels, YouTube Shorts, and TikTok. Normal messages and posts do not break the challenge.", color = Slate, textAlign = TextAlign.Center, lineHeight = 21.sp)
            }
        }

        if (state.challengeFinished) ChallengeComplete(state, model, share) else DailyCheckIn(state, model)

        Card(colors = CardDefaults.cardColors(containerColor = CardNavy), shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = SoftMint, shape = CircleShape, modifier = Modifier.size(42.dp)) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Psychology, null, tint = Mint) }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Need a replacement?", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                        Text("Play one short mind game, then leave your phone.", color = Slate)
                    }
                }
                Button(onClick = { model.navigate(Screen.PRACTICE) }, modifier = Modifier.fillMaxWidth()) { Text("Open mind games") }
            }
        }

        if (state.usageAccess) {
            UsageContextCard(state) { model.navigate(Screen.SETTINGS) }
        } else {
            InfoPanel("Self-reported by design", "Android cannot tell Reels apart from ordinary Instagram use, so your daily check-in is the source of truth.")
        }

        Text("Other challenge lengths", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        ProgramCatalog.programs.filter { it.id != program.id }.forEach { item -> ProgramCard(item) { model.selectProgram(item) } }
    }
}

@Composable private fun UsageContextCard(state: AppUiState, openSettings: () -> Unit) {
    Card(onClick = openSettings, colors = CardDefaults.cardColors(containerColor = CardNavy), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Selected-app context", fontWeight = FontWeight.Bold)
                    Text("${state.selectedUsageMinutes} minutes reported today", color = Slate)
                }
                Icon(Icons.Outlined.ChevronRight, null, tint = Mint)
            }
            if (state.trackedApps.isEmpty()) {
                Text("No apps selected. Choose apps in Settings.", color = Amber, fontSize = 12.sp)
            } else {
                state.trackedApps.sortedBy(TrackableAppCatalog::label).forEach { packageName ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(TrackableAppCatalog.label(packageName), color = Slate, fontSize = 13.sp)
                        Text("${state.usageMinutesByApp[packageName] ?: 0} min", fontSize = 13.sp)
                    }
                }
                if (state.selectedUsageMinutes == 0) Text("No activity reported yet. Some Android variants may delay Usage Stats.", color = Slate, fontSize = 11.sp)
            }
            Text("Context only · this cannot identify Reels", color = Mint, fontSize = 11.sp)
        }
    }
}

@Composable private fun ChallengeComplete(state: AppUiState, model: AppViewModel, share: (String) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = SoftMint), shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Outlined.EmojiEvents, null, tint = Amber, modifier = Modifier.size(48.dp))
            Text("Challenge complete", fontSize = 24.sp, fontWeight = FontWeight.Black)
            Text("${state.completedChallengeDays} no-reels or recovery days recorded.", color = Slate, textAlign = TextAlign.Center)
            OutlinedButton(onClick = { share("I completed ${state.activeProgram?.title ?: "a Focus Reset challenge"} with ${state.completedChallengeDays} focused days. https://focusreset.example/challenge/daily") }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.Share, null)
                Spacer(Modifier.width(8.dp))
                Text("Share completion")
            }
            Button(onClick = model::restartChallenge, modifier = Modifier.fillMaxWidth()) { Text("Restart this challenge") }
        }
    }
}

@Composable private fun WeekStrip(length: Int, currentDay: Int, outcomes: Map<Int, DayOutcome>, onClick: () -> Unit) {
    Card(onClick = onClick, colors = CardDefaults.cardColors(containerColor = CardNavy), shape = RoundedCornerShape(22.dp)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            (1..minOf(length, 7)).forEach { day ->
                val outcome = outcomes[day] ?: DayOutcome.PENDING
                val background = when (outcome) {
                    DayOutcome.PERFECT -> Mint
                    DayOutcome.RECOVERY -> Amber
                    DayOutcome.MISSED -> Coral
                    DayOutcome.PENDING -> if (day == currentDay) RaisedNavy else Color.Transparent
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text("D$day", color = if (day == currentDay) Ink else Slate, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Surface(color = background, shape = CircleShape, modifier = Modifier.size(35.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            when (outcome) {
                                DayOutcome.PERFECT -> Icon(Icons.Outlined.Check, null, tint = Paper, modifier = Modifier.size(20.dp))
                                DayOutcome.RECOVERY -> Icon(Icons.Outlined.Refresh, null, tint = Paper, modifier = Modifier.size(19.dp))
                                DayOutcome.MISSED -> Icon(Icons.Outlined.Close, null, tint = Paper, modifier = Modifier.size(19.dp))
                                DayOutcome.PENDING -> Text(day.toString(), color = if (day == currentDay) Ink else Slate)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun HistoryScreen(state: AppUiState, back: () -> Unit) {
    val program = state.activeProgram ?: return
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        TextButton(onClick = back) { Text("← Today") }
        Text("CHALLENGE HISTORY", color = Mint, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
        Text(program.title, fontSize = 32.sp, lineHeight = 37.sp, fontWeight = FontWeight.Black)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HistoryMetric("Completed", state.completedChallengeDays.toString(), Modifier.weight(1f))
            HistoryMetric("Streak", state.challengeStreak.toString(), Modifier.weight(1f))
            HistoryMetric("Today", "D${state.challengeDay}", Modifier.weight(1f))
        }
        Text("Daily record", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        (1..program.length.days).forEach { day ->
            val outcome = state.challengeOutcomes[day] ?: DayOutcome.PENDING
            val color = when (outcome) { DayOutcome.PERFECT -> Mint; DayOutcome.RECOVERY -> Amber; DayOutcome.MISSED -> Coral; DayOutcome.PENDING -> Slate }
            val title = when (outcome) { DayOutcome.PERFECT -> "No reels"; DayOutcome.RECOVERY -> "Recovery"; DayOutcome.MISSED -> "Missed"; DayOutcome.PENDING -> if (day == state.challengeDay) "Check in today" else "Upcoming" }
            Card(colors = CardDefaults.cardColors(containerColor = if (day == state.challengeDay) RaisedNavy else CardNavy), shape = RoundedCornerShape(18.dp)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = color.copy(alpha = .18f), shape = CircleShape, modifier = Modifier.size(44.dp)) {
                        Box(contentAlignment = Alignment.Center) { Text(day.toString(), color = color, fontWeight = FontWeight.Black) }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Day $day", fontWeight = FontWeight.Bold)
                        Text(title, color = color)
                        state.challengeNotes[day]?.let { Text(it, color = Slate, maxLines = 2, fontSize = 12.sp) }
                    }
                    Icon(when (outcome) { DayOutcome.PERFECT -> Icons.Outlined.CheckCircle; DayOutcome.RECOVERY -> Icons.Outlined.Refresh; DayOutcome.MISSED -> Icons.Outlined.Cancel; DayOutcome.PENDING -> Icons.Outlined.Schedule }, null, tint = color)
                }
            }
        }
        InfoPanel("How records work", "Past days without a check-in are marked missed. A recovery round can preserve momentum without rewriting an honest missed day as perfect.")
        Spacer(Modifier.height(8.dp))
    }
}

@Composable private fun HistoryMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = CardNavy), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.fillMaxWidth().padding(vertical = 15.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Text(label, color = Slate, fontSize = 12.sp)
        }
    }
}

@Composable private fun DailyCheckIn(state: AppUiState, model: AppViewModel) {
    Card(colors = CardDefaults.cardColors(containerColor = CardNavy), shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            when (state.currentDayOutcome) {
                DayOutcome.PENDING -> {
                    Text("Tonight's check-in", color = Mint, fontWeight = FontWeight.Bold)
                    Text("Did you avoid Reels, Shorts, and TikTok today?", fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold)
                    Button(onClick = { model.submitCheckIn(true) }, modifier = Modifier.fillMaxWidth().height(54.dp)) { Text("Yes — no reels today", fontWeight = FontWeight.Bold) }
                    TextButton(onClick = { model.submitCheckIn(false) }, modifier = Modifier.fillMaxWidth()) { Text("No — I watched short videos", color = Slate) }
                }
                DayOutcome.PERFECT -> StatusMessage(Icons.Outlined.Verified, Mint, "Check-in complete", "You recorded a no-reels day. Come back tomorrow.")
                DayOutcome.RECOVERY -> StatusMessage(Icons.Outlined.Refresh, Amber, "Recovery recorded", "You interrupted the loop and kept the challenge moving.")
                DayOutcome.MISSED -> {
                    StatusMessage(Icons.Outlined.FavoriteBorder, Coral, "Honesty counts", "Today is recorded as a missed day. Your challenge is still here.")
                    Button(onClick = model::startRecovery, modifier = Modifier.fillMaxWidth()) { Text("Do a 60-second Reset Round") }
                }
            }
            if (state.currentDayOutcome != DayOutcome.PENDING) {
                JournalCard(state, model)
            }
        }
    }
}

@Composable private fun JournalCard(state: AppUiState, model: AppViewModel) {
    val saved = state.challengeNotes[state.challengeDay].orEmpty()
    var note by androidx.compose.runtime.remember(state.challengeDay, saved) { androidx.compose.runtime.mutableStateOf(saved) }
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        HorizontalDivider(color = RaisedNavy)
        Text("Private reflection", fontWeight = FontWeight.Bold)
        Text("What triggered the urge—or what helped today?", color = Slate, fontSize = 13.sp)
        OutlinedTextField(
            value = note,
            onValueChange = { note = it.take(240) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4,
            supportingText = { Text("${note.length}/240 · stored only on this device") }
        )
        Button(onClick = { model.saveJournalNote(note) }, enabled = note.trim() != saved, modifier = Modifier.fillMaxWidth()) {
            Text(if (saved.isBlank()) "Save reflection" else "Update reflection")
        }
    }
}

@Composable private fun StatusMessage(icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, title: String, detail: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(color = color.copy(alpha = .16f), shape = CircleShape, modifier = Modifier.size(48.dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = color) }
        }
        Spacer(Modifier.width(14.dp))
        Column { Text(title, fontWeight = FontWeight.Bold, fontSize = 19.sp); Text(detail, color = Slate) }
    }
}

@Composable private fun ProgramCard(program: ChallengeProgram, onClick: () -> Unit) {
    Card(onClick = onClick, colors = CardDefaults.cardColors(containerColor = CardNavy), shape = RoundedCornerShape(20.dp)) {
        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = if (program.entitlement == Entitlement.FREE) SoftMint else RaisedNavy, shape = RoundedCornerShape(16.dp), modifier = Modifier.size(58.dp)) {
                Box(contentAlignment = Alignment.Center) { Text("${program.length.days}D", color = Mint, fontWeight = FontWeight.Black) }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) { Text(program.title, fontWeight = FontWeight.Bold); Text(program.summary, color = Slate) }
            if (program.entitlement == Entitlement.PREMIUM) Icon(Icons.Outlined.Lock, null, tint = Slate) else Icon(Icons.Outlined.ChevronRight, null, tint = Mint)
        }
    }
}

@Composable private fun ProgramScreen(state: AppUiState, model: AppViewModel) {
    val program = state.selectedProgram
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        TextButton(onClick = { model.navigate(Screen.HOME) }) { Text("← Back") }
        Text("${program.length.days}-DAY CHALLENGE", color = Mint, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
        Text(program.title, fontSize = 34.sp, lineHeight = 39.sp, fontWeight = FontWeight.Black)
        Text(program.summary, color = Slate, fontSize = 18.sp)
        InfoPanel("Your only daily requirement", "Avoid short-video feeds and check in honestly once per day. Mind games are optional.")
        ProgramCatalog.days(program).take(7).forEach { day ->
            ListItem(
                headlineContent = { Text(day.title, fontWeight = FontWeight.Bold) },
                supportingContent = { Text(day.instruction) },
                leadingContent = { Icon(Icons.Outlined.RadioButtonUnchecked, null, tint = Mint) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
        }
        Button(onClick = model::activateSelectedProgram, enabled = program.entitlement == Entitlement.FREE && state.activeProgram?.id != program.id, modifier = Modifier.fillMaxWidth().height(54.dp)) {
            Text(if (program.entitlement == Entitlement.PREMIUM) "Premium — coming later" else if (state.activeProgram?.id == program.id) "Challenge active" else "Start challenge")
        }
    }
}

@Composable private fun PracticeScreen(state: AppUiState, model: AppViewModel) {
    val remaining = ((15 * 60_000L - state.practiceUsedMs).coerceAtLeast(0) / 60_000L)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("MIND GAMES", color = Mint, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
        Text("Break the urge.", fontSize = 34.sp, fontWeight = FontWeight.Black)
        Text("Choose one finite game. When it ends, leave your phone.", color = Slate, fontSize = 17.sp)
        InfoPanel("$remaining minutes available", "Practice is capped daily so games never become another endless feed.")
        GameType.entries.forEach { type ->
            Card(onClick = { model.startPractice(type) }, colors = CardDefaults.cardColors(containerColor = CardNavy), shape = RoundedCornerShape(19.dp)) {
                Row(Modifier.fillMaxWidth().padding(17.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = SoftMint, shape = CircleShape, modifier = Modifier.size(44.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Extension, null, tint = Mint) } }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) { Text(type.label(), fontWeight = FontWeight.Bold); Text(type.skill(), color = Slate) }
                    Icon(Icons.Outlined.PlayArrow, null, tint = Mint)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable private fun SquadsScreen(share: (String) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text("SQUADS", color = Mint, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
        Text("Stay off the feed together.", fontSize = 32.sp, lineHeight = 37.sp, fontWeight = FontWeight.Black)
        InfoPanel("Private by default", "Friends see check-in status and game scores, never individual screen time.")
        Button(onClick = { share("Join my Focus Reset challenge: https://focusreset.example/challenge/demo7") }, modifier = Modifier.fillMaxWidth()) { Text("Preview invitation") }
        Text("Squad preview", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        listOf("Sara" to 7, "Adam" to 6, "You" to 5, "Lina" to 4).forEach { item ->
            ListItem(headlineContent = { Text(item.first) }, supportingContent = { Text("${item.second}-day no-reels streak") }, trailingContent = { Icon(Icons.Outlined.LocalFireDepartment, null, tint = Coral) }, colors = ListItemDefaults.colors(containerColor = Color.Transparent))
        }
    }
}

@Composable private fun SettingsScreen(state: AppUiState, model: AppViewModel, openUsageAccess: () -> Unit, requestReminderPermission: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text("SETTINGS", color = Mint, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
        Text("Your reset, your data.", fontSize = 32.sp, fontWeight = FontWeight.Black)
        Card(colors = CardDefaults.cardColors(containerColor = CardNavy)) {
            Column(Modifier.padding(bottom = 12.dp)) {
                ListItem(headlineContent = { Text("Optional usage context") }, supportingContent = { Text(if (state.usageAccess) "Enabled · totals stay local" else "Disabled · check-ins still work") }, trailingContent = { Button(onClick = openUsageAccess) { Text(if (state.usageAccess) "Manage" else "Enable") } })
                Text("Apps included", color = Slate, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                TrackableAppCatalog.apps.forEach { app ->
                    ListItem(
                        headlineContent = { Text(app.displayName) },
                        supportingContent = { if (app.packageName in state.trackedApps && state.usageAccess) Text("${state.usageMinutesByApp[app.packageName] ?: 0} min today", color = Slate) },
                        trailingContent = { Checkbox(checked = app.packageName in state.trackedApps, onCheckedChange = { model.toggleTrackedApp(app.packageName) }) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
                Text("Usage Stats cannot distinguish Reels, Shorts, or TikTok videos from other activity inside an app.", color = Slate, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = CardNavy)) {
            ListItem(headlineContent = { Text("Reduced motion") }, supportingContent = { Text("Restrains nonessential effects") }, trailingContent = { Switch(checked = state.reducedMotion, onCheckedChange = { model.toggleReducedMotion() }) })
        }
        Card(colors = CardDefaults.cardColors(containerColor = CardNavy)) {
            Column(Modifier.padding(bottom = if (state.reminderEnabled) 14.dp else 0.dp)) {
                ListItem(
                    headlineContent = { Text("Daily check-in reminder") },
                    supportingContent = { Text(if (state.reminderEnabled) "Scheduled for %02d:%02d".format(state.reminderHour, state.reminderMinute) else "Off") },
                    trailingContent = {
                        Switch(
                            checked = state.reminderEnabled,
                            onCheckedChange = { enabled -> if (enabled) requestReminderPermission() else model.setReminderEnabled(false) }
                        )
                    }
                )
                if (state.reminderEnabled) {
                    Text("Reminder time", color = Slate, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp))
                    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(19 to 0, 21 to 0, 22 to 30).forEach { time ->
                            val label = "%02d:%02d".format(time.first, time.second)
                            FilterChip(
                                selected = state.reminderHour == time.first && state.reminderMinute == time.second,
                                onClick = { model.setReminderTime(time.first, time.second) },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            }
        }
        Card(onClick = { model.navigate(Screen.PRIVACY) }, colors = CardDefaults.cardColors(containerColor = CardNavy)) {
            ListItem(
                headlineContent = { Text("Privacy and local data") },
                supportingContent = { Text("See what is stored and delete it") },
                leadingContent = { Icon(Icons.Outlined.Security, null, tint = Mint) },
                trailingContent = { Icon(Icons.Outlined.ChevronRight, null, tint = Slate) }
            )
        }
        InfoPanel("Open-source games", "Game rules adapted from Braincup by Simon Schubert under the Apache License 2.0. Full license included with the app.")
        Text("Focus Reset tracks a self-directed habit challenge. It does not diagnose, treat, heal, or measure intelligence.", color = Slate, fontSize = 13.sp)
    }
}

@Composable private fun PrivacyScreen(model: AppViewModel) {
    var confirmDelete by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        TextButton(onClick = model::goBack) { Text("← Settings") }
        Text("PRIVACY", color = Mint, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
        Text("Your attention data belongs to you.", fontSize = 32.sp, lineHeight = 38.sp, fontWeight = FontWeight.Black)
        Text("Solo mode works without an account. The current build does not upload your challenge activity or journal.", color = Slate, fontSize = 17.sp, lineHeight = 25.sp)

        PrivacySection(
            Icons.Outlined.PhoneAndroid,
            "Stored on this device",
            listOf("Challenge start date and daily outcomes", "Private reflection notes", "Game scores and practice duration", "Reminder and accessibility preferences")
        )
        PrivacySection(
            Icons.Outlined.VisibilityOff,
            "Not collected",
            listOf("The Reels or videos you watched", "Messages, photos, contacts, or browser history", "Accessibility Service or VPN traffic", "Individual screen time shared with squads")
        )
        PrivacySection(
            Icons.Outlined.QueryStats,
            "Optional Usage Access",
            listOf("Reports total foreground time for selected apps", "Cannot distinguish Reels from ordinary Instagram use", "Detailed values remain local in this version", "The challenge still works when access is disabled")
        )

        Card(colors = CardDefaults.cardColors(containerColor = Coral.copy(alpha = .10f)), shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Delete all local data", color = Coral, fontWeight = FontWeight.Bold, fontSize = 19.sp)
                Text("Permanently removes challenge history, journal notes, game scores, reminders, and preferences from this device.", color = Slate)
                OutlinedButton(onClick = { confirmDelete = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors(contentColor = Coral)) {
                    Icon(Icons.Outlined.DeleteForever, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Delete everything")
                }
            }
        }
        Text("Cloud backup, accounts, and squads are not active in this local beta build.", color = Slate, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            icon = { Icon(Icons.Outlined.WarningAmber, null, tint = Coral) },
            title = { Text("Delete all Focus Reset data?") },
            text = { Text("This cannot be undone. You will return to onboarding with a completely fresh local profile.") },
            confirmButton = { TextButton(onClick = { confirmDelete = false; model.deleteAllLocalData() }, colors = ButtonDefaults.textButtonColors(contentColor = Coral)) { Text("Delete permanently") } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }
}

@Composable private fun PrivacySection(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, items: List<String>) {
    Card(colors = CardDefaults.cardColors(containerColor = CardNavy), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = Mint)
                Spacer(Modifier.width(10.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            items.forEach { item ->
                Row(verticalAlignment = Alignment.Top) {
                    Text("•", color = Mint)
                    Spacer(Modifier.width(9.dp))
                    Text(item, color = Slate, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable private fun InfoPanel(title: String, detail: String) {
    Card(colors = CardDefaults.cardColors(containerColor = CardNavy), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(detail, color = Slate, lineHeight = 20.sp)
        }
    }
}

private fun GameType.label() = when (this) {
    GameType.GHOST_GRID -> "Ghost Grid"
    GameType.FLASH_CROWD -> "Flash Crowd"
    GameType.PATH_FINDER -> "Path Finder"
    GameType.SCHULTE_TABLE -> "Schulte Table"
    GameType.DIGIT_MEMORY -> "Digit Memory"
}
private fun GameType.skill() = when(this) {
    GameType.GHOST_GRID -> "Repeat a growing sequence of glowing tiles"
    GameType.FLASH_CROWD -> "Compare two brief crowds without counting"
    GameType.PATH_FINDER -> "Follow a bounded route in your head"
    GameType.SCHULTE_TABLE -> "Find shuffled numbers in order"
    GameType.DIGIT_MEMORY -> "Protect a digit sequence through distraction"
}
