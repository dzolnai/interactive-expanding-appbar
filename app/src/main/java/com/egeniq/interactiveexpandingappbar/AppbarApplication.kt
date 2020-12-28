package com.egeniq.interactiveexpandingappbar

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AppbarApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        CollectionGridHelper.init(this)
    }
}