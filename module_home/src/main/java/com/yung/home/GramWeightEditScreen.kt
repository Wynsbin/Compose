package com.yung.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

private val SheetBackground = Color(0xFFF0F9F4)
private val KeyBackground = Color(0xFFD8F0E0)
private val DoneGreen = Color(0xFF2BB673)
private val TitleColor = Color(0xFF1A1A1A)
private val ValueColor = Color(0xFF1A1A1A)
private val UnderlineColor = Color(0xFF2BB673)

@Composable
fun GramWeightEditScreen(
    viewModel: GramWeightEditViewModel,
    onClose: () -> Unit,
    onComplete: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.collectAsState()
    val context = LocalContext.current

    viewModel.collectSideEffect { effect ->
        when (effect) {
            GramWeightEditSideEffect.Close -> onClose()
            is GramWeightEditSideEffect.Complete -> onComplete(effect.gramsText)
            is GramWeightEditSideEffect.InvalidInput -> {
                Toast.makeText(context, effect.reason, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = SheetBackground,
    ) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(modifier = Modifier.width(40.dp))
                Text(
                    modifier = Modifier.weight(1f),
                    text = "编辑克重",
                    color = TitleColor,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { viewModel.onClose() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "关闭",
                        tint = TitleColor,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            val display = state.input.ifEmpty { "0" }
            val unitAlpha = if (state.input.isEmpty()) 0.45f else 1f
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = display,
                    color = ValueColor.copy(alpha = if (state.input.isEmpty()) 0.35f else 1f),
                    fontSize = 44.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    modifier = Modifier.padding(start = 6.dp, bottom = 6.dp),
                    text = "克",
                    color = ValueColor.copy(alpha = unitAlpha),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 6.dp)
                    .height(2.dp)
                    .background(UnderlineColor),
            )

            Spacer(modifier = Modifier.height(28.dp))

            val rowH = 56.dp
            val gap = 10.dp
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(rowH * 4 + gap * 3),
                horizontalArrangement = Arrangement.spacedBy(gap),
            ) {
                Column(
                    modifier = Modifier.weight(3f),
                    verticalArrangement = Arrangement.spacedBy(gap),
                ) {
                    KeypadDigitRow(rowH, "1", "2", "3") { viewModel.onDigitOrDot(it) }
                    KeypadDigitRow(rowH, "4", "5", "6") { viewModel.onDigitOrDot(it) }
                    KeypadDigitRow(rowH, "7", "8", "9") { viewModel.onDigitOrDot(it) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(rowH),
                        horizontalArrangement = Arrangement.spacedBy(gap),
                    ) {
                        KeypadIconCell(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            onClick = { viewModel.onClear() },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "清空",
                                tint = TitleColor,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        KeypadTextCell(
                            text = "0",
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            onClick = { viewModel.onDigitOrDot('0') },
                        )
                        KeypadTextCell(
                            text = ".",
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            onClick = { viewModel.onDigitOrDot('.') },
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(gap),
                ) {
                    KeypadIconCell(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(rowH),
                        onClick = { viewModel.onBackspace() },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = "退格",
                            tint = TitleColor,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(DoneGreen)
                            .clickable { viewModel.onComplete() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "完成",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadDigitRow(
    rowHeight: androidx.compose.ui.unit.Dp,
    a: String,
    b: String,
    c: String,
    onKey: (Char) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(rowHeight),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        KeypadTextCell(
            text = a,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            onClick = { onKey(a[0]) },
        )
        KeypadTextCell(
            text = b,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            onClick = { onKey(b[0]) },
        )
        KeypadTextCell(
            text = c,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            onClick = { onKey(c[0]) },
        )
    }
}

@Composable
private fun KeypadTextCell(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(KeyBackground)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = TitleColor,
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun KeypadIconCell(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(KeyBackground)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

