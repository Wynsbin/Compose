package com.yung.compose

import android.app.Application
import com.yung.route.RouteInitializer

class ComposeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        RouteInitializer.init(this)
    }

    override fun onTerminate() {
        RouteInitializer.destroy()
        super.onTerminate()
    }
}
