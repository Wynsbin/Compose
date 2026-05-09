package com.yung.route

import android.app.Application
import android.content.pm.ApplicationInfo
import com.alibaba.android.arouter.launcher.ARouter

object RouteInitializer {
    fun init(application: Application) {
        // 不要用 com.alibaba.android.arouter.BuildConfig：那是依赖库自身的开关，几乎恒为 false，
        // 会导致 openDebug/openLog 永远不执行，多模块下常见「找不到路由、无法跳转」。
        val isDebuggable =
            (application.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (isDebuggable) {
            ARouter.openLog()
            ARouter.openDebug()
        }
        ARouter.init(application)
    }

    fun destroy() {
        ARouter.getInstance().destroy()
    }
}
