package com.focusreset.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focusreset.app.ui.*
import com.focusreset.app.sharing.ShareCardRenderer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FocusResetTheme {
                val model: AppViewModel = viewModel()
                val state by model.state.collectAsState()
                val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                    model.setReminderEnabled(granted)
                }
                LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { model.refresh() }
                FocusResetApp(
                    state = state,
                    model = model,
                    openUsageAccess = { startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) },
                    requestReminderPermission = {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                            model.setReminderEnabled(true)
                        } else notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    },
                    share = { text ->
                        val card = ShareCardRenderer.create(this, text)
                        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                            type = "image/png"
                            putExtra(Intent.EXTRA_TEXT, text)
                            putExtra(Intent.EXTRA_STREAM, card)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }, "Share Focus Reset"))
                    }
                )
            }
        }
    }

}
