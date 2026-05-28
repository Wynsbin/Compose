package com.yung.module_pdf.activity

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
import com.yung.module_pdf.common.PdfEvent
import com.yung.module_pdf.R
import com.yung.module_pdf.common.BottomItemMenuBar
import com.yung.module_pdf.common.BottomMenuBar
import com.yung.module_pdf.common.BottomStyleMenuBar
import com.yung.module_pdf.common.EditMenuType
import com.yung.module_pdf.common.EventBusListener
import com.yung.module_pdf.common.ExitEditPromptDialog
import com.yung.module_pdf.common.FontSelectBottomSheet
import com.yung.module_pdf.common.ImageSticker
import com.yung.module_pdf.common.ImageStickerBox
import com.yung.module_pdf.common.LoadingDialog
import com.yung.module_pdf.common.PdfEventBusConstants
import com.yung.module_pdf.common.StickerMaskLayer
import com.yung.module_pdf.common.TextColorSelectionPanel
import com.yung.module_pdf.common.TextSticker
import com.yung.module_pdf.common.TextStickerBox
import com.yung.module_pdf.common.TextStyleBottomSheet
import com.yung.module_pdf.common.editMenus
import com.yung.module_pdf.common.noRippleClickable
import com.yung.module_pdf.model.PdfEditorModel
import com.yung.module_pdf.utils.ImagePickHelper
import kotlinx.coroutines.flow.asStateFlow
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
private fun PdfEditorScreen(pdfFile: File? = null, viewModel: PdfEditorModel = viewModel()) {

    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val pdfView by viewModel.pdfView.asStateFlow().collectAsState()
    val clickMenuType by viewModel.clickMenuType.asStateFlow().collectAsState()
    val curSticker by viewModel.curSticker.asStateFlow().collectAsState()
    val revokeImage by viewModel.revokeImage.asStateFlow().collectAsState()
    val showLoadingDialog by viewModel.showLoadingDialog.asStateFlow().collectAsState()
    val showExitEditPromptDialog by viewModel.showExitEditPromptDialog.asStateFlow()
        .collectAsState()
    var showFontSelectBottomSheet by remember { mutableStateOf(false) }
    var showTextStyleBottomSheet by remember { mutableStateOf(false) }
    var showTextColorBottomSheet by remember { mutableStateOf(false) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
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
                pickImageLauncher.launch("image/*")
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

            Box(modifier = Modifier
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
                                sticker = curSticker as TextSticker,
                                onDelete = { viewModel.onDeleteCurSticker() },
                                onUpdateOffset = {
                                    viewModel.onUpdateTextSticker(
                                        (curSticker as? TextSticker)?.copy(offset = it)
                                    )
                                },
                                onUpdateFocus = {
                                    viewModel.onUpdateTextSticker(
                                        (curSticker as? TextSticker)?.copy(focus = it)
                                    )
                                },
                                onUpdateRotationAndScale = { rotation, scale ->
                                    viewModel.onUpdateTextSticker(
                                        (curSticker as? TextSticker)?.copy(
                                            rotation = rotation,
                                            scaleRatio = scale
                                        )
                                    )
                                },
                                onUpdateValue = {
                                    viewModel.onUpdateTextSticker(
                                        (curSticker as? TextSticker)?.copy(text = it)
                                    )
                                },
                                onUpdateLineBreak = {
                                    viewModel.onUpdateTextSticker(
                                        (curSticker as? TextSticker)?.copy(textLineBreaks = it)
                                    )
                                },
                            )
                        }, onTap = {
                            viewModel.unSelectedStatusAndDrawToPreview()
                        })
                    }

                    is ImageSticker -> {
                        StickerMaskLayer(stickerBox = {
                            ImageStickerBox(
                                sticker = (curSticker as ImageSticker),
                                onCopy = { viewModel.onCopy() },
                                onDelete = { viewModel.onDeleteCurSticker() },
                                onUpdateOffset = {
                                    viewModel.onUpdateImageSticker(
                                        (curSticker as? ImageSticker)?.copy(
                                            offset = it
                                        )
                                    )
                                },
                                onUpdateRotationAndScale = { rotation, scale ->
                                    viewModel.onUpdateImageSticker(
                                        (curSticker as? ImageSticker)?.copy(
                                            rotation = rotation,
                                            scaleRatio = scale
                                        )
                                    )
                                },
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
                onClickKeyboard = {
                    viewModel.onUpdateTextSticker(
                        (curSticker as? TextSticker)?.copy(focus = true)
                    )
                },
                onClickTextStyle = { showTextStyleBottomSheet = true },
                onSwitchFontItem = { showFontSelectBottomSheet = true },
                onSwitchFontWeight = {
                    viewModel.onUpdateTextSticker(
                        (curSticker as? TextSticker)?.copy(
                            fontWeight = it
                        )
                    )
                },
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
                    viewModel.onUpdateTextSticker((curSticker as? TextSticker)?.copy(color = it))
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
            TextStyleBottomSheet(sticker = it,
                onDismiss = { showTextStyleBottomSheet = false },
                onSelectFont = { showFontSelectBottomSheet = true },
                onUpdateStyle = { fontItem, textUnit, fontWeight, color ->
                    viewModel.onUpdateTextSticker(
                        (curSticker as? TextSticker)?.copy(
                            fontItem = fontItem,
                            fontSize = textUnit,
                            fontWeight = fontWeight,
                            color = color
                        )
                    )
                })
        }
    }

    if (showFontSelectBottomSheet) {
        (curSticker as? TextSticker)?.let {
            FontSelectBottomSheet(sticker = it,
                onDismiss = { showFontSelectBottomSheet = false },
                onUpdateStyle = {
                    viewModel.onUpdateTextSticker((curSticker as? TextSticker)?.copy(fontItem = it))
                })
        }
    }

    if (showLoadingDialog) {
        LoadingDialog("保存中...")
    }
}