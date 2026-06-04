package com.yung.module_pdf.internal.ui.viewmodel

import android.content.Context
import android.view.MotionEvent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.ScreenUtils
import com.github.barteksc.pdfviewer.PDFView
import com.shockwave.pdfium.PdfDocument.Bookmark
import com.yung.module_pdf.internal.core.pdf.PdfDocumentManger
import com.yung.module_pdf.internal.core.pdf.PdfDrawManager
import com.yung.module_pdf.internal.core.pdf.PdfLoadManager
import com.yung.module_pdf.internal.core.pdf.StickerClickHandler
import com.yung.module_pdf.internal.data.RecentFileRepository
import com.yung.module_pdf.internal.db.FileInfoEntity
import com.yung.module_pdf.api.RecentFileFormat
import com.yung.module_pdf.internal.ui.sticker.TextSticker
import com.yung.module_pdf.internal.ui.sticker.watermarkTextColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container as orbitContainer
import java.io.File

data class PdfPreviewState(
    val pdfLocalList: List<FileInfoEntity> = emptyList(),
    val pdfRecentList: List<FileInfoEntity> = emptyList(),
    val searchKeyword: String = "",
    val bookmarks: List<Bookmark> = emptyList(),
    val pdfView: PDFView? = null,
    val showLoadingDialog: Boolean = false,
    val showBookmarkBottomSheet: Boolean = false,
    val curTextStickerNew: TextSticker? = null,
)

sealed interface PdfPreviewSideEffect {
    data class Toast(val message: String) : PdfPreviewSideEffect
    data class OpenEditor(val file: File) : PdfPreviewSideEffect
    data class ShareFile(val file: File) : PdfPreviewSideEffect
}

class PdfPreviewViewModel : ViewModel(), ContainerHost<PdfPreviewState, PdfPreviewSideEffect> {

    override val container: Container<PdfPreviewState, PdfPreviewSideEffect> =
        orbitContainer(PdfPreviewState())

    private val textStickerList = MutableStateFlow(emptyList<TextSticker>())
    private val curTextStickerOrig = MutableStateFlow<TextSticker?>(null)
    private val showTextStyleBox = MutableStateFlow(false)

    private val curPage = MutableStateFlow(0)
    private val totalPageCount = MutableStateFlow(0)
    private val stickersVisibleArea = MutableStateFlow(IntSize.Zero)
    private val firstVisibleArea = MutableStateFlow(FirstVisibleArea())
    private val pdfInfoEntity = MutableStateFlow<FileInfoEntity?>(null)

    private val stickerClickHandler = StickerClickHandler()

    fun loadLocalPdfList() = viewModelScope.launch(Dispatchers.IO) {
        PdfLoadManager.scanAllPdf().collectLatest { list ->
            intent { reduce { state.copy(pdfLocalList = list) } }
        }
    }

    fun loadRecentPdfList() = viewModelScope.launch(Dispatchers.IO) {
        RecentFileRepository.observeByFormat(RecentFileFormat.PDF).collectLatest { list ->
            intent { reduce { state.copy(pdfRecentList = list) } }
        }
    }

    fun setSearchKeyWord(value: String) = intent {
        reduce { state.copy(searchKeyword = value) }
    }

    fun searchPdfList() = viewModelScope.launch {
        val keyword = container.stateFlow.value.searchKeyword
        if (keyword.isEmpty()) {
            intent { reduce { state.copy(pdfLocalList = emptyList()) } }
            return@launch
        }
        PdfLoadManager.scanAllPdf().collect { list ->
            intent {
                reduce {
                    state.copy(
                        pdfLocalList = list.filter {
                            it.name.lowercase().contains(keyword)
                        },
                    )
                }
            }
        }
    }

