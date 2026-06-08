package com.yung.anr.watchdog

data class AnrWatchdogConfig(
    val enabled: Boolean = true,
    val timeoutMs: Int = AnrWatchdog.DEFAULT_TIMEOUT_MS,
    /** 真实项目可在此上报 Bugly / 自研 APM，无需依赖任何 UI */
    val onAnrDetected: ((Error) -> Unit)? = null,
)
