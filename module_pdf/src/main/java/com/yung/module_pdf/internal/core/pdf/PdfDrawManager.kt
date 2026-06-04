package com.yung.module_pdf.internal.core.pdf

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.TypedValue
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.res.ResourcesCompat
import com.blankj.utilcode.util.ToastUtils
import com.yung.module_pdf.internal.ui.sticker.ImageSticker
import com.yung.module_pdf.internal.ui.sticker.Sticker
import com.yung.module_pdf.internal.ui.sticker.TextSticker
import com.yung.module_pdf.internal.ui.sticker.stickerBoxSpaceLT
import com.yung.module_pdf.internal.core.ext.dp2px
import com.yung.module_pdf.internal.core.ext.toAndroidColor

/**
 * PDFView只是通过onDrawAll进行贴纸的预览绘制
 * 导出需要通过PDDocument进行真正绘制到新的PDF文件
 */

object PdfDrawManager {

    private val bgPaint = Paint().apply {
        color = android.graphics.Color.TRANSPARENT
        isAntiAlias = true
    }

    private val textBgPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val textBgRadiusPx = 4.dp2px

    //绘制单个贴纸
    fun drawSticker(
        context: Context,
        sticker: Sticker,
        canvas: Canvas,
        page: Int,
        stickerClickHandler: StickerClickHandler? = null,
    ) {
        runCatching {
            when (sticker) {
                is TextSticker -> {
                    val paint = Paint().apply {
                        color = sticker.color.copy(alpha = 1f - sticker.trans).toAndroidColor()
                        textSize = TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_SP,
                            sticker.fontSize.value,
                            Resources.getSystem().displayMetrics
                        )
                        // 自定义字体+粗体
                        val style =
                            if (sticker.fontWeight == FontWeight.Bold) Typeface.BOLD else Typeface.NORMAL

                        typeface = sticker.fontItem.fontResId.takeIf { it != 0 }?.let { resId ->
                            ResourcesCompat.getFont(context, resId)
                                ?.let { font -> Typeface.create(font, style) }
                        } ?: Typeface.defaultFromStyle(style)

                        isAntiAlias = true
                    }

                    val xPos = sticker.offset.x
                    val yPos = sticker.offset.y

                    // 1. 文本尺寸（未缩放，用来算比例）
                    val text = sticker.text.text

                    val lineBreaks = if (text.isNotEmpty() && sticker.textLineBreaks.isEmpty()) {
                        listOf(IntRange(0, text.length))
                    } else {
                        sticker.textLineBreaks
                    }

                    val lineCount = lineBreaks.size
                    val newText = lineBreaks.map { range ->
                        text.substring(range.first, range.last)
                    }
                    val lineWidths = newText.map { paint.measureText(it) }
                    val textWidth = lineWidths.maxOrNull() ?: 0f
                    val fm = paint.fontMetrics
                    val textRealHeight = fm.bottom - fm.top
                    val paddingHor = stickerBoxSpaceLT.first.value.dp2px
                    val paddingVer = stickerBoxSpaceLT.second.value.dp2px
                    val bgW = textWidth + paddingHor * 2
                    val bgH = textRealHeight * lineCount + paddingVer * 2
                    val cx = bgW / 2f
                    val cy = bgH / 2f
                    val bgRect = RectF(-cx, -cy, cx, cy)
                    val centerX = xPos + cx // 计算背景矩形中心点在屏幕上的绝对X坐标
                    val centerY = yPos + cy // 计算背景矩形中心点在屏幕上的绝对Y坐标
                    // 3. 画布变换顺序：先平移 → 再缩放 → 再旋转
                    canvas.translate(centerX, centerY) // 1. 原点移到目标中心点
                    canvas.scale(sticker.scaleRatio, sticker.scaleRatio) // 2. 整体放大
                    canvas.rotate(sticker.rotation)          // 3. 绕新原点旋转

                    // 4. 画背景
                    if (sticker.withBackground) {
                        canvas.drawRoundRect(bgRect, textBgRadiusPx, textBgRadiusPx, textBgPaint)
                    } else {
                        canvas.drawRoundRect(bgRect, 0f, 0f, bgPaint)
                    }

                    // 计算文本垂直居中的起始位置
                    val textStartY =
                        -((textRealHeight * lineCount) / 2) + textRealHeight - fm.descent
                    // 5. 画文字（边距也是放大后的像素）
                    newText.forEachIndexed { index, str ->
                        canvas.drawText(
                            str,
                            -lineWidths[index] / 2f,
                            textStartY + textRealHeight * index,
                            paint,
                        )
                    }

                    //点击事件处理
                    stickerClickHandler?.let {
                        //移动缩放旋转后的矩阵
                        val matrix = android.graphics.Matrix().apply {
                            postTranslate(centerX, centerY)
                            postScale(sticker.scaleRatio, sticker.scaleRatio, centerX, centerY)
                            postRotate(sticker.rotation, centerX, centerY)
                        }
                        val transformedRect = RectF()
                        matrix.mapRect(transformedRect, bgRect)

                        // 添加可点击区域（传递原始矩形和变换矩阵）
                        it.addClickableRegion(page, sticker.id, transformedRect)
                    }
                }

                is ImageSticker -> {
                    val xPos = sticker.offset.x
                    val yPos = sticker.offset.y

                    //边距（未缩放值）
                    val paddingHor = stickerBoxSpaceLT.first.value.dp2px
                    val paddingVer = stickerBoxSpaceLT.second.value.dp2px
                    val bgW = sticker.bitmap.width + paddingHor * 2
                    val bgH = sticker.bitmap.height + paddingVer * 2
                    val cx = bgW / 2f
                    val cy = bgH / 2f
                    val bgRect = RectF(-cx, -cy, cx, cy)
                    val centerX = xPos + cx // 计算背景矩形中心点在屏幕上的绝对X坐标
                    val centerY = yPos + cy // 计算背景矩形中心点在屏幕上的绝对Y坐标
                    canvas.translate(centerX, centerY) // 1. 原点移到目标中心点
                    canvas.scale(sticker.scaleRatio, sticker.scaleRatio) // 2. 整体放大
                    canvas.rotate(sticker.rotation)          // 3. 绕新原点旋转

                    // 画背景
                    canvas.drawRoundRect(bgRect, 0f, 0f, bgPaint)
                    canvas.drawBitmap(sticker.bitmap, -cx + paddingHor, -cy + paddingVer, null)

                    //点击事件处理
                    stickerClickHandler?.let {
                        //移动缩放旋转后的矩阵
                        val matrix = android.graphics.Matrix().apply {
                            postTranslate(centerX, centerY)
                            postScale(sticker.scaleRatio, sticker.scaleRatio, centerX, centerY)
                            postRotate(sticker.rotation, centerX, centerY)
                        }
                        val transformedRect = RectF()
                        matrix.mapRect(transformedRect, bgRect)

                        // 添加可点击区域（传递原始矩形和变换矩阵）
                        it.addClickableRegion(page, sticker.id, transformedRect)
                    }
                }
            }
        }.onFailure {
            ToastUtils.showShort("绘制贴纸错误")
        }
    }

    //绘制贴纸到PDFView上进行预览
    fun drawStickerToPrePage(
        context: Context,
        textStickerList: List<Sticker>,
        canvas: Canvas,
        page: Int,
        stickerClickHandler: StickerClickHandler? = null,
    ) {
        textStickerList.filter { it.curPage == page }.forEach { sticker ->
            // 保存画布状态
            canvas.save()
            //绘制文字或者图片贴纸
            drawSticker(context, sticker, canvas, page, stickerClickHandler)
            // 恢复画布状态
            canvas.restore()
        }
    }

    //绘制分割线和页码
    fun drawDividingLinesAndPageNumbers(
        canvas: Canvas,
        pageWidth: Float,
        pageHeight: Float,
        displayedPage: Int,
    ) {
        // 3. 新增：在页面底部绘制一条分割线（模拟页与页之间的间隔）
        // 分割线的属性
        val dividerHeight = 1f // 分割线的高度（像素）
        val dividerColor = android.graphics.Color.GRAY // 分割线的颜色

        val paint = Paint().apply {
            color = dividerColor
            style = android.graphics.Paint.Style.FILL // 填充样式
            isAntiAlias = true // 抗锯齿
        }

        // 计算当前页底部在Canvas中的Y坐标
        // 注意：PDF页面的坐标原点可能在左上角，且绘制区域可能只限于当前页面
        // 假设PDF页面在Canvas中是从Y=0开始绘制的
        val currentPageBottomY = pageHeight // 当前页的底部Y坐标

        // 绘制分割线：一个矩形，位于当前页底部下方
        // 这里我们在当前页的底部绘制一条线，模拟与下一页的间隔
        // 注意：实际效果可能需要根据你的PDFView的滚动和缩放行为进行调整
        val dividerTop = currentPageBottomY
        val dividerBottom = dividerTop + dividerHeight

        canvas.drawRect(0f, dividerTop, pageWidth, dividerBottom, paint)

        // 4. 如果你希望在每个PDF页面都绘制一些内容（例如页码），也可以在这里处理
        // 例如，在右下角绘制页码（这里只是示例，你可能已经有了）
        val pageNumberPaint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 24f
            isAntiAlias = true
        }
        val pageNumberText = "${displayedPage + 1}"
        val textWidth = pageNumberPaint.measureText(pageNumberText)
        val textX = pageWidth - textWidth - 20f // 右边距20像素
        val textY = pageHeight - 20f // 下边距20像素
        canvas.drawText(pageNumberText, textX, textY, pageNumberPaint)
    }
}

