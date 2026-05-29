package com.yung.module_pdf.internal.core.ext

import android.app.Activity
import android.graphics.Rect

object SoftKeyBoardListener {

    interface OnSoftKeyBoardChangeListener {
        fun keyBoardShow(height: Int)
        fun keyBoardHide()
    }

    fun setListener(activity: Activity, listener: OnSoftKeyBoardChangeListener) {
        val rootView = activity.window.decorView
        var wasVisible = false
        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            rootView.getWindowVisibleDisplayFrame(rect)
            val screenHeight = rootView.rootView.height
            val keypadHeight = screenHeight - rect.bottom
            val visible = keypadHeight > screenHeight * 0.15
            if (visible && !wasVisible) {
                listener.keyBoardShow(keypadHeight)
            } else if (!visible && wasVisible) {
                listener.keyBoardHide()
            }
            wasVisible = visible
        }
    }
}