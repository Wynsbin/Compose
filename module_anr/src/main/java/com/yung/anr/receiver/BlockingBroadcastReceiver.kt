package com.yung.anr.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.yung.anr.scenario.AnrScenarioExecutor

class BlockingBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != ACTION) return
        Thread.sleep(AnrScenarioExecutor.BLOCK_DURATION_MS)
    }

    companion object {
        const val ACTION = "com.yung.anr.action.BLOCKING_BROADCAST"
    }
}
