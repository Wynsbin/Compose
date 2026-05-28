package com.yung.module_pdf.model

import android.content.Context
import android.view.MotionEvent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.ToastUtils
import com.yung.module_pdf.db.FileInfoEntity
import com.yung.module_pdf.db.FileInfoFormat
import com.yung.module_pdf.internal.data.RecentFileRepository
import com.yung.module_pdf.activity.PdfEditorActivity
import com.yung.module_pdf.common.TextSticker
import com.yung.module_pdf.utils.FileManager
import com.yung.module_pdf.utils.PdfDocumentManger
import com.yung.module_pdf.utils.PdfDrawManager
import com.yung.module_pdf.utils.PdfLoadManager
import com.yung.module_pdf.utils.StickerClickHandler
import com.github.barteksc.pdfviewer.PDFView
import com.shockwave.pdfium.PdfDocument.Bookmark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class PdfPreviewModel : ViewModel() {

    val pdfLocalList = MutableStateFlow(emptyList<FileInfoEntity>())
    val pdfRecentList = MutableStateFlow(emptyList<FileInfoEntity>())
    val searchKeyword = MutableStateFlow("")
    val bookmarks = MutableStateFlow(emptyList<Bookmark>())
    val pdfView = MutableStateFlow<PDFView?>(null)
    var showLoadingDialog = MutableStateFlow(false)
    var showBookmarkBottomSheet = MutableStateFlow(false)

    private val textStickerList = MutableStateFlow(emptyList<TextSticker>())
    val curTextStickerNew = MutableStateFlow<TextSticker?>(null)
    private val curTextStickerOrig = MutableStateFlow<TextSticker?>(null)//用户恢复样式使用
    private var showTextStyleBox = MutableStateFlow(false)

    private val curPage = MutableStateFlow(0)//当前所在页，意义不大。关键还是firstVisibleArea来测量为准
    private val totalPageCount = MutableStateFlow(0)//页面数量
    private val stickersVisibleArea = MutableStateFlow(IntSize.Zero)//PDFView和贴纸操作可视化区域
    private val firstVisibleArea = MutableStateFlow(FirstVisibleArea())//第一个可见页
    private val pdfInfoEntity = MutableStateFlow<FileInfoEntity?>(null)

    private val stickerClickHandler = StickerClickHandler()

    fun loadLocalPdfList() = viewModelScope.launch(Dispatchers.IO) {
        PdfLoadManager.scanAllPdf().collectLatest {
            pdfLocalList.value = it
        }
    }

    fun loadRecentPdfList() = viewModelScope.launch(Dispatchers.IO) {
        RecentFileRepository.observeByFormat(FileInfoFormat.PDF)
            .collectLatest {
                pdfRecentList.value = it
            }
    }


    fun setSearchKeyWord(value: String) {
        searchKeyword.value = value
    }

    fun searchPdfList() = viewModelScope.launch {
        if (searchKeyword.value.isEmpty()) {
            pdfLocalList.value = emptyList()
            return@launch
        }
        PdfLoadManager.scanAllPdf().collect {
            pdfLocalList.value = it.filter { it.name.lowercase().contains(searchKeyword.value) }
        }
    }

    fun createPDFView(context: Context, entity: FileInfoEntity?) {
        if (entity == null) return
        pdfInfoEntity.value = entity
        pdfView.value = PDFView(context, null).apply {
            fromFile(File(entity.path)).enableSwipe(true).enableDoubletap(true).linkHandler(null)
                .onLoad { loadBookmarks(this) }
                .onDrawAll { canvas, pageWidth, pageHeight, displayedPage ->
                    // 计算水印在Box中的相对位置
                    PdfDrawManager.drawStickerToPrePage(
                        context, textStickerList.value, canvas, displayedPage, stickerClickHandler
                    )
                    PdfDrawManager.drawDividingLinesAndPageNumbers(
                        canvas, pageWidth, pageHeight, displayedPage
                    )
                }.onPageChange { page, pageCount ->
                    curPage.value = page
                    totalPageCount.value = pageCount
                }.onPageScroll { page, positionOffset ->
                    onPDFPageScroll(page, pageCount, getPageSize(page).height, positionOffset)
                }.onTap {
                    clickToSelectTextSticker(it)
                    return@onTap true
                }.load()
        }
    }

    // 加载完成后解析书签
    private fun loadBookmarks(pdfView: PDFView) {
        val contents = pdfView.tableOfContents // 获取目录数据
        bookmarks.value = contents
    }

    private fun onPDFPageScroll(
        page: Int,
        pageCount: Int,
        pageHeight: Float,
        positionOffset: Float,
    ) {
        val containerHeight = stickersVisibleArea.value.height//PDFView和水印可视化区域
        val totalPageHeight = pageCount * pageHeight//所有页面总高度
        val scrollableHeight = totalPageHeight - containerHeight//可滚动总高度
        val pixelOffset = positionOffset * scrollableHeight
        val firstVisiblePage =
            (pixelOffset / pageHeight).toInt().coerceIn(0, pageCount - 1)//可见页码
        val firstVisibleOffset = pixelOffset % pageHeight //可见偏移量
        val firstVisibleHeight = pageHeight - firstVisibleOffset//可见高度
        firstVisibleArea.value =
            FirstVisibleArea(firstVisiblePage, firstVisibleOffset, firstVisibleHeight)
    }

    //点击水印，将水印置顶到图层上
    private fun clickToSelectTextSticker(event: MotionEvent) {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val isNextPage = event.y > firstVisibleArea.value.visibleHeight
            val clickPage = firstVisibleArea.value.page + if (isNextPage) 1 else 0
            val yOffset =
                if (isNextPage) -firstVisibleArea.value.visibleHeight else firstVisibleArea.value.yOffset
            val clickPageYOffset = event.y + yOffset
            val id = stickerClickHandler.handleTap(clickPage, event.x, clickPageYOffset)

            if (id > 0L) {
                var state = textStickerList.value.first { it.id == id }
                val newOffset = Offset(x = state.offset.x, state.offset.y - yOffset)
                state = state.copy(isSelected = true, offset = newOffset)

                curTextStickerNew.value = state
                curTextStickerOrig.value = state
                removeTextStickerFromList(id)
                pdfView.value?.invalidate()
            }
        }
    }

    fun switchBookmarkBottomSheet(value: Boolean) {
        showBookmarkBottomSheet.value = value
    }

    fun setStickersVisibleArea(intSize: IntSize) {
        stickersVisibleArea.value = intSize
    }

    private fun clearAllSelectedBox() {
        textStickerList.update { it.map { it.copy(isSelected = false) } }
    }

    fun onUpdateTextSticker(textSticker: TextSticker?) {
        curTextStickerNew.value = textSticker
    }

    //添加水印盒子
    fun addWatermarkBox() {
        pdfView.value?.apply {
            resetZoomWithAnimation()
        }
        clearAllSelectedBox()
        showTextStyleBox.value = true
        val textSticker = TextSticker(
            id = System.currentTimeMillis(),
            offset = Offset.Zero,
            previewArea = stickersVisibleArea.value,
            text = TextFieldValue(text = "添加水印"),
            isSelected = true,
            curPage = curPage.value,
        )
        curTextStickerOrig.value = textSticker
        curTextStickerNew.value = textSticker
    }

    private fun removeTextStickerFromList(id: Long) {
        textStickerList.update { it.filterNot { it.id == id } }
        stickerClickHandler.clearClickableRegion(id)
    }

    fun closeUpdatedTextStickerStyle() {
        curTextStickerOrig.value?.let { orig ->
            submitUpdatedTextStickerStyle(orig.switch, orig.color, orig.fontSize)
        }
    }

    fun submitUpdatedTextStickerStyle(switch: Boolean, color: Color, fontSize: TextUnit) {
        curTextStickerNew.update {
            it?.copy(isSelected = false, switch = switch, color = color, fontSize = fontSize)
        }
        drawTextSticker()
    }

    //将水印绘制到PDFView上
    private fun drawTextSticker() {
        curTextStickerNew.value?.let { sticker ->
            //调整水印绘制位置
            val offset = sticker.offset
            //是否相对first的下一页
            val inNextPage = sticker.offset.y > firstVisibleArea.value.visibleHeight
            sticker.curPage = firstVisibleArea.value.page + if (inNextPage) 1 else 0
            val yOffset =
                if (inNextPage) -firstVisibleArea.value.visibleHeight else firstVisibleArea.value.yOffset
            sticker.offset = Offset(offset.x, offset.y + yOffset)

            val index = textStickerList.value.indexOfFirst { it.id == sticker.id }
            if (index > 0) {
                if (sticker.switch) {
                    textStickerList.update { currentList ->
                        currentList.toMutableList().apply {
                            set(index, sticker) // 替换指定位置元素
                        }
                    }
                } else {
                    removeTextStickerFromList(sticker.id)
                }
            } else {
                if (sticker.switch) {
                    // 添加到列表（保持不可变性）
                    textStickerList.update { currentList ->
                        currentList + sticker
                    }
                }
            }
        }
        pdfView.value?.invalidate()
        curTextStickerNew.value = null
        curTextStickerOrig.value = null
    }

    //保存
    private fun onSaveFile(context: Context, onCallback: ((File) -> Unit)? = null) {
        pdfInfoEntity.value?.let {
            val originalPdfFile = File(it.path)
            if (textStickerList.value.isEmpty()) {
                onCallback?.invoke(originalPdfFile)
            } else {
                val outputPdfFile = PdfDocumentManger.createPdfFile()
                PdfDocumentManger.drawableStickerToPDDocument(
                    context = context,
                    originalPdfFile = originalPdfFile,
                    outputPdfFile = outputPdfFile,
                    stickers = textStickerList.value
                ).onStart {
                    showLoadingDialog.value = true
                }.onCompletion {
                    showLoadingDialog.value = false
                    onCallback?.invoke(outputPdfFile)
                    insertPDFFile(outputPdfFile.path)
                }.catch {
                    ToastUtils.showShort("保存失败")
                }.launchIn(viewModelScope)
            }
        }
    }

    //分享文件
    fun sharePdfFile(context: Context) = viewModelScope.launch {
        onSaveFile(context) {
            FileManager.shareFile(context, it)
        }
    }

    //进入编辑
    fun enterEditorPage(context: Context) = viewModelScope.launch {
        onSaveFile(context) {
            PdfEditorActivity.start(context, it)
        }
    }

    fun insertPDFFile(path: String?) = viewModelScope.launch {
        RecentFileRepository.insertFile(path, FileInfoFormat.PDF)
    }
}

data class FirstVisibleArea(
    val page: Int = 0,//页码
    val yOffset: Float = 0f,//偏移量
    val visibleHeight: Float = 0f,//可见高度
)