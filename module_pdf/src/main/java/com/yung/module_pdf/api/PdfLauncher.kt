package com.yung.module_pdf.api

import android.content.Context
import com.yung.module_pdf.internal.ui.activity.PdfSelectActivity

/** 宿主启动 module_pdf 页面的统一入口。 */
object PdfLauncher {

    @JvmStatic
    fun openPdfSelect(context: Context, mode: PdfSelectMode) {
        PdfSelectActivity.start(context, mode)
    }
}
