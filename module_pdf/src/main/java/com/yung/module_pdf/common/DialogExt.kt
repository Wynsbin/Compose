package com.yung.module_pdf.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.blankj.utilcode.util.ToastUtils
import com.yung.module_pdf.R

@Composable
fun DialogDoubleBtn(
    leftStr: String = "取消",
    rightStr: String = "确认",
    onClickLeft: () -> Unit,
    onClickRight: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(text = leftStr,
            color = Color(0xff333333),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .height(46.dp)
                .background(Color(0xffEEEFEF), RoundedCornerShape(10.dp))
                .border(1.dp, Color(0xffEAEAEA), RoundedCornerShape(10.dp))
                .noRippleClickable { onClickLeft() }
                .wrapContentWidth(Alignment.CenterHorizontally)
                .wrapContentHeight(Alignment.CenterVertically))
        Spacer(modifier = Modifier.width(14.dp))
        Text(text = rightStr,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .height(46.dp)
                .background(Color(0xffF5341A), RoundedCornerShape(10.dp))
                .noRippleClickable { onClickRight() }
                .wrapContentWidth(Alignment.CenterHorizontally)
                .wrapContentHeight(Alignment.CenterVertically))
    }
}

@Composable
fun WatermarkTextInputDialog(
    value: String,
    onDismiss: () -> Unit,
    onComplete: (String) -> Unit,
) {
    var inputText by remember { mutableStateOf(value) }

    LaunchedEffect(value) {
        inputText = value
    }

    Dialog(onDismissRequest = { onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xffFFECEC), Color.White)
                        ), shape = RoundedCornerShape(20.dp)
                    )
                    .padding(20.dp, 24.dp),
            ) {
                Text(
                    text = "水印内容",
                    color = Color(0XFF252525),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                CusEditView(value = inputText,
                    onValueChange = {
                        if (it.length <= 20) {
                            inputText = it
                        }
                    },
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .fillMaxWidth()
                        .height(47.dp)
                        .background(Color.White, RoundedCornerShape(10.dp))
                        .border(0.3.dp, Color(0xffD8DBDD), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp),
                    placeholder = {
                        Text(text = "请输入添加内容", color = Color(0xffBFBFBF), fontSize = 14.sp)
                    })

                Text(
                    text = "${inputText.length}/20", color = Color(0xff999999), fontSize = 12.sp,
                    modifier = Modifier
                        .padding(top = 5.dp, bottom = 11.dp)
                        .align(Alignment.End)
                )

                DialogDoubleBtn(leftStr = "取消", rightStr = "确认",
                    onClickLeft = { onDismiss() },
                    onClickRight = {
                        if (inputText.isEmpty()) {
                            ToastUtils.showShort("请输入水印内容")
                        } else {
                            onComplete(inputText)
                            onDismiss()
                        }
                    })
            }
        })
}


@Composable
fun ExitEditPromptDialog(
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    Dialog(onDismissRequest = { onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .background(
                        brush = Brush.verticalGradient(
                            0.1f to Color(0xffFFECEC),
                            0.3f to Color.White
                        ), shape = RoundedCornerShape(20.dp)
                    )
                    .padding(20.dp, 24.dp),
            ) {
                Text(
                    text = "退出编辑提示",
                    color = Color(0XFF252525),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Text(
                    text = "当前文档已对页面进行编辑，\n是否保存修改内容？",
                    color = Color(0XFF999999),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 17.dp, bottom = 15.dp)
                )

                DialogDoubleBtn(leftStr = "不保存", rightStr = "保存",
                    onClickLeft = { onDismiss() },
                    onClickRight = { onSave() })
            }
        })
}


@Composable
fun DeletePagePromptDialog(
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
) {
    Dialog(onDismissRequest = { onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .background(
                        brush = Brush.verticalGradient(
                            0.1f to Color(0xffFFECEC),
                            0.3f to Color.White
                        ), shape = RoundedCornerShape(20.dp)
                    )
                    .padding(20.dp, 24.dp),
            ) {
                Text(
                    text = "删除提示",
                    color = Color(0XFF252525),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Text(
                    text = "确认删除所选页面吗？删除后将无法撤销",
                    color = Color(0XFF999999),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 24.dp, bottom = 28.dp)
                )

                DialogDoubleBtn(leftStr = "取消", rightStr = "删除",
                    onClickLeft = { onDismiss() },
                    onClickRight = {
                        onDismiss()
                        onDelete()
                    })
            }
        })
}


@Composable
fun FileRenameDialog(
    onDismiss: () -> Unit,
    onComplete: (String) -> Unit,
) {
    var inputText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = { onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .background(
                        brush = Brush.verticalGradient(
                            0.1f to Color(0xffFFECEC),
                            0.3f to Color.White
                        ), shape = RoundedCornerShape(20.dp)
                    )
                    .padding(20.dp, 24.dp),
            ) {
                Text(
                    text = "重命名",
                    color = Color(0XFF252525),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                CusEditView(value = inputText,
                    onValueChange = {
                        if (it.length <= 20) {
                            inputText = it
                        }
                    },
                    modifier = Modifier
                        .padding(top = 20.dp, bottom = 28.dp)
                        .fillMaxWidth()
                        .height(47.dp)
                        .background(Color.White, RoundedCornerShape(10.dp))
                        .border(0.3.dp, Color(0xffD8DBDD), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp),
                    placeholder = {
                        Text(text = "请输入文件名称", color = Color(0xffBFBFBF), fontSize = 14.sp)
                    })


                DialogDoubleBtn(leftStr = "取消", rightStr = "确认",
                    onClickLeft = { onDismiss() },
                    onClickRight = {
                        if (inputText.isEmpty()) {
                            ToastUtils.showShort("请输入文件名称")
                        } else {
                            onComplete(inputText)
                            onDismiss()
                        }
                    })
            }
        })
}

