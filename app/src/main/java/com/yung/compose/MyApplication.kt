package com.yung.compose

import androidx.appcompat.app.AppCompatDelegate
import com.blankj.utilcode.util.Utils
import com.yung.base.BaseApplication
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.yung.module_pdf.api.PdfSdk
import com.yung.route.RouteInitializer

class MyApplication : BaseApplication() {

    override fun onCreate() {
        super.onCreate()
        PdfSdk.init(this)
        Utils.init(this)
        RouteInitializer.init(this)
        PDFBoxResourceLoader.init(applicationContext)
    }

    override fun onTerminate() {
        RouteInitializer.destroy()
        super.onTerminate()
    }

    override fun appIsDebug(): Boolean = BuildConfig.DEBUG

    override fun getChannel(): String = "default"

    override fun getHttpHost(): String = "example.com"

    override val dayNightMode: Int = AppCompatDelegate.MODE_NIGHT_NO
}
