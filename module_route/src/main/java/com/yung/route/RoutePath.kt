package com.yung.route

object RoutePath {
    object Home {
        const val MAIN = "/home/main"
        const val WEIGHT_EDIT = "/home/WeightEdit"
        const val CATEGORY_LIST = "/home/category"
        const val CATEGORY_FILES = "/home/category/files"
    }

    object User {
        const val LOGIN = "/user/login"
        const val ABOUT = "/user/about"
    }

    object Iot {
        const val MAIN = "/iot/main"
        const val DEVICE_LIST = "/iot/device/list"
        const val DEVICE_DETAIL = "/iot/device/detail"
        const val DEVICE_SETTINGS = "/iot/device/settings"
        const val PROVISION_START = "/iot/provision/start"
        const val DEBUG_MQTT = "/iot/debug/mqtt"
    }

    object Anr {
        const val MAIN = "/anr/main"
        const val REPORTS = "/anr/reports"
    }
}