@Composable
fun FileDeleteDialog(
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
) {
    Dialog(onDismissRequest = { onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .background(
                        brush = Brush.verticalGradient(
                            0.1f to Color(0xffFFECEC),
                            0.3f to Color.White
                        ), shape = RoundedCornerShape(20.dp)
                    )
                    .padding(20.dp, 24.dp),
            ) {
                Text(
                    text = "删除提示",
                    color = Color(0XFF252525),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Text(
                    text = "确定删除该文件吗？删除后将无法恢复",
                    color = Color(0XFF999999),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 24.dp, bottom = 28.dp)
                )

                DialogDoubleBtn(leftStr = "取消", rightStr = "确认",
                    onClickLeft = { onDismiss() },
                    onClickRight = { onDelete() })
            }
        })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatermarkTextStyleBox(
    sticker: TextSticker,
    onDismiss: () -> Unit,
    onComplete: (Boolean, Color, TextUnit) -> Unit,
    onUpdateData: (Boolean, Color, TextUnit) -> Unit,
    modifier: Modifier = Modifier,
) {
    var switch by remember { mutableStateOf(true) }
    var color by remember(sticker.color) { mutableStateOf(sticker.color) }
    var fontSize by remember(sticker.fontSize) { mutableStateOf(sticker.fontSize) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .border(
                0.5.dp,
                Color.Black.copy(0.09f),
                RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .noRippleClickable { }, horizontalAlignment = Alignment.CenterHorizontally
    ) {
//        Box(
//            modifier = Modifier
//                .padding(vertical = 8.dp)
//                .size(40.dp, 4.dp)
//                .background(Color(0xffD8D8D8), CircleShape)
//        )
        Row(
            modifier = Modifier
                .padding(top = 20.dp)
                .fillMaxWidth()
                .height(42.dp)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "水印",
                color = Color(0xff525252),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Image(
                painter = painterResource(if (switch) R.mipmap.module_pdf_edit_icon_k else R.mipmap.module_pdf_edit_icon_g),
                contentDescription = null,
                modifier = Modifier.noRippleClickable {
                    switch = !switch
                    onUpdateData(switch, color, fontSize)
                }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "颜色",
                color = Color(0xff525252),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            watermarkTextColors.forEach {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .noRippleClickable {
                            color = it
                            onUpdateData(switch, color, fontSize)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(color = it)
                    )
                    if (color == it) {
                        Image(
                            painter = painterResource(id = R.mipmap.module_pdf_edit_ys_xz),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "字号",
                color = Color(0xff525252),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )

            Slider(modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .height(16.dp),//有默认高度需要设置height
                value = fontSize.value,
                onValueChange = {
                    fontSize = it.toInt().sp
                    onUpdateData(switch, color, fontSize)
                },
                valueRange = 8f..36f,
                enabled = true,
                thumb = {
                    Spacer(
                        modifier = Modifier
                            .size(16.dp)
                            .background(Color(0xffF5341A), CircleShape)
                            .border(2.dp, Color.White, CircleShape)
                    )
                },
                track = {
                    SliderTrack(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        value = fontSize.value,
                        valueRange = 8f..36f,
                        inactiveTrackColor = Color(0xffCACACA),
                        activeTrackColor = Color(0xffF5341A)
                    )
                })
        }
        Spacer(modifier = Modifier.height(20.dp))

        HorizontalDivider(color = Color(0xffF2F3F5))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 26.dp)
        ) {
            Image(painter = painterResource(id = R.mipmap.module_pdf_edit_tjsy_icon_qx),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .noRippleClickable { onDismiss() })
            Text(
                text = "添加水印",
                color = Color(0xff252525),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.Center)
            )
            Image(painter = painterResource(id = R.mipmap.module_pdf_edit_tjsy_icon_wc),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .noRippleClickable {
                        onComplete(switch, color, fontSize)
                    })
        }
    }

}


@Composable
fun SliderTrack(
    modifier: Modifier,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    inactiveTrackColor: Color,
    activeTrackColor: Color,
) {
    Canvas(modifier = modifier) {
        val sliderStart = Offset(0f, center.y)
        val sliderEnd = Offset(size.width, center.y)
        val ratio = (value - valueRange.start) / (valueRange.endInclusive - valueRange.start)
        val sliderValueEnd = Offset(size.width * ratio, center.y)

        drawLine(
            inactiveTrackColor, sliderStart, sliderEnd, size.height, StrokeCap.Round
        )

        drawLine(
            activeTrackColor, sliderStart, sliderValueEnd, size.height, StrokeCap.Round
        )
    }
}

@Composable
internal fun LoadingDialog(
    content: String,
    modifier: Modifier = Modifier
        .width(150.dp)
        .background(Color.White, RoundedCornerShape(12.dp)),
    dimAmount: Float = 0.5f,
    properties: DialogProperties = DialogProperties(
        dismissOnBackPress = false,
        dismissOnClickOutside = false,
        usePlatformDefaultWidth = false
    ),
    onDismissRequest: () -> Unit = {},
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        val parent = LocalView.current.parent
        if (parent is DialogWindowProvider) {
            parent.window.setDimAmount(dimAmount)
        }
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(27.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(36.dp),
                color = Color(0XFF00A1FF),
                strokeWidth = 4.dp,
                trackColor = Color(0XFFEDEDED),
                strokeCap = StrokeCap.Round
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = content,
                color = Color(0XFF333333),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
