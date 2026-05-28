package com.yung.module_pdf.utils

import android.content.res.Resources

val Float.dp2px: Float
    get() = this * Resources.getSystem().displayMetrics.density

val Int.dp2px: Float
    get() = this * Resources.getSystem().displayMetrics.density
