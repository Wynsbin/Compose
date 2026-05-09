package com.yung.host

import android.content.Context
import com.alibaba.android.arouter.launcher.ARouter
import com.yung.route.RoutePath

object HostNavigator {
    fun toHome(context: Context) {
        ARouter.getInstance().build(RoutePath.Home.MAIN).navigation(context)
    }

    fun toWeightEdit(context: Context) {
        ARouter.getInstance().build(RoutePath.Home.WEIGHT_EDIT).navigation(context)
    }

    fun toLogin(context: Context) {
        ARouter.getInstance().build(RoutePath.User.LOGIN).navigation(context)
    }

    fun toAbout(context: Context) {
        ARouter.getInstance().build(RoutePath.User.ABOUT).navigation(context)
    }
}
