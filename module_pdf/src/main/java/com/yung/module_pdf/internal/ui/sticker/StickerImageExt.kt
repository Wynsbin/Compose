package com.yung.module_pdf.internal.ui.sticker

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import coil.compose.AsyncImage
import com.yung.module_pdf.R
import com.yung.module_pdf.internal.core.ext.dp2px
import com.yung.module_pdf.internal.ui.component.noRippleClickable
import kotlin.math.cos
import kotlin.math.sin


/**
 * 图片贴纸
 */
@Composable
fun ImageStickerBox(
    sticker: ImageSticker,
    onDelete: () -> Unit,
    onCopy: () -> Unit = {},
    onUpdateOffset: (Offset) -> Unit = {},
    onUpdateRotationAndScale: (Float, Float) -> Unit = { f1, f2 -> },
) {
    // 获取屏幕宽度
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    // 计算最大宽度为屏幕宽度的80%
    val maxWidth = screenWidthDp * 0.8f

    var offset by remember(sticker.offset) { mutableStateOf(sticker.offset) }
    var waterSize by remember { mutableStateOf(IntSize.Zero) }
    val isSelected by remember(sticker.isSelected) { mutableStateOf(sticker.isSelected) }
    var isDragging by remember { mutableStateOf(false) }
    var rotation by remember(sticker.rotation) { mutableStateOf(sticker.rotation) }
    var scaleRatio by remember(sticker.scaleRatio) { mutableStateOf((sticker.scaleRatio)) }
    val cachedBitmap = remember(sticker.bitmap) { sticker.bitmap }

    ConstraintLayout(modifier = Modifier
        .graphicsLayer {
            translationX = offset.x
            translationY = offset.y
            rotationZ = rotation
            scaleX = scaleRatio
            scaleY = scaleRatio
        }
        .onSizeChanged {
            if (offset == Offset.Zero) {
                offset = Offset(
                    (sticker.previewArea.width - it.width) / 2f,
                    (sticker.previewArea.height - it.height) / 2f
                )
                onUpdateOffset(offset)
            }
            waterSize = it
        }) {
        val (edit, delete, copy, move) = createRefs()
        Box(modifier = Modifier
            .padding(stickerBoxSpace1)
            .constrainAs(edit) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
            .drawBehind {
                val radius = 4.dp.toPx()          // 圆角半径
                val roundRectPath = Path().apply {
                    addRoundRect(RoundRect(0f, 0f, size.width, size.height, radius, radius))
                }

                drawPath(
                    path = roundRectPath,
                    color = if (isSelected) Color.Black else Color.Transparent,
                    style = Stroke(
                        width = 1.dp2px, pathEffect = PathEffect.dashPathEffect(
                            intervals = floatArrayOf(5.dp2px, 3.dp2px), phase = 0f
                        )
                    )
                )
            }
            .padding(stickerBoxSpace2)
            .pointerInput(Unit) {
                detectDragGestures(onDragStart = {
                    isDragging = true
                }, onDragEnd = {
                    isDragging = false
                }, onDrag = { change, dragAmount ->
                    change.consume()
                    // 获取当前的旋转角度（假设在Compose状态中存储，比如rotationAngle是当前组件的旋转角度状态）
                    val angleRad = -Math.toRadians(rotation.toDouble())
                    val cos = cos(angleRad)
                    val sin = sin(angleRad)
                    val transformedDragAmount = Offset(
                        x = (dragAmount.x * cos + dragAmount.y * sin).toFloat() * scaleRatio,
                        y = (-dragAmount.x * sin + dragAmount.y * cos).toFloat() * scaleRatio
                    )
                    val newOffset = offset + transformedDragAmount
                    val offsetX = newOffset.x
                    val offsetY = newOffset.y.coerceIn(
                        0.1f, (sticker.previewArea.height - waterSize.height).toFloat()
                    )
                    offset = Offset(offsetX, offsetY)
                    onUpdateOffset(offset)
                }, onDragCancel = {
                    isDragging = false
                })
            }) {

            AsyncImage(
                model = cachedBitmap,
                contentDescription = null,
                modifier = Modifier
                    .padding(stickerBoxSpace3.first, stickerBoxSpace3.second)
                    .widthIn(max = maxWidth),
            )
        }

        if (isSelected) {
            Image(painter = painterResource(id = R.mipmap.module_pdf_edit_icon_sc),
                contentDescription = null,
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer {
                        // 应用反向缩放，抵消父Box的缩放效果
                        scaleX = 1 / scaleRatio
                        scaleY = 1 / scaleRatio
                    }
                    .constrainAs(delete) {
                        top.linkTo(edit.top)
                        end.linkTo(edit.end)
                    }
                    .noRippleClickable { onDelete() })
            Image(painter = painterResource(id = R.mipmap.module_pdf_edit_icon_fz),
                contentDescription = null,
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer {
                        // 应用反向缩放，抵消父Box的缩放效果
                        scaleX = 1 / scaleRatio
                        scaleY = 1 / scaleRatio
                    }
                    .constrainAs(copy) {
                        end.linkTo(edit.end)
                        bottom.linkTo(edit.bottom)
                    }
                    .noRippleClickable { onCopy() })
            Image(painter = painterResource(id = R.mipmap.module_pdf_edit_icon_xz),
                contentDescription = null,
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer {
                        // 应用反向缩放，抵消父Box的缩放效果
                        scaleX = 1 / scaleRatio
                        scaleY = 1 / scaleRatio
                    }
                    .constrainAs(move) {
                        start.linkTo(edit.start)
                        bottom.linkTo(edit.bottom)
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val dragOffset = Offset(-dragAmount.x, -dragAmount.y)
                            val angleChange = calculateAngle(dragOffset)
                            val scaleChange = calculateScale(dragOffset)
                            rotation += angleChange
                            scaleRatio = (scaleRatio + scaleChange).coerceIn(1f, 2f) // 限制缩放范围
                            onUpdateRotationAndScale(rotation, scaleRatio)
                        }
                    })
        }
    }
}
