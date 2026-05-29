package com.yung.compose

import androidx.appcompat.app.AppCompatDelegate
import com.blankj.utilcode.util.Utils
import com.yung.base.BaseApplication
import com.yung.compose.recent.HostAppDatabase
import com.yung.compose.recent.HostRecentFileStore
import com.yung.module_pdf.api.PdfSdk
import com.yung.module_pdf.api.PdfSdkConfig
import com.yung.route.RouteInitializer

class MyApplication : BaseApplication() {

    override fun onCreate() {
        super.onCreate()
//        val recentFileStore = HostRecentFileStore(
//            dao = HostAppDatabase.get(this).recentFileDao(),
//        )
//        PdfSdk.init(
//            application = this,
//            config = PdfSdkConfig(recentFileStore = recentFileStore),
//        )
        PdfSdk.init(application = this)
        Utils.init(this)
        RouteInitializer.init(this)
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
