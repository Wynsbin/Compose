package com.yung.module_pdf.internal.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.yung.module_pdf.R
import com.yung.module_pdf.internal.ui.sticker.TextSticker
import com.yung.module_pdf.internal.ui.sticker.defFontItem
import com.yung.module_pdf.internal.ui.sticker.defFontWeight
import com.yung.module_pdf.internal.ui.sticker.switchFontWeight
import com.yung.module_pdf.internal.ui.sticker.textStickerColors


@Composable
fun <T> BottomMenuBar(menus: List<T>, itemContent: @Composable (T) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(menus.size),
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .background(Color.White),
        verticalArrangement = Arrangement.Center
    ) {
        items(items = menus) { itemContent(it) }
    }
}

@Composable
fun BottomItemMenuBar(name: String, resId: Int, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Image(painter = painterResource(resId), contentDescription = null)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = name, color = Color(0xff525252), fontSize = 12.sp, fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun BottomStyleMenuBar(
    sticker: TextSticker,
    onClickKeyboard: () -> Unit,
    onClickTextStyle: () -> Unit,
    onSwitchFontItem: () -> Unit,
    onSwitchFontWeight: (FontWeight) -> Unit,
    onSelectedColor: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconSize = remember { 36.dp }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(88.dp)
            .background(Color.White),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.mipmap.module_pdf_edit_btn_jp),
            contentDescription = null,
            modifier = Modifier
                .size(iconSize)
                .noRippleClickable { onClickKeyboard() }
        )
        Image(
            painter = painterResource(id = R.mipmap.module_pdf_edit_btn_gs),
            contentDescription = null,
            modifier = Modifier
                .size(iconSize)
                .noRippleClickable { onClickTextStyle() }
        )

        Box(
            modifier = Modifier
                .size(1.dp, 12.dp)
                .background(Color(0xffC1C1C1))
        )

        Image(
            painter = painterResource(if (sticker.fontItem == defFontItem) R.mipmap.module_pdf_edit_btn_crwz else R.mipmap.module_pdf_edit_btn_crwz_sel),
            contentDescription = null,
            modifier = Modifier
                .size(iconSize)
                .noRippleClickable { onSwitchFontItem() }
        )

        Image(
            painter = painterResource(if (sticker.fontWeight == defFontWeight) R.mipmap.module_pdf_edit_btn_jc else R.mipmap.module_pdf_edit_btn_jc_sel),
            contentDescription = null,
            modifier = Modifier
                .size(iconSize)
                .noRippleClickable { onSwitchFontWeight(sticker.fontWeight.switchFontWeight()) }
        )

        Box(
            modifier = Modifier
                .size(iconSize)
                .background(Color(0xffF7F7F7), RoundedCornerShape(8.dp))
                .noRippleClickable { onSelectedColor() },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(sticker.color, CircleShape),
            )
        }
    }
}


@Preview
@Composable
fun TextColorSelectionPanel(
    modifier: Modifier = Modifier,
    selColor: Color = textStickerColors[1],
    clickColor: (Color) -> Unit = {},
) {
    ConstraintLayout(
        modifier = modifier
            .size(253.dp, 128.dp)
    ) {
        val (bgBox, colorBox) = createRefs()
        Image(
            painter = painterResource(id = R.mipmap.module_pdf_edit_bg_edit_colors),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxSize()
                .constrainAs(bgBox) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            modifier = Modifier
                .constrainAs(colorBox) {
                    start.linkTo(bgBox.start)
                    end.linkTo(bgBox.end)
                    top.linkTo(bgBox.top)
                    bottom.linkTo(bgBox.bottom)
                },
            contentPadding = PaddingValues(horizontal = 27.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            items(items = textStickerColors) {
                TextColorSelector(it, selColor == it, clickColor)
            }
        }
    }
}

@Composable
fun TextColorSelector(color: Color, isSelected: Boolean, clickColor: (Color) -> Unit) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .noRippleClickable { clickColor(color) },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .background(color, CircleShape)
        )
        if (isSelected) {
            Image(
                painter = painterResource(id = R.mipmap.module_pdf_edit_ys_xz),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
