package com.yung.host

import com.alibaba.android.arouter.launcher.ARouter
import com.yung.route.RoutePath

object HostNavigator {
    fun toHome() {
        ARouter.getInstance().build(RoutePath.Home.MAIN).navigation()
    }

    fun toLogin() {
        ARouter.getInstance().build(RoutePath.User.LOGIN).navigation()
    }

    fun toAbout() {
        ARouter.getInstance().build(RoutePath.User.ABOUT).navigation()
    }
}
