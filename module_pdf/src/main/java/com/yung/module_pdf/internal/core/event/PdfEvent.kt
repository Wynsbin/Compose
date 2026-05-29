package com.yung.module_pdf.internal.core.event

/** SDK 内部 EventBus 事件（不依赖宿主 lib_base）。 */
data class PdfEvent(
    val code: Int,
    val data: Any? = null,
)
