package com.yung.module_pdf.internal.ui.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.blankj.utilcode.util.ToastUtils
import com.yung.module_pdf.internal.core.event.PdfEvent
import com.yung.module_pdf.internal.core.event.PdfEventBusConstants
import com.yung.module_pdf.internal.core.file.FileManager
import com.yung.module_pdf.internal.ui.activity.PdfEditorActivity
import com.yung.module_pdf.internal.ui.activity.PdfPreviewActivity
import org.greenrobot.eventbus.EventBus
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun CollectFileManagerSideEffects(viewModel: FileManagerViewModel) {
    val context = LocalContext.current
    viewModel.collectSideEffect { effect ->
        when (effect) {
            is FileManagerSideEffect.Toast -> ToastUtils.showShort(effect.message)
            is FileManagerSideEffect.OpenPdfPreview ->
                PdfPreviewActivity.start(context, effect.entity)

            is FileManagerSideEffect.OpenPdfEditor ->
                PdfEditorActivity.start(context, effect.file)

            is FileManagerSideEffect.OpenExternalFile ->
                FileManager.openFile(context, effect.path)

            FileManagerSideEffect.RefreshFileList ->
                EventBus.getDefault()
                    .post(PdfEvent(PdfEventBusConstants.REFRESH_SELECT_FILE_LIST))
        }
    }
}

@Composable
fun CollectPdfPreviewSideEffects(viewModel: PdfPreviewViewModel) {
    val context = LocalContext.current
    viewModel.collectSideEffect { effect ->
        when (effect) {
            is PdfPreviewSideEffect.Toast -> ToastUtils.showShort(effect.message)
            is PdfPreviewSideEffect.OpenEditor -> PdfEditorActivity.start(context, effect.file)
            is PdfPreviewSideEffect.ShareFile -> FileManager.shareFile(context, effect.file)
        }
    }
}

@Composable
fun CollectPdfEditorSideEffects(viewModel: PdfEditorViewModel) {
    val context = LocalContext.current
    viewModel.collectSideEffect { effect ->
        when (effect) {
            is PdfEditorSideEffect.Toast -> ToastUtils.showShort(effect.message)
            PdfEditorSideEffect.Finish -> (context as? android.app.Activity)?.finish()
            is PdfEditorSideEffect.OpenPageManager ->
                com.yung.module_pdf.internal.ui.activity.PdfManagerActivity.start(context, effect.file)
        }
    }
}

@Composable
fun CollectPdfManagerSideEffects(viewModel: PdfManagerViewModel) {
    val context = LocalContext.current
    viewModel.collectSideEffect { effect ->
        when (effect) {
            is PdfManagerSideEffect.Toast -> ToastUtils.showShort(effect.message)
            PdfManagerSideEffect.Finish -> (context as? android.app.Activity)?.finish()
            is PdfManagerSideEffect.RefreshEditFile ->
                EventBus.getDefault()
                    .post(PdfEvent(PdfEventBusConstants.REFRESH_EDIT_FILE, effect.file))
        }
    }
}
