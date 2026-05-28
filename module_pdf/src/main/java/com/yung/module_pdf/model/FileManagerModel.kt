package com.yung.module_pdf.model

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.ToastUtils
import com.yung.module_pdf.common.PdfEvent
import com.yung.module_pdf.db.FileInfoEntity
import com.yung.module_pdf.db.FileInfoFormat
import com.yung.module_pdf.internal.data.RecentFileRepository
import com.yung.module_pdf.activity.PdfEditorActivity
import com.yung.module_pdf.activity.PdfPreviewActivity
import com.yung.module_pdf.common.FileMenuType
import com.yung.module_pdf.common.PdfEventBusConstants
import com.yung.module_pdf.common.PdfSelectMode
import com.yung.module_pdf.utils.FileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.io.File

class FileManagerModel : ViewModel() {

    val showFileMoreBottomSheet = MutableStateFlow(false)
    val showFileRenameDialog = MutableStateFlow(false)
    val showFileDeleteDialog = MutableStateFlow(false)
    val moreFileInfoEntity = MutableStateFlow<FileInfoEntity?>(null)

    fun setFileMoreEntity(entity: FileInfoEntity?) {
        moreFileInfoEntity.value = entity
    }

    fun switchFileMoreBottomSheet(value: Boolean) {
        showFileMoreBottomSheet.value = value
    }

    fun switchFileDeleteDialog(value: Boolean) {
        showFileDeleteDialog.value = value
        if (!value) {
            setFileMoreEntity(null)
        }
    }

    fun switchFileRenameDialog(value: Boolean) {
        showFileRenameDialog.value = value
        if (!value) {
            setFileMoreEntity(null)
        }
    }

    fun openFile(
        context: Context,
        entity: FileInfoEntity,
        pdfSelectMode: PdfSelectMode = PdfSelectMode.PREVIEW,
    ) {
        if (!File(entity.path).exists()) return ToastUtils.showShort("文件不存在")
        if (entity.format == FileInfoFormat.PDF) {
            when (pdfSelectMode) {
                PdfSelectMode.PREVIEW -> PdfPreviewActivity.start(context, entity)
                PdfSelectMode.MANAGEMENT -> PdfEditorActivity.start(context, File(entity.path))
            }
        } else {
            FileManager.openFile(context, entity.path)
        }
    }

    fun onFileMoreEvent(context: Context, type: FileMenuType, entity: FileInfoEntity?) = viewModelScope.launch {
        entity?.let {
            when (type) {
                FileMenuType.DELETE -> switchFileDeleteDialog(true)
                FileMenuType.RENAME -> switchFileRenameDialog(true)
                FileMenuType.SHARE -> {
                    FileManager.shareFile(context, entity.path)
                    setFileMoreEntity(null)
                }
            }
        }
    }

    fun onDeleteFile() {
        moreFileInfoEntity.value?.let { entity ->
            FileManager.deleteFile(entity.path) { isSuccess, msg ->
                viewModelScope.launch(Dispatchers.IO) {
                    RecentFileRepository.deleteRecord(entity)
                }
                if (entity.id != null) {
                    ToastUtils.showShort("删除成功")
                } else {
                    ToastUtils.showShort(msg)
                }
                EventBus.getDefault()
                    .post(PdfEvent(PdfEventBusConstants.REFRESH_SELECT_FILE_LIST))
            }
        }
    }

    fun onRenameFile(newName: String) {
        moreFileInfoEntity.value?.let { entity ->
            FileManager.renameFile(entity.path, newName) { newFile, msg ->
                if (newFile != null) {
                    viewModelScope.launch(Dispatchers.IO) {
                        RecentFileRepository.updateRecord(
                            entity.copy(
                                name = newName,
                                path = newFile.absolutePath,
                                time = System.currentTimeMillis(),
                            )
                        )
                    }
                }
                ToastUtils.showShort(msg)
                EventBus.getDefault()
                    .post(PdfEvent(PdfEventBusConstants.REFRESH_SELECT_FILE_LIST))
            }
        }
    }
}