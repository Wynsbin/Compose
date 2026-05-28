package com.yung.module_pdf.utils

import java.util.concurrent.atomic.AtomicInteger

/**
 * 自定义唯一id
 */
//如果你真的只想用 Long 型时间戳，务必再加“进程内自增”或“随机尾缀”做保险：
object IdGen {
    private val seq = AtomicInteger(0)
    fun next(): Long =
        System.currentTimeMillis() * 1000 + seq.incrementAndGet() % 1000
}