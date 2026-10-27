package com.example

import android.app.Application
import com.google.firebase.FirebaseApp

class ReturnRouteApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (FirebaseApp.initializeApp(this) == null) return
        AppCheckInstaller.install(this)
    }
}
