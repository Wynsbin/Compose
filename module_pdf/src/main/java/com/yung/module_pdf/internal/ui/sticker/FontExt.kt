package com.yung.module_pdf.internal.ui.sticker

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.yung.module_pdf.R

data class FontItem(val fontName: String, val fontResId: Int) {
    fun getFontFamily(): FontFamily {
        return if (fontResId == 0) FontFamily.Default else FontFamily(Font(fontResId))
    }
}

val defFontItem = FontItem("默认", 0)

val stickerFontList = listOf(
    FontItem("Fandol楷体", R.font.fandolkai),
    FontItem("凌慧体", R.font.linghuiti),
    FontItem("娃娃体", R.font.wawati),
    FontItem("庞门正道轻松体", R.font.pangmenzhengdao),
    FontItem("思源黑体CN", R.font.siyuanheiti_cn),
    FontItem("阿里巴巴普惠体中黑", R.font.alibabapuhuiti_medium),
    FontItem("阿里巴巴普惠体常规", R.font.alibabapuhuiti_regular),
    FontItem("阿里巴巴普惠体特黑", R.font.alibabapuhuiti_heavy),
    FontItem("阿里巴巴普惠体粗", R.font.alibabapuhuiti_bold),
    FontItem("阿里巴巴普惠体细", R.font.alibabapuhuiti_light),
    FontItem("魏碑", R.font.weibei)
)

val defFontWeight = FontWeight.Normal

fun FontWeight.switchFontWeight() = if (this == defFontWeight) FontWeight.Bold else defFontWeight