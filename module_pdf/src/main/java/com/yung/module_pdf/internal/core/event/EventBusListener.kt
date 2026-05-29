package com.yung.module_pdf.internal.core.event

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@Composable
fun <T> EventBusListener(clazz: Class<T>, onEvent: (T) -> Unit) {
    DisposableEffect(clazz) {
        val subscriber = object {
            @Subscribe(threadMode = ThreadMode.MAIN)
            fun onEventReceived(event: Any) {
                if (clazz.isInstance(event)) {
                    @Suppress("UNCHECKED_CAST")
                    onEvent(event as T)
                }
            }
        }
        EventBus.getDefault().register(subscriber)
        onDispose {
            EventBus.getDefault().unregister(subscriber)
        }
    }
}