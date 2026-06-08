package com.yung.anr.scenario

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.yung.anr.provider.BlockingContentProvider
import com.yung.anr.receiver.BlockingBroadcastReceiver
import com.yung.anr.service.BlockingService
import java.io.File
import java.util.concurrent.CountDownLatch
import kotlin.math.sqrt

object AnrScenarioExecutor {

    const val BLOCK_DURATION_MS = 20_000L

    fun trigger(context: Context, scenario: AnrScenario) {
        when (scenario) {
            AnrScenario.MAIN_THREAD_SLEEP -> mainThreadSleep()
            AnrScenario.MAIN_THREAD_BUSY_LOOP -> mainThreadBusyLoop()
            AnrScenario.MAIN_THREAD_FILE_IO -> mainThreadFileIo(context)
            AnrScenario.SYNCHRONIZED_CONTENTION -> synchronizedContention()
            AnrScenario.MAIN_THREAD_DEADLOCK -> mainThreadDeadlock()
            AnrScenario.MAIN_THREAD_JOIN -> mainThreadJoin()
            AnrScenario.BROADCAST_RECEIVER -> broadcastReceiverBlock(context)
            AnrScenario.CONTENT_PROVIDER_QUERY -> contentProviderQueryBlock(context)
            AnrScenario.SERVICE_ON_CREATE -> serviceOnCreateBlock(context)
        }
    }

    private fun mainThreadSleep() {
        Thread.sleep(BLOCK_DURATION_MS)
    }

    private fun mainThreadBusyLoop() {
        val deadline = System.currentTimeMillis() + BLOCK_DURATION_MS
        var accumulator = 0.0
        while (System.currentTimeMillis() < deadline) {
            for (i in 1..50_000) {
                accumulator += sqrt(i.toDouble()) * Math.sin(i.toDouble())
            }
        }
        @Suppress("UNUSED_VARIABLE")
        val ignored = accumulator
    }

    private fun mainThreadFileIo(context: Context) {
        val file = File(context.cacheDir, "anr_io_stress.tmp")
        val payload = ByteArray(512 * 1024) { (it % 256).toByte() }
        val deadline = System.currentTimeMillis() + BLOCK_DURATION_MS
        while (System.currentTimeMillis() < deadline) {
            file.outputStream().use { it.write(payload) }
            file.inputStream().use { input ->
                val buffer = ByteArray(64 * 1024)
                while (input.read(buffer) != -1) {
                    // keep reading until EOF
                }
            }
        }
    }

    private fun synchronizedContention() {
        val lock = Any()
        Thread {
            synchronized(lock) {
                Thread.sleep(BLOCK_DURATION_MS)
            }
        }.start()
        Thread.sleep(300)
        synchronized(lock) {
            // waits until background thread releases lock
        }
    }

    private fun mainThreadDeadlock() {
        val lockA = Any()
        val lockB = Any()
        Thread {
            synchronized(lockB) {
                Thread.sleep(200)
                synchronized(lockA) {
                    // never reached
                }
            }
        }.start()
        Thread.sleep(100)
        synchronized(lockA) {
            synchronized(lockB) {
                // deadlock: main holds A waits B, worker holds B waits A
            }
        }
    }

    private fun mainThreadJoin() {
        val latch = CountDownLatch(1)
        val worker = Thread {
            Handler(Looper.getMainLooper()).post {
                latch.countDown()
            }
            latch.await()
        }
        worker.start()
        worker.join()
    }

    private fun broadcastReceiverBlock(context: Context) {
        context.sendBroadcast(
            Intent(BlockingBroadcastReceiver.ACTION).setPackage(context.packageName),
        )
    }

    private fun contentProviderQueryBlock(context: Context) {
        val uri = Uri.parse("content://${BlockingContentProvider.authority(context)}/block")
        context.contentResolver.query(uri, null, null, null, null)?.close()
    }

    private fun serviceOnCreateBlock(context: Context) {
        context.startService(Intent(context, BlockingService::class.java))
    }
}
