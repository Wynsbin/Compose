package com.yung.module_pdf.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yung.module_pdf.db.FileInfoEntity
import com.yung.module_pdf.R
import com.shockwave.pdfium.PdfDocument.Bookmark


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CusDragHandle() {
    BottomSheetDefaults.DragHandle(
        width = 40.dp,
        height = 4.dp,
        color = Color(0xffD8D8D8),
        shape = RoundedCornerShape(2.dp)
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkBottomSheet(
    bookmarks: List<Bookmark>,
    onDismiss: () -> Unit = {},
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val configuration = LocalConfiguration.current

    ModalBottomSheet(
        modifier = Modifier,
        onDismissRequest = { onDismiss() },
        sheetState = sheetState,
        dragHandle = { CusDragHandle() },
        sheetMaxWidth = configuration.screenWidthDp.dp,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(start = 13.dp, end = 13.dp, bottom = 13.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items = bookmarks) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xffF9F9F9), RoundedCornerShape(6.dp))
                        .border(1.dp, Color(0XFFF3F3F3), RoundedCornerShape(6.dp))
                ) {
                    BookmarkItem(it)
                }
            }
        }
    }
}

@Composable
fun BookmarkItem(bookmark: Bookmark, level: Int = 0) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.padding(start = level.dp * 16)) {
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }) {
            if (bookmark.hasChildren()) {
                Image(
                    painter = painterResource(id = R.mipmap.module_pdf_edit_icon_ml),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .rotate(if (expanded) 90f else 0f)
                )
            }
            Text(
                text = bookmark.title,
                color = Color(0xff525252),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(
                    start = (level + 1).dp * 16, end = 16.dp, top = 16.dp, bottom = 16.dp
                )
            )
        }
        if (bookmark.hasChildren() && expanded) {
            bookmark.children.forEach { child ->
                BookmarkItem(bookmark = child, level = level + 1)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontSelectBottomSheet(
    sticker: TextSticker,
    onDismiss: () -> Unit = {},
    onUpdateStyle: (FontItem) -> Unit,
) {

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val configuration = LocalConfiguration.current

    var fontItem by remember(sticker.fontItem) { mutableStateOf(sticker.fontItem) }

    ModalBottomSheet(
        modifier = Modifier,
        onDismissRequest = { onDismiss() },
        sheetState = sheetState,
        dragHandle = { CusDragHandle() },
        sheetMaxWidth = configuration.screenWidthDp.dp,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(stickerFontList) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(
                            if (fontItem == it) Color(0xffFFE8E8) else Color(0xffF9F9F9),
                            RoundedCornerShape(6.dp)
                        )
                        .border(
                            1.dp,
                            if (fontItem == it) Color(0xffFF9B8E) else Color(0xffF3F3F3),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(16.dp, 17.dp)
                        .noRippleClickable {
                            fontItem = it
                            onUpdateStyle(fontItem)
                        }
                ) {
                    Text(
                        text = it.fontName,
                        color = if (fontItem == it) Color(0xffF5341A) else Color(0xff252525),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextStyleBottomSheet(
    sticker: TextSticker,
    onDismiss: () -> Unit = {},
    onSelectFont: () -> Unit = {},
    onUpdateStyle: (FontItem, TextUnit, FontWeight, Color) -> Unit,
) {

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val configuration = LocalConfiguration.current

    var fontItem by remember(sticker.fontItem) { mutableStateOf(sticker.fontItem) }
    var fontSize by remember(sticker.fontSize) { mutableStateOf(sticker.fontSize) }
    var fontWeight by remember(sticker.fontWeight) { mutableStateOf(sticker.fontWeight) }
    var color by remember(sticker.color) { mutableStateOf(sticker.color) }

    val defBgModifier = remember {
        Modifier
            .fillMaxWidth()
            .background(Color(0xffF9F9F9), RoundedCornerShape(6.dp))
            .border(1.dp, Color(0xffF3F3F3), RoundedCornerShape(6.dp))
    }

    ModalBottomSheet(
        modifier = Modifier,
        onDismissRequest = { onDismiss() },
        sheetState = sheetState,
        dragHandle = { CusDragHandle() },
        sheetMaxWidth = configuration.screenWidthDp.dp,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(defBgModifier.padding(16.dp, 17.dp)) {
                Text(
                    text = "字体：",
                    color = Color(0xff525252),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.CenterStart)
                )

                Text(
                    text = fontItem.fontName,
                    color = Color(0xff252525),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .noRippleClickable { onSelectFont() }
                )
            }

            Column(defBgModifier) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp, 17.dp)
                ) {
                    Text(
                        text = "字号：",
                        color = Color(0xff525252),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )

                    Text(
                        text = "${fontSize.value.toInt()}",
                        color = Color(0xff252525),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }
                HorizontalDivider(color = Color(0xffF2F3F5))
                Slider(modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),//有默认高度需要设置height
                    value = fontSize.value,
                    onValueChange = {
                        fontSize = it.toInt().sp
                        onUpdateStyle(fontItem, fontSize, fontWeight, color)
                    },
                    valueRange = 8f..96f,
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
                            valueRange = 8f..96f,
                            inactiveTrackColor = Color(0xffCACACA),
                            activeTrackColor = Color(0xffF5341A)
                        )
                    })
            }

            Box(defBgModifier.padding(16.dp, 17.dp)) {
                Text(
                    text = "样式：",
                    color = Color(0xff525252),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.CenterStart)
                )

                Text(
                    text = "B",
                    color = if (fontWeight == defFontWeight) Color(0xff383838)
                    else Color(0xffF5341A),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .noRippleClickable {
                            fontWeight = fontWeight.switchFontWeight()
                            onUpdateStyle(fontItem, fontSize, fontWeight, color)
                        }
                )
            }


            Column(defBgModifier) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp, 17.dp)
                ) {
                    Text(
                        text = "字体颜色：",
                        color = Color(0xff525252),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                }
                HorizontalDivider(color = Color(0xffF2F3F5))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(items = textStickerColors) {
                        TextColorSelector(it, color == it) {
                            color = it
                            onUpdateStyle(fontItem, fontSize, fontWeight, color)
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileMoreBottomSheet(
    entity: FileInfoEntity?,
    onDismiss: () -> Unit,
    onMoreEvent: (FileMenuType, FileInfoEntity?) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val configuration = LocalConfiguration.current

    ModalBottomSheet(
        modifier = Modifier,
        onDismissRequest = { onDismiss() },
        sheetState = sheetState,
        dragHandle = { },
        sheetMaxWidth = configuration.screenWidthDp.dp,
        containerColor = Color(0XFFFFF2F2),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column {
            Text(
                text = "文件：${entity?.name}",
                color = Color(0xffF5341A),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp, 17.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            ) {
                fileMenus.forEachIndexed { index, type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .noRippleClickable { onMoreEvent(type, entity) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Image(
                            painter = painterResource(type.resId),
                            contentDescription = null
                        )
                        Text(
                            text = type.type,
                            color = Color(0xff666666),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (index != fileMenus.lastIndex) {
                        HorizontalDivider(
                            color = Color(0xffF2F5F8),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                        )
                    }
                }
            }
        }
    }
}