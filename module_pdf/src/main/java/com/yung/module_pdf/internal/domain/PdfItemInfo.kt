package com.yung.module_pdf.internal.domain

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PdfItemInfo(
    val id: Long,
    val bitmap: Bitmap?,
    val width: Int,
    val height: Int,
    val page: Int,
    val rotationAngle: Int = 0,
) : Parcelable