package com.yung.anr.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.yung.anr.scenario.AnrScenarioExecutor

class BlockingService : Service() {

    override fun onCreate() {
        super.onCreate()
        Thread.sleep(AnrScenarioExecutor.BLOCK_DURATION_MS)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
