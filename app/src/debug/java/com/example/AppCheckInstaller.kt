package com.example

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

internal object AppCheckInstaller {
    fun install(context: Context) {
        val app = FirebaseApp.getInstance()
        FirebaseAppCheck.getInstance(app)
            .installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())
    }
}
