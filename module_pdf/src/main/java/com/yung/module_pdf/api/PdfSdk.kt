package com.yung.module_pdf.api

import android.app.Application
import com.yung.module_pdf.internal.data.RecentFileApiImpl
import com.yung.module_pdf.internal.data.RecentFileRepository

/**
 * module_pdf SDK 入口。宿主在 [Application.onCreate] 中调用 [init]。
 */
object PdfSdk {

    private lateinit var application: Application
    private var config: PdfSdkConfig = PdfSdkConfig()
    private val recentFileApi: PdfRecentFileApi by lazy { RecentFileApiImpl() }

    @JvmStatic
    fun init(application: Application, config: PdfSdkConfig = PdfSdkConfig()) {
        this.application = application
        this.config = config
        RecentFileRepository.init(config.recentFileStore)
    }

    @JvmStatic
    fun requireApp(): Application {
        check(::application.isInitialized) {
            "PdfSdk.init(Application) must be called before using module_pdf"
        }
        return application
    }

    /** 最近打开/管理的文件记录（对外 DTO，内部可为 Room 或宿主 [RecentFileStore]）。 */
    @JvmStatic
    fun recentFiles(): PdfRecentFileApi = recentFileApi

    internal fun config(): PdfSdkConfig = config
}