    fun createPDFView(context: Context, entity: FileInfoEntity?) {
        if (entity == null) return
        pdfInfoEntity.value = entity
        val view = PDFView(context, null).apply {
            fromFile(File(entity.path)).enableSwipe(true).enableDoubletap(true).linkHandler(null)
                .onLoad { loadBookmarks(this) }
                .onDrawAll { canvas, pageWidth, pageHeight, displayedPage ->
                    PdfDrawManager.drawStickerToPrePage(
                        context, textStickerList.value, canvas, displayedPage, stickerClickHandler,
                    )
                    PdfDrawManager.drawDividingLinesAndPageNumbers(
                        canvas, pageWidth, pageHeight, displayedPage,
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
        viewModelScope.launch {
            intent { reduce { state.copy(pdfView = view) } }
        }
    }

    private fun loadBookmarks(pdfView: PDFView) {
        val contents = pdfView.tableOfContents
        viewModelScope.launch {
            intent { reduce { state.copy(bookmarks = contents) } }
        }
    }

    private fun onPDFPageScroll(
        page: Int,
        pageCount: Int,
        pageHeight: Float,
        positionOffset: Float,
    ) {
        val containerHeight = stickersVisibleArea.value.height
        val totalPageHeight = pageCount * pageHeight
        val scrollableHeight = totalPageHeight - containerHeight
        val pixelOffset = positionOffset * scrollableHeight
        val firstVisiblePage =
            (pixelOffset / pageHeight).toInt().coerceIn(0, pageCount - 1)
        val firstVisibleOffset = pixelOffset % pageHeight
        val firstVisibleHeight = pageHeight - firstVisibleOffset
        firstVisibleArea.value =
            FirstVisibleArea(firstVisiblePage, firstVisibleOffset, firstVisibleHeight)
    }

    private fun clickToSelectTextSticker(event: MotionEvent) {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val isNextPage = event.y > firstVisibleArea.value.visibleHeight
            val clickPage = firstVisibleArea.value.page + if (isNextPage) 1 else 0
            val yOffset =
                if (isNextPage) -firstVisibleArea.value.visibleHeight else firstVisibleArea.value.yOffset
            val clickPageYOffset = event.y + yOffset
            val id = stickerClickHandler.handleTap(clickPage, event.x, clickPageYOffset)

            if (id > 0L) {
                var sticker = textStickerList.value.first { it.id == id }
                val newOffset = Offset(x = sticker.offset.x, sticker.offset.y - yOffset)
                sticker = sticker.copy(isSelected = true, offset = newOffset)

                viewModelScope.launch {
                    intent { reduce { state.copy(curTextStickerNew = sticker) } }
                }
                curTextStickerOrig.value = sticker
                removeTextStickerFromList(id)
                container.stateFlow.value.pdfView?.invalidate()
            }
        }
    }

    fun switchBookmarkBottomSheet(value: Boolean) = intent {
        reduce { state.copy(showBookmarkBottomSheet = value) }
    }

    fun setStickersVisibleArea(intSize: IntSize) {
        stickersVisibleArea.value = intSize
    }

    private fun clearAllSelectedBox() {
        textStickerList.update { it.map { sticker -> sticker.copy(isSelected = false) } }
    }

    fun onUpdateTextSticker(textSticker: TextSticker?) = intent {
        reduce { state.copy(curTextStickerNew = textSticker) }
    }

    fun updateWatermarkOffset(offset: Offset) = intent {
        reduce {
            state.copy(curTextStickerNew = state.curTextStickerNew?.copy(offset = offset))
        }
    }

    fun updateWatermarkRotationAndScale(rotation: Float, scaleRatio: Float) = intent {
        reduce {
            state.copy(
                curTextStickerNew = state.curTextStickerNew?.copy(
                    rotation = rotation,
                    scaleRatio = scaleRatio,
                ),
            )
        }
    }

    fun updateWatermarkText(text: TextFieldValue) = intent {
        reduce {
            state.copy(curTextStickerNew = state.curTextStickerNew?.copy(text = text))
        }
    }

    fun updateWatermarkLineBreaks(textLineBreaks: List<IntRange>) = intent {
        reduce {
            state.copy(
                curTextStickerNew = state.curTextStickerNew?.copy(textLineBreaks = textLineBreaks),
            )
        }
    }

    fun updateWatermarkStyle(switch: Boolean, color: Color, fontSize: TextUnit) = intent {
        reduce {
            state.copy(
                curTextStickerNew = state.curTextStickerNew?.copy(
                    switch = switch,
                    color = color,
                    fontSize = fontSize,
                ),
            )
        }
    }

    fun addWatermarkBox() {
        container.stateFlow.value.pdfView?.resetZoomWithAnimation()
        clearAllSelectedBox()
        showTextStyleBox.value = true
        val textSticker = TextSticker(
            id = System.currentTimeMillis(),
            offset = Offset.Zero,
            previewArea = stickersVisibleArea.value,
            text = TextFieldValue(text = ""),
            color = watermarkTextColors[0],
            isSelected = true,
            curPage = curPage.value,
            withBackground = true,
        )
        curTextStickerOrig.value = textSticker
        viewModelScope.launch {
            intent { reduce { state.copy(curTextStickerNew = textSticker) } }
        }
    }

    private fun removeTextStickerFromList(id: Long) {
        textStickerList.update { it.filterNot { sticker -> sticker.id == id } }
        stickerClickHandler.clearClickableRegion(id)
    }

    fun closeUpdatedTextStickerStyle() = intent {
        curTextStickerOrig.value = null
        reduce { state.copy(curTextStickerNew = null) }
    }

    fun submitUpdatedTextStickerStyle(switch: Boolean, color: Color, fontSize: TextUnit) = intent {
        val current = state.curTextStickerNew ?: return@intent
        if (switch && current.text.text.isBlank()) {
            postSideEffect(PdfPreviewSideEffect.Toast("请输入水印内容"))
            return@intent
        }
        val updated = current.copy(isSelected = false, switch = switch, color = color, fontSize = fontSize)
        reduce { state.copy(curTextStickerNew = updated) }
        drawTextSticker(updated)
    }

    private fun drawTextSticker(stickerInput: TextSticker? = null) {
        val sticker = stickerInput ?: container.stateFlow.value.curTextStickerNew ?: return
        val offset = sticker.offset
        val inNextPage = sticker.offset.y > firstVisibleArea.value.visibleHeight
        sticker.curPage = firstVisibleArea.value.page + if (inNextPage) 1 else 0
        val yOffset =
            if (inNextPage) -firstVisibleArea.value.visibleHeight else firstVisibleArea.value.yOffset
        sticker.offset = Offset(offset.x, offset.y + yOffset)

        val index = textStickerList.value.indexOfFirst { it.id == sticker.id }
        if (index >= 0) {
            if (sticker.switch) {
                textStickerList.update { currentList ->
                    currentList.toMutableList().apply {
                        set(index, sticker)
                    }
                }
            } else {
                removeTextStickerFromList(sticker.id)
            }
        } else {
            if (sticker.switch) {
                textStickerList.update { currentList -> currentList + sticker }
            }
        }
        container.stateFlow.value.pdfView?.invalidate()
        curTextStickerOrig.value = null
        viewModelScope.launch {
            intent { reduce { state.copy(curTextStickerNew = null) } }
        }
    }

    private fun onSaveFile(context: Context, onCallback: ((File) -> Unit)? = null) {
        pdfInfoEntity.value?.let { entity ->
            val originalPdfFile = File(entity.path)
            if (textStickerList.value.isEmpty()) {
                onCallback?.invoke(originalPdfFile)
            } else {
                val outputPdfFile = PdfDocumentManger.createPdfFile()
                PdfDocumentManger.drawableStickerToPDDocument(
                    context = context,
                    originalPdfFile = originalPdfFile,
                    outputPdfFile = outputPdfFile,
                    stickers = textStickerList.value,
                    canvasWidth = stickersVisibleArea.value.width
                        .takeIf { it > 0 } ?: ScreenUtils.getScreenWidth(),
                ).onStart {
                    viewModelScope.launch {
                        intent { reduce { state.copy(showLoadingDialog = true) } }
                    }
                }.onCompletion {
                    viewModelScope.launch {
                        intent { reduce { state.copy(showLoadingDialog = false) } }
                    }
                    onCallback?.invoke(outputPdfFile)
                    insertPDFFile(outputPdfFile.path)
                }.catch {
                    viewModelScope.launch {
                        intent { postSideEffect(PdfPreviewSideEffect.Toast("保存失败")) }
                    }
                }.launchIn(viewModelScope)
            }
        }
    }

    fun sharePdfFile(context: Context) = viewModelScope.launch {
        onSaveFile(context) { file ->
            intent { postSideEffect(PdfPreviewSideEffect.ShareFile(file)) }
        }
    }

    fun enterEditorPage(context: Context) = viewModelScope.launch {
        onSaveFile(context) { file ->
            intent { postSideEffect(PdfPreviewSideEffect.OpenEditor(file)) }
        }
    }

    fun insertPDFFile(path: String?) = viewModelScope.launch {
        RecentFileRepository.insertFile(path, RecentFileFormat.PDF)
    }
}
