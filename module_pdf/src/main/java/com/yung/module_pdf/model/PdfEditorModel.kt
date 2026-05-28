package com.yung.module_pdf.model

import android.content.Context
import android.graphics.Bitmap
import android.view.MotionEvent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntSize
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.ToastUtils
import com.yung.module_pdf.db.FileInfoFormat
import com.yung.module_pdf.internal.data.RecentFileRepository
import com.yung.module_pdf.db.insertFile
import com.yung.module_pdf.R
import com.yung.module_pdf.activity.PdfManagerActivity
import com.yung.module_pdf.common.EditMenuType
import com.yung.module_pdf.common.ImageSticker
import com.yung.module_pdf.common.Sticker
import com.yung.module_pdf.common.TextSticker
import com.yung.module_pdf.utils.PdfDocumentManger
import com.yung.module_pdf.utils.PdfDrawManager
import com.yung.module_pdf.utils.StickerClickHandler
import com.github.barteksc.pdfviewer.PDFView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class PdfEditorModel : ViewModel() {

    private val exitEditingModeTips = "请先退出编辑模式"
    private var pdfFile = MutableStateFlow<File?>(null)
    val pdfView = MutableStateFlow<PDFView?>(null)
    var showLoadingDialog = MutableStateFlow(false)

    var showExitEditPromptDialog = MutableStateFlow(false)

    var clickMenuType = MutableStateFlow<EditMenuType>(EditMenuType.PAGE_MANAGEMENT)
    val curSticker = MutableStateFlow<Sticker?>(null)
    private val stickerList = MutableStateFlow(emptyList<Sticker>())
    private val stickerDelList = MutableStateFlow(emptyList<Sticker>())
    private val stickerSaveList = MutableStateFlow(emptyList<Sticker>())
    private val stickerClickHandler = StickerClickHandler()

    private val curPage = MutableStateFlow(0)//当前所在页，意义不大。关键还是firstVisibleArea来测量为准
    private val totalPageCount = MutableStateFlow(0)//页面数量
    private val stickersVisibleArea = MutableStateFlow(IntSize.Zero)//PDFView和贴纸操作可视化区域
    private val firstVisibleArea = MutableStateFlow(FirstVisibleArea())//第一个可见页
    val revokeImage = MutableStateFlow(R.mipmap.module_pdf_edit_icon_xyb_nor)

    fun createPDFView(context: Context, file: File) {
        pdfView.value = null
        pdfFile.value = file
        pdfView.value = PDFView(context, null).apply {
            fromFile(file).enableSwipe(true).enableDoubletap(true).linkHandler(null)
                .onDrawAll { canvas, pageWidth, pageHeight, displayedPage ->
                    // 计算水印在Box中的相对位置
                    PdfDrawManager.drawStickerToPrePage(
                        context, stickerList.value, canvas, displayedPage, stickerClickHandler
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
                    when (clickMenuType.value) {
                        EditMenuType.PAGE_MANAGEMENT -> clickToSelectTextSticker(it)
                        EditMenuType.INSERT_TEXT -> addTextStickerToPdf(it.x, it.y)
                        EditMenuType.INSERT_IMAGE -> {}
                    }
                    return@onTap true
                }.load()
        }
        stickerClickHandler.clearAll()
        curSticker.value = null
        stickerList.value = emptyList()
        stickerDelList.value = emptyList()
        stickerSaveList.value = emptyList()
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
        val firstVisiblePage = (pixelOffset / pageHeight).toInt().coerceIn(0, pageCount - 1)//可见页码
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
                stickerList.value.firstOrNull { it.id == id }?.let {
                    val newOffset = Offset(x = it.offset.x, it.offset.y - yOffset)
                    it.isSelected = true
                    it.offset = newOffset
                    curSticker.value = it
                    //置顶当前选中的贴纸到图层上
                    stickerList.update { it.filterNot { it.id == id } }
                }
                stickerClickHandler.clearClickableRegion(id)
                pdfView.value?.invalidate()
            }
        }
    }

    fun setStickersVisibleArea(intSize: IntSize) {
        stickersVisibleArea.value = intSize
    }

    fun switchInsertMode(value: EditMenuType, activity: FragmentActivity) {
        if (curSticker.value?.isSelected == true) {
            return ToastUtils.showShort(exitEditingModeTips)
        }
        clickMenuType.value = value
        if (value != EditMenuType.PAGE_MANAGEMENT) {
            pdfView.value?.apply {
                resetZoomWithAnimation()
            }
        }
        when (value) {
            EditMenuType.PAGE_MANAGEMENT -> {
                onSave(activity) { b, f ->
                    if (b) {
                        PdfManagerActivity.start(activity, f)
                    }
                }
            }

            EditMenuType.INSERT_TEXT -> ToastUtils.showLong("请点击选择要插入的位置")
            EditMenuType.INSERT_IMAGE -> Unit
        }
    }

    fun prepareInsertImageMode(): Boolean {
        if (curSticker.value?.isSelected == true) {
            ToastUtils.showShort(exitEditingModeTips)
            return false
        }
        clickMenuType.value = EditMenuType.INSERT_IMAGE
        pdfView.value?.resetZoomWithAnimation()
        return true
    }

    fun addImageSticker(bitmap: Bitmap) {
        addImageStickerToPdf(bitmap)
    }

    private fun addTextStickerToPdf(x: Float, y: Float) {
        val textSticker = TextSticker(
            id = System.currentTimeMillis(),
            isSelected = true,
            curPage = curPage.value,
            offset = Offset(x, y),
            previewArea = stickersVisibleArea.value,
            text = TextFieldValue(text = ""),
        )
        curSticker.value = textSticker
    }

    private fun addImageStickerToPdf(bitmap: Bitmap) {
        val imageSticker = ImageSticker(
            id = System.currentTimeMillis(),
            isSelected = true,
            curPage = curPage.value,
            offset = Offset.Zero,
            previewArea = stickersVisibleArea.value,
            bitmap = bitmap
        )
        curSticker.value = imageSticker
    }

    fun onUpdateTextSticker(sticker: TextSticker?) {
        curSticker.value = sticker
    }

    fun onUpdateImageSticker(sticker: ImageSticker?) {
        curSticker.value = sticker
    }

    fun onDeleteCurSticker() {
        curSticker.value?.let { sticker ->
            deleteSticker(sticker)

            stickerList.update { it.filterNot { it.id == sticker.id } }
            curSticker.value = null

            unSelectedStatusAndDrawToPreview()
        }
    }

    fun unSelectedStatusAndDrawToPreview() {
        clickMenuType.value = EditMenuType.PAGE_MANAGEMENT
        drawStickers()
    }

    //将水印绘制到PDFView上
    private fun drawStickers() {
        curSticker.value?.let { sticker ->
            sticker.isSelected = false
            //调整水印绘制位置
            val offset = sticker.offset
            //是否相对first的下一页
            val inNextPage = sticker.offset.y > firstVisibleArea.value.visibleHeight
            sticker.curPage = firstVisibleArea.value.page + if (inNextPage) 1 else 0
            val yOffset =
                if (inNextPage) -firstVisibleArea.value.visibleHeight else firstVisibleArea.value.yOffset
            sticker.offset = Offset(offset.x, offset.y + yOffset)
            val index = stickerList.value.indexOfFirst { it.id == sticker.id }
            if (index > 0) {
                stickerList.update { list ->
                    list.toMutableList().apply {
                        set(index, sticker) // 替换指定位置元素
                    }
                }
            } else {
                // 添加到列表（保持不可变性）
                addSticker(sticker)
            }
        }
        pdfView.value?.invalidate()
        curSticker.value = null
    }

    //复制图片贴纸
    fun onCopy() {
        val copySticker = curSticker.value
        unSelectedStatusAndDrawToPreview()

        (copySticker as? ImageSticker)?.let {
            curSticker.value = ImageSticker(
                id = System.currentTimeMillis(),
                isSelected = true,
                curPage = it.curPage,
                offset = Offset(x = it.offset.x + 50, y = it.offset.y + 50),
                previewArea = it.previewArea,
                rotation = it.rotation,
                scaleRatio = it.scaleRatio,
                bitmap = it.bitmap
            )
        }
    }

    fun onComplete(context: Context) {
        if (curSticker.value?.isSelected == true) {
            return ToastUtils.showShort(exitEditingModeTips)
        }
        //上次触发保存的列表与更新过的不一致，触发退出编辑提示弹窗
        if (stickerSaveList.value.containsAll(stickerList.value)) {
            onDialogNotSave(context)
        } else {
            if (stickerList.value.isNotEmpty()) {
                showExitEditPromptDialog.value = true
            } else {
                onDialogNotSave(context)
            }
        }
    }

    fun onDialogNotSave(context: Context) {
        showExitEditPromptDialog.value = false
        (context as? FragmentActivity)?.finish()
    }

    fun onDialogSave(context: Context) {
        onSave(context) { b, f ->
            showExitEditPromptDialog.value = false
            (context as? FragmentActivity)?.finish()
        }
    }

    //保存
    fun onSave(context: Context, onCallback: ((Boolean, File) -> Unit)? = null) {
        if (curSticker.value?.isSelected == true) {
            return ToastUtils.showShort(exitEditingModeTips)
        }
        pdfFile.value?.let {
            val originalPdfFile = File(it.path)
            //没有编辑过的情况
            if (stickerList.value.isEmpty()) {
                onCallback?.invoke(true, originalPdfFile)
            } else {
                val outputPdfFile = PdfDocumentManger.createPdfFile()
                PdfDocumentManger.drawableStickerToPDDocument(
                    context = context,
                    originalPdfFile = originalPdfFile,
                    outputPdfFile = outputPdfFile,
                    stickers = stickerList.value,
                ).onStart {
                    showLoadingDialog.value = true
                }.onCompletion {
                    showLoadingDialog.value = false
                    ToastUtils.showShort(PdfDocumentManger.SAVE_SUCCESS_TIPS)
                    onCallback?.invoke(true, outputPdfFile)
                    insertPDFFile(outputPdfFile.path)
                    stickerSaveList.value = stickerList.value
                }.catch { e ->
                    showLoadingDialog.value = false
                    ToastUtils.showShort("保存失败")
                    onCallback?.invoke(false, outputPdfFile)
                    stickerSaveList.value = stickerList.value
                }.launchIn(viewModelScope)
            }
        }
    }

    private fun updateRevokeImage() {
        revokeImage.value = if (stickerDelList.value.isEmpty()) {
            R.mipmap.module_pdf_edit_icon_xyb_nor
        } else {
            R.mipmap.module_pdf_edit_icon_xyb_sel
        }
    }

    //撤销
    fun onRevoke() {
        if (curSticker.value?.isSelected == true) {
            return ToastUtils.showShort(exitEditingModeTips)
        }
        stickerList.value.lastOrNull()?.let {
            deleteSticker(it)
            unSelectedStatusAndDrawToPreview()
        }
    }

    //恢复
    fun onRecover() {
        if (curSticker.value?.isSelected == true) {
            return ToastUtils.showShort(exitEditingModeTips)
        }
        stickerDelList.value.lastOrNull()?.let {
            addSticker(it)
            unSelectedStatusAndDrawToPreview()
        }
    }

    private fun addSticker(sticker: Sticker) {
        stickerList.update { it + sticker }
        stickerDelList.update { it - sticker }
        updateRevokeImage()
    }

    private fun deleteSticker(sticker: Sticker) {
        stickerList.update { it - sticker }
        stickerDelList.update { it + sticker }
        updateRevokeImage()
    }

    fun insertPDFFile(path: String?) = viewModelScope.launch {
        RecentFileRepository.insertFile(path, FileInfoFormat.PDF)
    }
}