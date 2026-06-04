package com.yung.module_pdf.internal.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blankj.utilcode.util.ToastUtils
import com.yung.module_pdf.internal.core.event.PdfEvent
import com.yung.module_pdf.R
import com.yung.module_pdf.internal.ui.component.BottomItemMenuBar
import com.yung.module_pdf.internal.ui.component.BottomMenuBar
import com.yung.module_pdf.internal.ui.component.BottomStyleMenuBar
import com.yung.module_pdf.internal.ui.component.EditMenuType
import com.yung.module_pdf.internal.core.event.EventBusListener
import com.yung.module_pdf.internal.ui.component.ExitEditPromptDialog
import com.yung.module_pdf.internal.ui.component.FontSelectBottomSheet
import com.yung.module_pdf.internal.ui.sticker.ImageSticker
import com.yung.module_pdf.internal.ui.sticker.ImageStickerBox
import com.yung.module_pdf.internal.ui.component.LoadingDialog
import com.yung.module_pdf.internal.core.event.PdfEventBusConstants
import com.yung.module_pdf.internal.ui.sticker.StickerMaskLayer
import com.yung.module_pdf.internal.ui.component.TextColorSelectionPanel
import com.yung.module_pdf.internal.ui.sticker.TextSticker
import com.yung.module_pdf.internal.ui.sticker.TextStickerBox
import com.yung.module_pdf.internal.ui.component.TextStyleBottomSheet
import com.yung.module_pdf.internal.ui.component.editMenus
import com.yung.module_pdf.internal.ui.component.noRippleClickable
import com.yung.module_pdf.internal.ui.viewmodel.CollectPdfEditorSideEffects
import com.yung.module_pdf.internal.ui.viewmodel.PdfEditorViewModel
import com.yung.module_pdf.internal.core.media.ImagePickHelper
import com.yung.module_pdf.internal.core.permission.PermissionUseCase
import org.orbitmvi.orbit.compose.collectAsState
import java.io.File

class PdfEditorActivity : FragmentActivity() {
    companion object {

        const val KEY_FILE = "KEY_FILE"

        @JvmStatic
        fun start(context: Context, file: File) {
            if (file.exists()) {
                val starter = Intent(context, PdfEditorActivity::class.java)
                    .putExtra(KEY_FILE, file)
                context.startActivity(starter)
            } else {
                ToastUtils.showShort("文件不存在")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val pdfFile = intent?.getSerializableExtra(KEY_FILE) as? File
        setContent {
            PdfEditorScreen(pdfFile)
        }
    }
}

@Preview
@Composable
private fun PdfEditorScreen(pdfFile: File? = null, viewModel: PdfEditorViewModel = viewModel()) {

    CollectPdfEditorSideEffects(viewModel)
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val uiState by viewModel.collectAsState()
    val pdfView = uiState.pdfView
    val clickMenuType = uiState.clickMenuType
    val curSticker = uiState.curSticker
    val revokeImage = uiState.revokeImage
    val showLoadingDialog = uiState.showLoadingDialog
    val showExitEditPromptDialog = uiState.showExitEditPromptDialog
    var showFontSelectBottomSheet by remember { mutableStateOf(false) }
    var showTextStyleBottomSheet by remember { mutableStateOf(false) }
    var showTextColorBottomSheet by remember { mutableStateOf(false) }

    val pickPhoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val bitmap = ImagePickHelper.loadBitmapFromUri(context, uri)
        if (bitmap == null) {
            ToastUtils.showShort("无法读取所选图片")
            return@rememberLauncherForActivityResult
        }
        viewModel.addImageSticker(bitmap)
    }

    fun clickEditMenu(type: EditMenuType) {
        val act = activity ?: return
        if (type == EditMenuType.INSERT_IMAGE) {
            if (viewModel.prepareInsertImageMode()) {
                PermissionUseCase.useGalleryImages(
                    activity = act,
                    allow = { pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) })
            }
            return
        }
        viewModel.switchInsertMode(type, act)
    }

    LaunchedEffect(Unit) {
        viewModel.insertPDFFile(pdfFile?.path)
    }

    LaunchedEffect(Unit) {
        viewModel.createPDFView(context, pdfFile!!)
    }

    BackHandler {
        viewModel.onComplete(context)
    }

