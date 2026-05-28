package com.yung.module_pdf.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blankj.utilcode.util.ToastUtils
import com.yung.module_pdf.db.FileInfoEntity
import com.yung.module_pdf.R
import com.yung.module_pdf.common.BookmarkBottomSheet
import com.yung.module_pdf.common.BottomItemMenuBar
import com.yung.module_pdf.common.BottomMenuBar
import com.yung.module_pdf.common.LoadingDialog
import com.yung.module_pdf.common.PreviewMenuType
import com.yung.module_pdf.common.StickerMaskLayer
import com.yung.module_pdf.common.WatermarkBox
import com.yung.module_pdf.common.WatermarkTextStyleBox
import com.yung.module_pdf.common.getCusParcelableExtra
import com.yung.module_pdf.common.noRippleClickable
import com.yung.module_pdf.common.previewMenus
import com.yung.module_pdf.model.PdfPreviewModel
import kotlinx.coroutines.flow.asStateFlow

class PdfPreviewActivity : FragmentActivity() {
    companion object {
        const val KEY_ENTITY = "KEY_ENTITY"

        @JvmStatic
        fun start(context: Context, entity: FileInfoEntity) {
            val starter = Intent(context, PdfPreviewActivity::class.java)
                .putExtra(KEY_ENTITY, entity)
            context.startActivity(starter)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val entity = intent?.getCusParcelableExtra(KEY_ENTITY, FileInfoEntity::class.java)
        setContent { PdfPreviewScreen(entity) }
    }
}

@Preview
@Composable
private fun PdfPreviewScreen(
    entity: FileInfoEntity? = null,
    viewModel: PdfPreviewModel = viewModel(),
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val bookmarks by viewModel.bookmarks.asStateFlow().collectAsState()
    val pdfView by viewModel.pdfView.asStateFlow().collectAsState()
    val curTextStickerNew by viewModel.curTextStickerNew.asStateFlow().collectAsState()
    val showLoadingDialog by viewModel.showLoadingDialog.asStateFlow().collectAsState()
    val showBookmarkBottomSheet by viewModel.showBookmarkBottomSheet.asStateFlow().collectAsState()

    fun clickPreviewMenu(type: PreviewMenuType) {
        when (type) {
            PreviewMenuType.CATALOGUE -> {
                if (bookmarks.isEmpty()) {
                    ToastUtils.showShort("没有目录列表")
                } else {
                    viewModel.switchBookmarkBottomSheet(true)
                }
            }

            PreviewMenuType.EDIT -> {
                    viewModel.enterEditorPage(context)
            }

            PreviewMenuType.WATERMARK -> {
                    viewModel.addWatermarkBox()
            }

            PreviewMenuType.SHARE -> viewModel.sharePdfFile(context)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.insertPDFFile(entity?.path)
    }

    LaunchedEffect(Unit) {
        viewModel.createPDFView(context, entity)
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
                Box(modifier = Modifier.width(40.dp), contentAlignment = Alignment.Center) {
                    Image(painter = painterResource(id = R.mipmap.module_pdf_edit_btn_back),
                        contentDescription = null,
                        modifier = Modifier.noRippleClickable { activity?.finish() })
                }
                Text(
                    text = entity?.name ?: "标题",
                    color = Color(0xff252525),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                Spacer(modifier = Modifier.width(40.dp))
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

                //水印贴纸
                if (curTextStickerNew != null) {
                    StickerMaskLayer(stickerBox = {
                        WatermarkBox(
                            sticker = curTextStickerNew!!,
                            onUpdateOffset = {
                                viewModel.onUpdateTextSticker(curTextStickerNew?.copy(offset = it))
                            },
                            onUpdateRotationAndScale = { rotation, scale ->
                                viewModel.onUpdateTextSticker(
                                    curTextStickerNew?.copy(
                                        rotation = rotation,
                                        scaleRatio = scale
                                    )
                                )
                            },
                            onUpdateValue = {
                                viewModel.onUpdateTextSticker(curTextStickerNew?.copy(text = it))
                            },
                            onUpdateLineBreak = {
                                viewModel.onUpdateTextSticker(
                                    curTextStickerNew?.copy(textLineBreaks = it)
                                )
                            },
                        )
                    })
                }
            }

            BottomMenuBar(previewMenus) {
                BottomItemMenuBar(name = it.name,
                    resId = it.resId,
                    modifier = Modifier.noRippleClickable { clickPreviewMenu(it) })
            }
        }

        //添加水印
        curTextStickerNew?.let {
            WatermarkTextStyleBox(sticker = it,
                modifier = Modifier.align(Alignment.BottomCenter),
                onDismiss = {
                    viewModel.closeUpdatedTextStickerStyle()
                },
                onComplete = { switch, color, fontSize ->
                    viewModel.submitUpdatedTextStickerStyle(switch, color, fontSize)
                },
                onUpdateData = { switch, color, fontSize ->
                    viewModel.onUpdateTextSticker(
                        curTextStickerNew?.copy(
                            switch = switch, color = color, fontSize = fontSize
                        )
                    )
                })
        }

    }

    if (showBookmarkBottomSheet) {
        BookmarkBottomSheet(bookmarks) {
            viewModel.switchBookmarkBottomSheet(false)
        }
    }

    if (showLoadingDialog) {
        LoadingDialog("保存中...")
    }

}