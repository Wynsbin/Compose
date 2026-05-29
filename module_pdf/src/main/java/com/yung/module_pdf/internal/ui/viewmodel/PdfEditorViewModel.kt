package com.yung.module_pdf.internal.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.view.MotionEvent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntSize
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.barteksc.pdfviewer.PDFView
import com.yung.module_pdf.R
import com.yung.module_pdf.internal.core.pdf.PdfDocumentManger
import com.yung.module_pdf.internal.core.pdf.PdfDrawManager
import com.yung.module_pdf.internal.core.pdf.StickerClickHandler
import com.yung.module_pdf.internal.data.RecentFileRepository
import com.yung.module_pdf.api.RecentFileFormat
import com.yung.module_pdf.internal.ui.component.EditMenuType
import com.yung.module_pdf.internal.ui.sticker.ImageSticker
import com.yung.module_pdf.internal.ui.sticker.Sticker
import com.yung.module_pdf.internal.ui.sticker.TextSticker
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container as orbitContainer
import java.io.File

data class PdfEditorState(
    val pdfView: PDFView? = null,
    val showLoadingDialog: Boolean = false,
    val showExitEditPromptDialog: Boolean = false,
    val clickMenuType: EditMenuType = EditMenuType.PAGE_MANAGEMENT,
    val curSticker: Sticker? = null,
    val revokeImage: Int = R.mipmap.module_pdf_edit_icon_xyb_nor,
)

sealed interface PdfEditorSideEffect {
    data class Toast(val message: String) : PdfEditorSideEffect
    data object Finish : PdfEditorSideEffect
    data class OpenPageManager(val file: File) : PdfEditorSideEffect
}

class PdfEditorViewModel : ViewModel(), ContainerHost<PdfEditorState, PdfEditorSideEffect> {

    override val container: Container<PdfEditorState, PdfEditorSideEffect> =
        orbitContainer(PdfEditorState())

    private val exitEditingModeTips = "请先退出编辑模式"
    private var pdfFile: File? = null
    private var stickerList = emptyList<Sticker>()
    private var stickerDelList = emptyList<Sticker>()
    private var stickerSaveList = emptyList<Sticker>()
    private val stickerClickHandler = StickerClickHandler()
    private var curPage = 0
    private var totalPageCount = 0
    private var stickersVisibleArea = IntSize.Zero
    private var firstVisibleArea = FirstVisibleArea()

    fun createPDFView(context: Context, file: File) {
        pdfFile = file
        stickerClickHandler.clearAll()
        stickerList = emptyList()
        stickerDelList = emptyList()
        stickerSaveList = emptyList()

        val pdfViewInstance = PDFView(context, null).apply {
            fromFile(file).enableSwipe(true).enableDoubletap(true).linkHandler(null)
                .onDrawAll { canvas, pageWidth, pageHeight, displayedPage ->
                    PdfDrawManager.drawStickerToPrePage(
                        context, stickerList, canvas, displayedPage, stickerClickHandler
                    )
                    PdfDrawManager.drawDividingLinesAndPageNumbers(
                        canvas, pageWidth, pageHeight, displayedPage
                    )
                }.onPageChange { page, pageCount ->
                    curPage = page
                    totalPageCount = pageCount
                }.onPageScroll { page, positionOffset ->
                    onPDFPageScroll(page, pageCount, getPageSize(page).height, positionOffset)
                }.onTap {
                    when (container.stateFlow.value.clickMenuType) {
                        EditMenuType.PAGE_MANAGEMENT -> clickToSelectTextSticker(it)
                        EditMenuType.INSERT_TEXT -> addTextStickerToPdf(it.x, it.y)
                        EditMenuType.INSERT_IMAGE -> {}
                    }
                    return@onTap true
                }.load()
        }

        viewModelScope.launch {
            intent {
                reduce {
                    state.copy(
                        pdfView = pdfViewInstance,
                        curSticker = null,
                    )
                }
            }
        }
    }

