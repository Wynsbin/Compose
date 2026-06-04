package com.yung.module_pdf.internal.ui.viewmodel

import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yung.module_pdf.internal.core.ext.IdGen
import com.yung.module_pdf.internal.core.pdf.PdfDocumentManger
import com.yung.module_pdf.internal.data.RecentFileRepository
import com.yung.module_pdf.api.RecentFileFormat
import com.yung.module_pdf.internal.domain.PdfItemInfo
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import java.io.File
import org.orbitmvi.orbit.viewmodel.container as orbitContainer

data class PdfManagerState(
    val pdfFile: File? = null,
    val isEditModel: Boolean = false,
    val pdfItemInfoList: List<PdfItemInfo> = emptyList(),
    val pdfItemInfoSelectedList: List<PdfItemInfo> = emptyList(),
    val showLoadingDialog: Boolean = false,
    val showLoadingSaveDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showExitEditPromptDialog: Boolean = false,
    val itemBoxSize: IntSize = IntSize.Zero,
)

sealed interface PdfManagerSideEffect {
    data class Toast(val message: String) : PdfManagerSideEffect
    data object Finish : PdfManagerSideEffect
    data class RefreshEditFile(val file: File) : PdfManagerSideEffect
}

class PdfManagerViewModel : ViewModel(), ContainerHost<PdfManagerState, PdfManagerSideEffect> {

    override val container: Container<PdfManagerState, PdfManagerSideEffect> =
        orbitContainer(PdfManagerState())

    fun analysisPdf(file: File?) {
        viewModelScope.launch {
            intent { reduce { state.copy(pdfFile = file) } }
            PdfDocumentManger.analysisPDPages(file)
                .onStart {
                    intent { reduce { state.copy(showLoadingDialog = true) } }
                }
                .onCompletion {
                    intent {
                        reduce { state.copy(showLoadingDialog = false) }
                        postSideEffect(PdfManagerSideEffect.Toast("解析成功"))
                    }
                }
                .catch {
                    intent {
                        reduce { state.copy(showLoadingDialog = false) }
                        postSideEffect(PdfManagerSideEffect.Toast("解析失败"))
                    }
                }
                .collect { items ->
                    intent {
                        reduce {
                            state.copy(
                                pdfItemInfoList = items,
                                itemBoxSize = if (items.isNotEmpty()) {
                                    IntSize(items.maxOf { it.width }, items.maxOf { it.height })
                                } else {
                                    IntSize.Zero
                                },
                            )
                        }
                    }
                }
        }
    }

    fun enterEditMode(currentOrder: List<PdfItemInfo>) {
        if (container.stateFlow.value.isEditModel) {
            intent { reduce { state.copy(pdfItemInfoList = currentOrder) } }
            savePages(pages = currentOrder)
        } else {
            intent { reduce { state.copy(isEditModel = true) } }
        }
    }

    fun updatePdfItemOrder(list: List<PdfItemInfo>) = intent {
        reduce { state.copy(pdfItemInfoList = list) }
    }

    fun addToSelectedList(info: PdfItemInfo) = intent {
        if (!state.isEditModel) return@intent
        val selected = state.pdfItemInfoSelectedList
        reduce {
            state.copy(
                pdfItemInfoSelectedList = if (selected.any { it.id == info.id }) {
                    selected - info
                } else {
                    selected + info
                },
            )
        }
    }

    fun selectAll(currentOrder: List<PdfItemInfo>) = intent {
        if (!state.isEditModel) return@intent
        val selectedIds = state.pdfItemInfoSelectedList.map { it.id }.toSet()
        val allSelected = currentOrder.isNotEmpty() && selectedIds.size == currentOrder.size &&
            selectedIds.containsAll(currentOrder.map { it.id })
        reduce {
            state.copy(
                pdfItemInfoSelectedList = if (allSelected) emptyList() else currentOrder,
            )
        }
    }

