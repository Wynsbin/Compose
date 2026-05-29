package com.yung.module_pdf.internal.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blankj.utilcode.util.ActivityUtils.startActivityForResult
import com.yung.module_pdf.internal.core.event.PdfEvent
import com.yung.module_pdf.R
import com.yung.module_pdf.internal.core.event.EventBusListener
import com.yung.module_pdf.internal.ui.component.FileMoreManagerDialog
import com.yung.module_pdf.internal.ui.component.PDFSelectType
import com.yung.module_pdf.internal.core.event.PdfEventBusConstants
import com.yung.module_pdf.internal.ui.component.PdfListItem
import com.yung.module_pdf.api.PdfSelectMode
import com.yung.module_pdf.internal.ui.component.noRippleClickable
import com.yung.module_pdf.internal.ui.viewmodel.CollectPdfPreviewSideEffects
import com.yung.module_pdf.internal.ui.viewmodel.FileManagerViewModel
import com.yung.module_pdf.internal.ui.viewmodel.PdfPreviewViewModel
import com.yung.module_pdf.internal.core.permission.PermissionUseCase
import org.orbitmvi.orbit.compose.collectAsState
import kotlinx.coroutines.launch


class PdfSelectActivity : FragmentActivity() {
    companion object {
        const val KEY_MODE = "mode"

        @JvmStatic
        fun start(context: Context, mode: PdfSelectMode) {

            PermissionUseCase.useStorageManager(context as FragmentActivity, allow = {
                val starter = Intent(context, PdfSelectActivity::class.java)
                    .putExtra(KEY_MODE, mode)
                context.startActivity(starter)
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val mode =
            (intent?.getSerializableExtra(KEY_MODE) as? PdfSelectMode) ?: PdfSelectMode.PREVIEW
        setContent {
            PdfSelectScreen(mode)
        }
    }
}


@Preview
@Composable
private fun PdfSelectScreen(
    pdfSelectMode: PdfSelectMode = PdfSelectMode.PREVIEW,
    vm: PdfPreviewViewModel = viewModel(),
    fileManagerViewModel: FileManagerViewModel = viewModel(),
) {
    CollectPdfPreviewSideEffects(vm)
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val previewState by vm.collectAsState()
    val pdfLocalList = previewState.pdfLocalList
    val pdfRecentList = previewState.pdfRecentList

    val tabTypes = mutableListOf(PDFSelectType.LOCAL, PDFSelectType.RECENT)

    val pagerState = rememberPagerState(pageCount = { tabTypes.size })
    val scope = rememberCoroutineScope()

    LifecycleResumeEffect(Unit) {
        val act = activity
        if (act != null && !PermissionUseCase.hasStorageAccess(act)) {
            PermissionUseCase.useStorageManager(
                activity = act,
                allow = {
                    vm.loadLocalPdfList()
                    vm.loadRecentPdfList()
                },
            )
        } else {
            vm.loadLocalPdfList()
            vm.loadRecentPdfList()
        }
        onPauseOrDispose { }
    }

    EventBusListener(PdfEvent::class.java) { eventBusEntity ->
        if (eventBusEntity.code == PdfEventBusConstants.REFRESH_SELECT_FILE_LIST) {
            vm.loadLocalPdfList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .height(44.dp)
                .padding(horizontal = 20.dp),
        ) {
            Image(
                painter = painterResource(id = R.mipmap.module_pdf_edit_btn_back),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .noRippleClickable { activity?.finish() }
            )

            Text(
                text = "选择文件",
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )

            Image(
                painter = painterResource(id = R.mipmap.module_pdf_edit_xzwj_icon_ss),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .noRippleClickable { PdfSearchActivity.start(context) }
            )
        }

        HorizontalDivider(color = Color(0xffF2F3F5))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(Color(0xffF8F9FB)),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            tabTypes.forEachIndexed { index, item ->
                Column(
                    modifier = Modifier
                        .align(Alignment.Bottom)
                        .noRippleClickable {
                            scope.launch {
                                pagerState.scrollToPage(index)
                            }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = item.type,
                        color = if (pagerState.currentPage == index) Color(0xff252525) else Color(
                            0xff999999
                        ),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Box(
                        modifier = Modifier
                            .size(16.dp, 4.dp)
                            .background(
                                if (pagerState.currentPage == index) Color(0xffF5341A) else Color.Transparent,
                                CircleShape
                            )
                    )
                }
            }
        }

        HorizontalDivider(color = Color(0xffF2F3F5))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            val list = if (pagerState.currentPage == 0) {
                pdfLocalList
            } else {
                pdfRecentList
            }
            if (list.isEmpty()) {
                EmptyFileList()
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(items = list) {
                        PdfListItem(entity = it, onClickFile = {
                            fileManagerViewModel.openFile(context, it, pdfSelectMode)
                        }, onClickMore = {
                            fileManagerViewModel.switchFileMoreBottomSheet(true)
                            fileManagerViewModel.setFileMoreEntity(it)
                        })
                    }
                }
            }
        }
    }


    FileMoreManagerDialog(context, fileManagerViewModel)
}
