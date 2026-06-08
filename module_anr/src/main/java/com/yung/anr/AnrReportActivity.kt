package com.yung.anr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.alibaba.android.arouter.facade.annotation.Route
import com.yung.anr.ui.AnrReportScreen
import com.yung.route.RoutePath

/** Debug 专用：查看 Watchdog 采集的 ANR 堆栈，与业务页面解耦。 */
@Route(path = RoutePath.Anr.REPORTS)
class AnrReportActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AnrReportScreen()
        }
    }
}
