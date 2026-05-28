package com.yung.base

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

/**
 * 精简版 Application 基类（原 pdf_editor 工程在 lib_base 本地提供，ijjCore 不包含此类）。
 */
abstract class BaseApplication : Application() {

    companion object {
        @Volatile
        private var application: BaseApplication? = null

        @JvmStatic
        fun getApplication(): BaseApplication =
            requireNotNull(application) { "BaseApplication is not initialized" }
    }

    override fun onCreate() {
        super.onCreate()
        application = this
    }

    abstract fun appIsDebug(): Boolean

    abstract fun getChannel(): String

    abstract fun getHttpHost(): String

    @get:AppCompatDelegate.NightMode
    abstract val dayNightMode: Int

    open fun initNormalSdk() {}

    open fun initThirdSdk() {}
}