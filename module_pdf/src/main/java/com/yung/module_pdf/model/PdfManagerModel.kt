package com.yung.module_pdf.model

import android.content.Context
import androidx.compose.ui.unit.IntSize
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.ToastUtils
import com.yung.module_pdf.common.PdfEvent
import com.yung.module_pdf.db.FileInfoFormat
import com.yung.module_pdf.internal.data.RecentFileRepository
import com.yung.module_pdf.db.insertFile
import com.yung.module_pdf.common.PdfEventBusConstants
import com.yung.module_pdf.utils.IdGen
import com.yung.module_pdf.entity.PdfItemInfo
import com.yung.module_pdf.utils.PdfDocumentManger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ItemPosition
import org.greenrobot.eventbus.EventBus
import java.io.File

class PdfManagerModel : ViewModel() {

    val pdfFile = MutableStateFlow<File?>(null)
    val isEditModel = MutableStateFlow(false)
    val pdfItemInfoList = MutableStateFlow(emptyList<PdfItemInfo>())
    val pdfItemInfoSelectedList = MutableStateFlow(emptyList<PdfItemInfo>())
    var showLoadingDialog = MutableStateFlow(false)
    var showLoadingSaveDialog = MutableStateFlow(false)
    var showDeleteDialog = MutableStateFlow(false)
    var showExitEditPromptDialog = MutableStateFlow(false)
    val itemBoxSize = MutableStateFlow(IntSize.Zero)

    fun analysisPdf(file: File?) = viewModelScope.launch {
        pdfFile.value = file
        PdfDocumentManger.analysisPDPages(file)
            .onStart {
                showLoadingDialog.value = true
            }.onCompletion {
                showLoadingDialog.value = false
                ToastUtils.showShort("解析成功")
            }.catch { e ->
                showLoadingDialog.value = false
                ToastUtils.showShort("解析失败")
            }.collect {
                pdfItemInfoList.value = it
                if (it.isNotEmpty()) {
                    itemBoxSize.value = IntSize(it.maxOf { it.width }, it.maxOf { it.height })
                }
            }
    }

    fun enterEditMode() = viewModelScope.launch {
        if (isEditModel.value) {
            onSave()
        } else {
            isEditModel.value = true
        }
    }

    fun onMove(from: ItemPosition, to: ItemPosition) {
        pdfItemInfoList.value = pdfItemInfoList.value.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
    }

    fun addToSelectedList(info: PdfItemInfo) {
        if (isEditModel.value) {
            if (pdfItemInfoSelectedList.value.any { it.id == info.id }) {
                pdfItemInfoSelectedList.update { it - info }
            } else {
                pdfItemInfoSelectedList.update { it + info }
            }
        }
    }

    fun selectAll() {
        if (!isEditModel.value) return
        if (pdfItemInfoSelectedList.value == pdfItemInfoList.value) {
            pdfItemInfoSelectedList.value = emptyList()
        } else {
            pdfItemInfoSelectedList.value = pdfItemInfoList.value
        }
    }

    fun addPage() {
        if (pdfItemInfoSelectedList.value.isEmpty()) return
        val first = pdfItemInfoList.value.first()
        val rotatedMap = pdfItemInfoSelectedList.value.associateBy { it.id }

        val addInfo = PdfItemInfo(IdGen.next(), null, first.width, first.height, 0)
        val insertIndex = pdfItemInfoList.value.indexOfFirst { rotatedMap.containsKey(it.id) }

        pdfItemInfoList.update {
            it.toMutableList().apply {
                add(insertIndex, addInfo)
            }
        }
    }

    fun rotatePage() {
        if (pdfItemInfoSelectedList.value.isEmpty()) return

        val rotated = pdfItemInfoSelectedList.value.map { info ->
            val angle = (info.rotationAngle + 90) % 360
            info.copy(rotationAngle = angle)
        }
        pdfItemInfoSelectedList.value = rotated

        val rotatedMap = rotated.associateBy { it.id }

        pdfItemInfoList.value = pdfItemInfoList.value.map { old ->
            rotatedMap[old.id] ?: old
        }
    }

    fun onDelete() {
        if (pdfItemInfoSelectedList.value.isEmpty()) return
        showDeleteDialog.value = true
    }

    fun onDialogDeletePage() {
        if (pdfItemInfoSelectedList.value.size == pdfItemInfoList.value.size) {
            return ToastUtils.showShort("删除失败，至少保留一个页面")
        }
        val rotatedMap = pdfItemInfoSelectedList.value.associateBy { it.id }
        pdfItemInfoList.update {
            it.filterNot { rotatedMap.containsKey(it.id) }
        }
        pdfItemInfoSelectedList.value = emptyList()
    }

    fun onDialogUnDeletePage() {
        showDeleteDialog.value = false
    }

    fun exitPageManager(context: Context) {
        if (isEditModel.value) {
            showExitEditPromptDialog.value = true
        } else {
            (context as? FragmentActivity)?.finish()
        }
    }

    private fun onSave(onCallback: ((File) -> Unit)? = null) = viewModelScope.launch {
        pdfFile.value?.let {
            val outputPdfFile = PdfDocumentManger.createPdfFile()
            PdfDocumentManger.createNewPDDocument(
                originalPdfFile = File(it.path),
                outputPdfFile = outputPdfFile,
                pdItems = pdfItemInfoList.value
            ).onStart {
                showLoadingSaveDialog.value = true
            }.onCompletion {
                showLoadingSaveDialog.value = false
                isEditModel.value = false
                ToastUtils.showShort("保存成功")
                onCallback?.invoke(outputPdfFile)
                EventBus.getDefault()
                    .post(PdfEvent(PdfEventBusConstants.REFRESH_EDIT_FILE, outputPdfFile))
                insertPDFFile(outputPdfFile.path)
            }.catch {
                ToastUtils.showShort("保存失败")
            }.launchIn(this)
        }
    }

    fun onDialogNotSave(context: Context) {
        (context as? FragmentActivity)?.finish()
    }

    fun onDialogSave(context: Context) {
        showExitEditPromptDialog.value = false
        onSave {
            (context as? FragmentActivity)?.finish()
        }
    }

    fun insertPDFFile(path: String?) = viewModelScope.launch {
        RecentFileRepository.insertFile(path, FileInfoFormat.PDF)
    }
}