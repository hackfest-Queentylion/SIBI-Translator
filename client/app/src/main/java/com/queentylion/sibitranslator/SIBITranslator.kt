package com.queentylion.sibitranslator

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SIBITranslator: Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = this
    }
}

lateinit var appContext: Context