package com.yung.module_pdf.internal.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yung.module_pdf.api.PdfSelectMode
import com.yung.module_pdf.internal.core.file.FileManager
import com.yung.module_pdf.internal.data.RecentFileRepository
import com.yung.module_pdf.internal.db.FileInfoEntity
import com.yung.module_pdf.api.RecentFileFormat
import com.yung.module_pdf.internal.ui.component.FileMenuType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container as orbitContainer
import java.io.File

data class FileManagerState(
    val showFileMoreBottomSheet: Boolean = false,
    val showFileRenameDialog: Boolean = false,
    val showFileDeleteDialog: Boolean = false,
    val moreFileInfoEntity: FileInfoEntity? = null,
)

sealed interface FileManagerSideEffect {
    data class Toast(val message: String) : FileManagerSideEffect
    data class OpenPdfPreview(val entity: FileInfoEntity) : FileManagerSideEffect
    data class OpenPdfEditor(val file: File) : FileManagerSideEffect
    data class OpenExternalFile(val path: String) : FileManagerSideEffect
    data object RefreshFileList : FileManagerSideEffect
}

class FileManagerViewModel : ViewModel(), ContainerHost<FileManagerState, FileManagerSideEffect> {

    override val container: Container<FileManagerState, FileManagerSideEffect> =
        orbitContainer(FileManagerState())

    fun setFileMoreEntity(entity: FileInfoEntity?) = intent {
        reduce { state.copy(moreFileInfoEntity = entity) }
    }

    fun switchFileMoreBottomSheet(value: Boolean) = intent {
        reduce { state.copy(showFileMoreBottomSheet = value) }
    }

    fun switchFileDeleteDialog(value: Boolean) = intent {
        reduce {
            state.copy(
                showFileDeleteDialog = value,
                moreFileInfoEntity = if (value) state.moreFileInfoEntity else null,
            )
        }
    }

    fun switchFileRenameDialog(value: Boolean) = intent {
        reduce {
            state.copy(
                showFileRenameDialog = value,
                moreFileInfoEntity = if (value) state.moreFileInfoEntity else null,
            )
        }
    }

    fun openFile(
        context: Context,
        entity: FileInfoEntity,
        pdfSelectMode: PdfSelectMode = PdfSelectMode.PREVIEW,
    ) = intent {
        if (!File(entity.path).exists()) {
            postSideEffect(FileManagerSideEffect.Toast("文件不存在"))
            return@intent
        }
        when {
            entity.format != RecentFileFormat.PDF ->
                postSideEffect(FileManagerSideEffect.OpenExternalFile(entity.path))

            pdfSelectMode == PdfSelectMode.PREVIEW ->
                postSideEffect(FileManagerSideEffect.OpenPdfPreview(entity))

            else ->
                postSideEffect(FileManagerSideEffect.OpenPdfEditor(File(entity.path)))
        }
    }

    fun onFileMoreEvent(context: Context, type: FileMenuType, entity: FileInfoEntity?) = intent {
        entity ?: return@intent
        when (type) {
            FileMenuType.DELETE -> reduce { state.copy(showFileDeleteDialog = true) }
            FileMenuType.RENAME -> reduce { state.copy(showFileRenameDialog = true) }
            FileMenuType.SHARE -> {
                FileManager.shareFile(context, entity.path)
                reduce { state.copy(moreFileInfoEntity = null) }
            }
        }
    }

    fun onDeleteFile() {
        val entity = container.stateFlow.value.moreFileInfoEntity ?: return
        FileManager.deleteFile(entity.path) { _, msg ->
            viewModelScope.launch(Dispatchers.IO) {
                RecentFileRepository.deleteRecord(entity)
            }
            viewModelScope.launch {
                intent {
                    postSideEffect(
                        FileManagerSideEffect.Toast(
                            if (entity.id != null) "删除成功" else msg
                        )
                    )
                    postSideEffect(FileManagerSideEffect.RefreshFileList)
                }
            }
        }
    }

    fun onRenameFile(newName: String) {
        val entity = container.stateFlow.value.moreFileInfoEntity ?: return
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
            viewModelScope.launch {
                intent {
                    postSideEffect(FileManagerSideEffect.Toast(msg))
                    postSideEffect(FileManagerSideEffect.RefreshFileList)
                }
            }
        }
    }
}