    private fun onPDFPageScroll(
        page: Int,
        pageCount: Int,
        pageHeight: Float,
        positionOffset: Float,
    ) {
        val containerHeight = stickersVisibleArea.height
        val totalPageHeight = pageCount * pageHeight
        val scrollableHeight = totalPageHeight - containerHeight
        val pixelOffset = positionOffset * scrollableHeight
        val firstVisiblePage = (pixelOffset / pageHeight).toInt().coerceIn(0, pageCount - 1)
        val firstVisibleOffset = pixelOffset % pageHeight
        val firstVisibleHeight = pageHeight - firstVisibleOffset
        firstVisibleArea = FirstVisibleArea(firstVisiblePage, firstVisibleOffset, firstVisibleHeight)
    }

    private fun clickToSelectTextSticker(event: MotionEvent) {
        if (event.action != MotionEvent.ACTION_DOWN) return

        val isNextPage = event.y > firstVisibleArea.visibleHeight
        val clickPage = firstVisibleArea.page + if (isNextPage) 1 else 0
        val yOffset =
            if (isNextPage) -firstVisibleArea.visibleHeight else firstVisibleArea.yOffset
        val clickPageYOffset = event.y + yOffset
        val id = stickerClickHandler.handleTap(clickPage, event.x, clickPageYOffset)

        if (id <= 0L) return

        var selectedSticker: Sticker? = null
        stickerList.firstOrNull { it.id == id }?.let { sticker ->
            val newOffset = Offset(x = sticker.offset.x, sticker.offset.y - yOffset)
            sticker.isSelected = true
            sticker.offset = newOffset
            selectedSticker = sticker
            stickerList = stickerList.filterNot { it.id == id }
        }
        stickerClickHandler.clearClickableRegion(id)
        container.stateFlow.value.pdfView?.invalidate()

        val sticker = selectedSticker ?: return
        viewModelScope.launch {
            intent { reduce { state.copy(curSticker = sticker) } }
        }
    }

    fun setStickersVisibleArea(intSize: IntSize) {
        stickersVisibleArea = intSize
    }

    fun switchInsertMode(value: EditMenuType, activity: FragmentActivity) {
        if (container.stateFlow.value.curSticker?.isSelected == true) {
            viewModelScope.launch {
                intent { postSideEffect(PdfEditorSideEffect.Toast(exitEditingModeTips)) }
            }
            return
        }
        viewModelScope.launch {
            intent { reduce { state.copy(clickMenuType = value) } }
        }
        if (value != EditMenuType.PAGE_MANAGEMENT) {
            container.stateFlow.value.pdfView?.resetZoomWithAnimation()
        }
        when (value) {
            EditMenuType.PAGE_MANAGEMENT -> {
                onSave(activity) { success, file ->
                    if (success) {
                        viewModelScope.launch {
                            intent { postSideEffect(PdfEditorSideEffect.OpenPageManager(file)) }
                        }
                    }
                }
            }

            EditMenuType.INSERT_TEXT -> {
                viewModelScope.launch {
                    intent {
                        postSideEffect(PdfEditorSideEffect.Toast("请点击选择要插入的位置"))
                    }
                }
            }

            EditMenuType.INSERT_IMAGE -> Unit
        }
    }

    fun prepareInsertImageMode(): Boolean {
        if (container.stateFlow.value.curSticker?.isSelected == true) {
            viewModelScope.launch {
                intent { postSideEffect(PdfEditorSideEffect.Toast(exitEditingModeTips)) }
            }
            return false
        }
        viewModelScope.launch {
            intent { reduce { state.copy(clickMenuType = EditMenuType.INSERT_IMAGE) } }
        }
        container.stateFlow.value.pdfView?.resetZoomWithAnimation()
        return true
    }

    fun addImageSticker(bitmap: Bitmap) {
        addImageStickerToPdf(bitmap)
    }

