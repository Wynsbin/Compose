package com.yung.anr.scenario

enum class AnrScenario(
    val title: String,
    val description: String,
    val anrType: String,
) {
    MAIN_THREAD_SLEEP(
        title = "主线程 Sleep",
        description = "在主线程调用 Thread.sleep，模拟 Input dispatching 超时（约 5s）",
        anrType = "Input dispatching timed out",
    ),
    MAIN_THREAD_BUSY_LOOP(
        title = "主线程密集计算",
        description = "主线程执行大量浮点运算，CPU 占满导致无法处理输入事件",
        anrType = "Input dispatching timed out",
    ),
    MAIN_THREAD_FILE_IO(
        title = "主线程同步 IO",
        description = "主线程循环读写临时文件，模拟磁盘 IO 阻塞",
        anrType = "Input dispatching timed out",
    ),
    SYNCHRONIZED_CONTENTION(
        title = "主线程锁竞争",
        description = "后台线程持有 synchronized 锁，主线程等待同一把锁",
        anrType = "Input dispatching timed out",
    ),
    MAIN_THREAD_DEADLOCK(
        title = "主线程死锁",
        description = "主线程与后台线程互相等待对方持有的锁，永久阻塞",
        anrType = "Input dispatching timed out",
    ),
    MAIN_THREAD_JOIN(
        title = "主线程 Thread.join",
        description = "主线程 join 等待子线程，子线程又通过 Handler 等待主线程",
        anrType = "Input dispatching timed out",
    ),
    BROADCAST_RECEIVER(
        title = "BroadcastReceiver 阻塞",
        description = "onReceive 中长时间阻塞，触发 Broadcast 超时（约 10s）",
        anrType = "Broadcast of Intent",
    ),
    CONTENT_PROVIDER_QUERY(
        title = "ContentProvider 查询阻塞",
        description = "主线程查询自定义 Provider，query() 中 sleep 阻塞",
        anrType = "ContentProvider not responding",
    ),
    SERVICE_ON_CREATE(
        title = "Service onCreate 阻塞",
        description = "startService 在主线程同步执行 onCreate，其中长时间 sleep",
        anrType = "executing service",
    ),
}
