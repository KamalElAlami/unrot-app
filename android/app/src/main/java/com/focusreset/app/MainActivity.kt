package com.focusreset.app

import android.app.usage.UsageStatsManager
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focusreset.app.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FocusResetTheme {
                val model: AppViewModel = viewModel()
                val state by model.state.collectAsState()
                LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { model.refresh() }
                FocusResetApp(
                    state = state,
                    model = model,
                    openUsageAccess = { startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) },
                    share = { text -> startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }, "Share challenge")) }
                )
            }
        }
    }

}