    EventBusListener(PdfEvent::class.java) { eventBusEntity ->
        if (eventBusEntity.code == PdfEventBusConstants.REFRESH_EDIT_FILE) {
            (eventBusEntity.data as? File)?.let {
                viewModel.createPDFView(context, it)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xffF7F7F7))
        ) {
            Row(
                modifier = Modifier
                    .background(Color.White)
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .height(44.dp), verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "完成",
                    color = Color(0xff252525),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .noRippleClickable { viewModel.onComplete(context) }
                )
                Box(
                    modifier = Modifier
                        .size(1.dp, 12.dp)
                        .background(Color(0xffC1C1C1))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Image(
                    painter = painterResource(id = R.mipmap.module_pdf_edit_icon_bc),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .size(16.dp)
                        .noRippleClickable { viewModel.onSave(context) }
                )
                Image(
                    painter = painterResource(id = R.mipmap.module_pdf_edit_icon_syb_sel),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .size(16.dp)
                        .noRippleClickable { viewModel.onRevoke() }
                )
                Image(
                    painter = painterResource(revokeImage),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .size(16.dp)
                        .size(16.dp)
                        .noRippleClickable { viewModel.onRecover() }
                )
            }

            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .fillMaxSize()
                    .weight(1f)
                    .onSizeChanged { viewModel.setStickersVisibleArea(it) }) {
                pdfView?.let { view ->
                    key(view) { // 当pdfView变化时触发重组
                        AndroidView(
                            factory = { context ->
                                val frameLayout = FrameLayout(context)
                                val lp = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                frameLayout.removeAllViews() // 先清除旧视图
                                frameLayout.addView(view, lp)
                                frameLayout
                            }, modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                //文字贴纸
                when (curSticker) {
                    is TextSticker -> {
                        StickerMaskLayer(stickerBox = {
                            TextStickerBox(
                                sticker = curSticker,
                                onDelete = { viewModel.onDeleteCurSticker() },
                                onUpdateOffset = viewModel::updateTextStickerOffset,
                                onUpdateFocus = viewModel::updateTextStickerFocus,
                                onUpdateRotationAndScale = viewModel::updateTextStickerRotationAndScale,
                                onUpdateValue = viewModel::updateTextStickerText,
                                onUpdateLineBreak = viewModel::updateTextStickerLineBreaks,
                            )
                        }, onTap = {
                            viewModel.unSelectedStatusAndDrawToPreview()
                        })
                    }

                    is ImageSticker -> {
                        StickerMaskLayer(stickerBox = {
                            ImageStickerBox(
                                sticker = curSticker,
                                onCopy = { viewModel.onCopy() },
                                onDelete = { viewModel.onDeleteCurSticker() },
                                onUpdateOffset = viewModel::updateImageStickerOffset,
                                onUpdateRotationAndScale = viewModel::updateImageStickerRotationAndScale,
                            )
                        }, onTap = {
                            viewModel.unSelectedStatusAndDrawToPreview()
                        })
                    }

                    else -> {}
                }
            }

            BottomMenuBar(editMenus) {
                BottomItemMenuBar(
                    name = it.name,
                    resId = when (clickMenuType) {
                        EditMenuType.PAGE_MANAGEMENT -> it.norResId
                        else -> if (it.name == clickMenuType.name) it.selResId else it.norResId
                    },
                    modifier = Modifier.noRippleClickable { clickEditMenu(it) })
            }
        }

        //插入文字底部导航
        (curSticker as? TextSticker)?.let {
            BottomStyleMenuBar(
                sticker = it,
                onClickKeyboard = { viewModel.updateTextStickerFocus(true) },
                onClickTextStyle = { showTextStyleBottomSheet = true },
                onSwitchFontItem = { showFontSelectBottomSheet = true },
                onSwitchFontWeight = viewModel::updateTextStickerFontWeight,
                onSelectedColor = { showTextColorBottomSheet = true },
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            if (showTextColorBottomSheet) {
                TextColorSelectionPanel(
                    selColor = it.color,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 65.dp, end = 15.dp)
                ) {
                    showTextColorBottomSheet = false
                    viewModel.updateTextStickerColor(it)
                }
            }
        }
    }


    if (showExitEditPromptDialog) {
        ExitEditPromptDialog(onDismiss = {
            viewModel.onDialogNotSave(context)
        }, onSave = {
            viewModel.onDialogSave(context)
        })
    }

    if (showTextStyleBottomSheet) {
        (curSticker as? TextSticker)?.let {
            TextStyleBottomSheet(
                sticker = it,
                onDismiss = { showTextStyleBottomSheet = false },
                onSelectFont = { showFontSelectBottomSheet = true },
                onUpdateStyle = { fontItem, textUnit, fontWeight, color ->
                    viewModel.updateTextStickerStyle(fontItem, textUnit, fontWeight, color)
                })
        }
    }

    if (showFontSelectBottomSheet) {
        (curSticker as? TextSticker)?.let {
            FontSelectBottomSheet(
                sticker = it,
                onDismiss = { showFontSelectBottomSheet = false },
                onUpdateStyle = viewModel::updateTextStickerFontItem,
            )
        }
    }

    if (showLoadingDialog) {
        LoadingDialog("保存中...")
    }
}