    private fun addTextStickerToPdf(x: Float, y: Float) {
        val textSticker = TextSticker(
            id = System.currentTimeMillis(),
            isSelected = true,
            curPage = curPage,
            offset = Offset(x, y),
            previewArea = stickersVisibleArea,
            text = TextFieldValue(text = ""),
        )
        viewModelScope.launch {
            intent { reduce { state.copy(curSticker = textSticker) } }
        }
    }

    private fun addImageStickerToPdf(bitmap: Bitmap) {
        val imageSticker = ImageSticker(
            id = System.currentTimeMillis(),
            isSelected = true,
            curPage = curPage,
            offset = Offset.Zero,
            previewArea = stickersVisibleArea,
            bitmap = bitmap,
        )
        viewModelScope.launch {
            intent { reduce { state.copy(curSticker = imageSticker) } }
        }
    }

    fun onUpdateTextSticker(sticker: TextSticker?) = intent {
        reduce { state.copy(curSticker = sticker) }
    }

    fun onUpdateImageSticker(sticker: ImageSticker?) = intent {
        reduce { state.copy(curSticker = sticker) }
    }

    fun onDeleteCurSticker() {
        val sticker = container.stateFlow.value.curSticker ?: return
        deleteSticker(sticker)
        stickerList = stickerList.filterNot { it.id == sticker.id }
        viewModelScope.launch {
            intent { reduce { state.copy(curSticker = null) } }
        }
        unSelectedStatusAndDrawToPreview()
    }

    fun unSelectedStatusAndDrawToPreview() {
        viewModelScope.launch {
            intent { reduce { state.copy(clickMenuType = EditMenuType.PAGE_MANAGEMENT) } }
        }
        drawStickers()
    }

    private fun drawStickers() {
        container.stateFlow.value.curSticker?.let { sticker ->
            sticker.isSelected = false
            val offset = sticker.offset
            val inNextPage = sticker.offset.y > firstVisibleArea.visibleHeight
            sticker.curPage = firstVisibleArea.page + if (inNextPage) 1 else 0
            val yOffset =
                if (inNextPage) -firstVisibleArea.visibleHeight else firstVisibleArea.yOffset
            sticker.offset = Offset(offset.x, offset.y + yOffset)
            val index = stickerList.indexOfFirst { it.id == sticker.id }
            if (index > 0) {
                stickerList = stickerList.toMutableList().apply { set(index, sticker) }
            } else {
                addSticker(sticker)
            }
        }
        container.stateFlow.value.pdfView?.invalidate()
        viewModelScope.launch {
            intent { reduce { state.copy(curSticker = null) } }
        }
    }

    fun onCopy() {
        val copySticker = container.stateFlow.value.curSticker
        unSelectedStatusAndDrawToPreview()
        (copySticker as? ImageSticker)?.let { original ->
            val newSticker = ImageSticker(
                id = System.currentTimeMillis(),
                isSelected = true,
                curPage = original.curPage,
                offset = Offset(x = original.offset.x + 50, y = original.offset.y + 50),
                previewArea = original.previewArea,
                rotation = original.rotation,
                scaleRatio = original.scaleRatio,
                bitmap = original.bitmap,
            )
            viewModelScope.launch {
                intent { reduce { state.copy(curSticker = newSticker) } }
            }
        }
    }

    fun onComplete(context: Context) {
        if (container.stateFlow.value.curSticker?.isSelected == true) {
            viewModelScope.launch {
                intent { postSideEffect(PdfEditorSideEffect.Toast(exitEditingModeTips)) }
            }
            return
        }
        if (stickerSaveList.containsAll(stickerList)) {
            onDialogNotSave(context)
        } else {
            if (stickerList.isNotEmpty()) {
                viewModelScope.launch {
                    intent { reduce { state.copy(showExitEditPromptDialog = true) } }
                }
            } else {
                onDialogNotSave(context)
            }
        }
    }

