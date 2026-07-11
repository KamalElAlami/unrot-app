package com.focusreset.app

import android.app.Application
import com.focusreset.app.data.AppDatabase

class FocusResetApplication : Application() {
    val database by lazy { AppDatabase.create(this) }
}
