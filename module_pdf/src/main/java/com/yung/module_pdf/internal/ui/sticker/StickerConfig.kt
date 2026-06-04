package com.yung.module_pdf.internal.ui.sticker

import android.graphics.Bitmap
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.atan2

sealed class Sticker {
    abstract val id: Long
    abstract var isSelected: Boolean
    abstract var curPage: Int
    abstract var offset: Offset
    abstract var previewArea: IntSize
    abstract val rotation: Float
    abstract val scaleRatio: Float
}

data class TextSticker(
    override val id: Long,
    override var isSelected: Boolean = false,
    override var curPage: Int,
    override var offset: Offset,
    override var previewArea: IntSize,
    override val rotation: Float = 0f,
    override val scaleRatio: Float = 1f,
    var text: TextFieldValue,
    val textLineBreaks: List<IntRange> = emptyList(),
    var trans: Float = 0f,
    var color: Color = textStickerColors[1],
    var fontSize: TextUnit = 14.sp,
    var fontWeight: FontWeight = defFontWeight,
    var fontStyle: FontStyle = FontStyle.Normal,
    var textDecoration: TextDecoration = TextDecoration.None,
    var textAlign: TextAlign = TextAlign.Start,

    var switch: Boolean = true,
    val fontItem: FontItem = defFontItem,
    var focus: Boolean = false,
    var bitmap: Bitmap? = null,
    var withBackground: Boolean = false,
) : Sticker()

const val WATERMARK_PLACEHOLDER = "添加水印"

data class ImageSticker(
    override val id: Long,
    override var isSelected: Boolean = false,
    override var curPage: Int,
    override var offset: Offset,
    override var previewArea: IntSize,
    override val rotation: Float = 0f,
    override val scaleRatio: Float = 1f,
    var bitmap: Bitmap,
) : Sticker()

val watermarkTextColors = listOf(
    Color(0xffD8D8D8),
    Color(0xff7DC4FF),
    Color(0xffFF7D7D),
    Color(0xff868686)
)

val textStickerColors = listOf(
    Color(0xff000000),
    Color(0xff3575CB),
    Color(0xffFF485A),
    Color(0xffF5C340),
    Color(0xff96CEB3),
    Color(0xffC2C2C2),
    Color(0xff4FC0E8),
    Color(0xff7957D0),
    Color(0xffF59A23),
    Color(0xff3BC6BD)
)

val stickerBoxSpace1 = 10.dp//虚线外围
val stickerBoxSpace2 = 10.dp//虚线内围
val stickerBoxSpace3 = 8.dp to 4.dp//文字周围

//贴纸文字左边跟顶部的边距
val stickerBoxSpaceLT =
    (stickerBoxSpace1 + stickerBoxSpace2 + stickerBoxSpace3.first) to (stickerBoxSpace1 + stickerBoxSpace2 + stickerBoxSpace3.second)

fun calculateAngle(offset: Offset): Float {
    val radians = atan2(offset.y.toDouble(), offset.x.toDouble())
    println("calculateAngle:$radians")
//    将角度范围从-180到180缩小到-1到1。这样可以确保旋转角度不会过大，使旋转更加平滑和自然。
    val a = 90
    return (Math.toDegrees(radians) / a).toFloat()
}

fun calculateScale(offset: Offset): Float {
    // 根据拖动方向计算缩放因子
    // 向右拖动放大，向左拖动缩小
    val xComponent = offset.x
    return xComponent / 100f // 调整缩放灵敏度
}

//贴纸遮罩层，避免对PDFView进行事件处理，比如滚动，缩放，点击等
@Composable
fun StickerMaskLayer(stickerBox: @Composable () -> Unit, onTap: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                //禁止添加水印时进行手势
                detectTapGestures(onTap = { onTap() },
                    onDoubleTap = { /* 双击触发 */ },
                    onLongPress = { /* 长按触发 */ },
                    onPress = { /* 按压触发 */ })
            }) {
        stickerBox()
    }
}