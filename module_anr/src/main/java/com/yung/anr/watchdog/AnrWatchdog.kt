package com.yung.anr.watchdog

import android.app.Application
import android.util.Log
import com.github.anrwatchdog.ANRWatchDog

/**
 * Debug / 测试包 ANR 采集入口。
 *
 * 真实项目典型用法（无 UI 侵入）：
 * 1. Application.onCreate 中 init，仅 debug / 内测渠道启用
 * 2. 监听到 ANR 后 Log + 落盘（默认行为）
 * 3. 通过 [AnrWatchdogConfig.onAnrDetected] 上报 APM，或 adb pull cache 目录排查
 * 4. 需要看堆栈时，从宿主调试菜单进入 [com.yung.anr.AnrReportActivity]
 */
object AnrWatchdog {

    private const val TAG = "AnrWatchdog"

    /** 与系统 Input dispatching ANR 阈值一致 */
    const val DEFAULT_TIMEOUT_MS = 5_000

    @Volatile
    private var started = false

    fun init(application: Application, enabled: Boolean) {
        init(application, AnrWatchdogConfig(enabled = enabled))
    }

    fun init(application: Application, config: AnrWatchdogConfig) {
        if (!config.enabled || started) return
        started = true
        AnrWatchdogStore.init(application)
        ANRWatchDog(config.timeoutMs)
            .setANRListener { error ->
                Log.e(TAG, "ANR detected by watchdog", error)
                AnrWatchdogStore.saveReport(error)
                config.onAnrDetected?.invoke(error)
            }
            .start()
        Log.i(TAG, "ANRWatchDog started, timeout=${config.timeoutMs}ms, dir=${AnrWatchdogStore.getReportDirPath()}")
    }

    fun isRunning(): Boolean = started
}