    fun onDialogNotSave(context: Context) = intent {
        reduce { state.copy(showExitEditPromptDialog = false) }
        postSideEffect(PdfEditorSideEffect.Finish)
    }

    fun onDialogSave(context: Context) {
        onSave(context) { _, _ ->
            viewModelScope.launch {
                intent {
                    reduce { state.copy(showExitEditPromptDialog = false) }
                    postSideEffect(PdfEditorSideEffect.Finish)
                }
            }
        }
    }

    fun onSave(context: Context, onCallback: ((Boolean, File) -> Unit)? = null) {
        if (container.stateFlow.value.curSticker?.isSelected == true) {
            viewModelScope.launch {
                intent { postSideEffect(PdfEditorSideEffect.Toast(exitEditingModeTips)) }
            }
            return
        }
        val file = pdfFile ?: return
        val originalPdfFile = File(file.path)
        if (stickerList.isEmpty()) {
            onCallback?.invoke(true, originalPdfFile)
            return
        }
        val outputPdfFile = PdfDocumentManger.createPdfFile()
        PdfDocumentManger.drawableStickerToPDDocument(
            context = context,
            originalPdfFile = originalPdfFile,
            outputPdfFile = outputPdfFile,
            stickers = stickerList,
        ).onStart {
            viewModelScope.launch {
                intent { reduce { state.copy(showLoadingDialog = true) } }
            }
        }.onCompletion {
            viewModelScope.launch {
                intent {
                    reduce { state.copy(showLoadingDialog = false) }
                    postSideEffect(PdfEditorSideEffect.Toast(PdfDocumentManger.SAVE_SUCCESS_TIPS))
                }
            }
            onCallback?.invoke(true, outputPdfFile)
            insertPDFFile(outputPdfFile.path)
            stickerSaveList = stickerList
        }.catch {
            viewModelScope.launch {
                intent {
                    reduce { state.copy(showLoadingDialog = false) }
                    postSideEffect(PdfEditorSideEffect.Toast("保存失败"))
                }
            }
            onCallback?.invoke(false, outputPdfFile)
            stickerSaveList = stickerList
        }.launchIn(viewModelScope)
    }

    private fun updateRevokeImage() {
        val resId = if (stickerDelList.isEmpty()) {
            R.mipmap.module_pdf_edit_icon_xyb_nor
        } else {
            R.mipmap.module_pdf_edit_icon_xyb_sel
        }
        viewModelScope.launch {
            intent { reduce { state.copy(revokeImage = resId) } }
        }
    }

    fun onRevoke() {
        if (container.stateFlow.value.curSticker?.isSelected == true) {
            viewModelScope.launch {
                intent { postSideEffect(PdfEditorSideEffect.Toast(exitEditingModeTips)) }
            }
            return
        }
        stickerList.lastOrNull()?.let {
            deleteSticker(it)
            unSelectedStatusAndDrawToPreview()
        }
    }

    fun onRecover() {
        if (container.stateFlow.value.curSticker?.isSelected == true) {
            viewModelScope.launch {
                intent { postSideEffect(PdfEditorSideEffect.Toast(exitEditingModeTips)) }
            }
            return
        }
        stickerDelList.lastOrNull()?.let {
            addSticker(it)
            unSelectedStatusAndDrawToPreview()
        }
    }

    private fun addSticker(sticker: Sticker) {
        stickerList = stickerList + sticker
        stickerDelList = stickerDelList - sticker
        updateRevokeImage()
    }

    private fun deleteSticker(sticker: Sticker) {
        stickerList = stickerList - sticker
        stickerDelList = stickerDelList + sticker
        updateRevokeImage()
    }

    fun insertPDFFile(path: String?) = viewModelScope.launch {
        RecentFileRepository.insertFile(path, RecentFileFormat.PDF)
    }
}
