package com.yung.host

import android.content.Context
import com.alibaba.android.arouter.launcher.ARouter
import com.yung.route.RoutePath

object HostNavigator {
    private const val CATEGORY_ID_EXTRA = "extra_category_id"
    private const val CATEGORY_NAME_EXTRA = "extra_category_name"
    fun toHome(context: Context) {
        ARouter.getInstance().build(RoutePath.Home.MAIN).navigation(context)
    }

    fun toWeightEdit(context: Context) {
        ARouter.getInstance().build(RoutePath.Home.WEIGHT_EDIT)
            .withString("extra_initial_grams", "200")
            .navigation(context)
    }

    fun toCategoryList(context: Context) {
        ARouter.getInstance().build(RoutePath.Home.CATEGORY_LIST).navigation(context)
    }

    fun toCategoryFiles(context: Context, categoryId: String, categoryName: String) {
        ARouter.getInstance()
            .build(RoutePath.Home.CATEGORY_FILES)
            .withString(CATEGORY_ID_EXTRA, categoryId)
            .withString(CATEGORY_NAME_EXTRA, categoryName)
            .navigation(context)
    }

    fun toLogin(context: Context) {
        ARouter.getInstance().build(RoutePath.User.LOGIN).navigation(context)
    }

    fun toAbout(context: Context) {
        ARouter.getInstance().build(RoutePath.User.ABOUT).navigation(context)
    }

    fun toIot(context: Context) {
        ARouter.getInstance().build(RoutePath.Iot.MAIN).navigation(context)
    }

    fun toIotProvision(context: Context) {
        ARouter.getInstance().build(RoutePath.Iot.PROVISION_START).navigation(context)
    }

    fun toIotDeviceDetail(context: Context, deviceId: String) {
        ARouter.getInstance()
            .build(RoutePath.Iot.DEVICE_DETAIL)
            .withString("extra_device_id", deviceId)
            .navigation(context)
    }

    fun toIotDebug(context: Context) {
        ARouter.getInstance().build(RoutePath.Iot.DEBUG_MQTT).navigation(context)
    }

    fun toAnr(context: Context) {
        ARouter.getInstance().build(RoutePath.Anr.MAIN).navigation(context)
    }

    fun toAnrReports(context: Context) {
        ARouter.getInstance().build(RoutePath.Anr.REPORTS).navigation(context)
    }
}