    fun addPage(currentOrder: List<PdfItemInfo>) = intent {
        if (state.pdfItemInfoSelectedList.isEmpty()) return@intent
        val first = currentOrder.firstOrNull() ?: return@intent
        val selectedIds = state.pdfItemInfoSelectedList.map { it.id }.toSet()
        val addInfo = PdfItemInfo(IdGen.next(), null, first.width, first.height, 0)
        val insertIndex = currentOrder.indexOfFirst { selectedIds.contains(it.id) }
        val list = currentOrder.toMutableList().apply { add(insertIndex, addInfo) }
        reduce { state.copy(pdfItemInfoList = list) }
    }

    fun rotatePage(currentOrder: List<PdfItemInfo>) = intent {
        if (state.pdfItemInfoSelectedList.isEmpty()) return@intent
        val rotated = state.pdfItemInfoSelectedList.map { info ->
            info.copy(rotationAngle = (info.rotationAngle + 90) % 360)
        }
        val rotatedMap = rotated.associateBy { it.id }
        reduce {
            state.copy(
                pdfItemInfoSelectedList = rotated,
                pdfItemInfoList = currentOrder.map { old -> rotatedMap[old.id] ?: old },
            )
        }
    }

    fun onDelete() = intent {
        if (state.pdfItemInfoSelectedList.isEmpty()) return@intent
        reduce { state.copy(showDeleteDialog = true) }
    }

    fun onDialogDeletePage(currentOrder: List<PdfItemInfo>) = intent {
        if (state.pdfItemInfoSelectedList.size == currentOrder.size) {
            postSideEffect(PdfManagerSideEffect.Toast("删除失败，至少保留一个页面"))
            return@intent
        }
        val selectedIds = state.pdfItemInfoSelectedList.map { it.id }.toSet()
        reduce {
            state.copy(
                pdfItemInfoList = currentOrder.filterNot { selectedIds.contains(it.id) },
                pdfItemInfoSelectedList = emptyList(),
                showDeleteDialog = false,
            )
        }
    }

    fun onDialogUnDeletePage() = intent {
        reduce { state.copy(showDeleteDialog = false) }
    }

    fun exitPageManager() = intent {
        if (state.isEditModel) {
            reduce { state.copy(showExitEditPromptDialog = true) }
        } else {
            postSideEffect(PdfManagerSideEffect.Finish)
        }
    }

    fun onDialogNotSave() = intent {
        reduce { state.copy(showExitEditPromptDialog = false) }
        postSideEffect(PdfManagerSideEffect.Finish)
    }

    fun onDialogSave(currentOrder: List<PdfItemInfo>) = intent {
        reduce { state.copy(showExitEditPromptDialog = false, pdfItemInfoList = currentOrder) }
        savePages(shouldFinish = true, pages = currentOrder)
    }

    fun insertPDFFile(path: String?) {
        viewModelScope.launch {
            RecentFileRepository.insertFile(path, RecentFileFormat.PDF)
        }
    }

    private fun savePages(shouldFinish: Boolean = false, pages: List<PdfItemInfo>? = null) {
        val pdfFile = container.stateFlow.value.pdfFile ?: return
        val pdItems = pages ?: container.stateFlow.value.pdfItemInfoList
        val outputPdfFile = PdfDocumentManger.createPdfFile()
        PdfDocumentManger.createNewPDDocument(
            originalPdfFile = File(pdfFile.path),
            outputPdfFile = outputPdfFile,
            pdItems = pdItems,
        ).onStart {
            intent { reduce { state.copy(showLoadingSaveDialog = true) } }
        }.onCompletion {
            intent {
                reduce {
                    state.copy(
                        showLoadingSaveDialog = false,
                        isEditModel = false,
                    )
                }
                postSideEffect(PdfManagerSideEffect.Toast("保存成功"))
                postSideEffect(PdfManagerSideEffect.RefreshEditFile(outputPdfFile))
                if (shouldFinish) {
                    postSideEffect(PdfManagerSideEffect.Finish)
                }
            }
            insertPDFFile(outputPdfFile.path)
        }.catch {
            intent {
                reduce { state.copy(showLoadingSaveDialog = false) }
                postSideEffect(PdfManagerSideEffect.Toast("保存失败"))
            }
        }.launchIn(viewModelScope)
    }
}
