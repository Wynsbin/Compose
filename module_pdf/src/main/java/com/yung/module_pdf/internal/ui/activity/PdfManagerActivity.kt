package com.yung.module_pdf.internal.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.yung.module_pdf.R
import com.yung.module_pdf.internal.ui.component.BottomItemMenuBar
import com.yung.module_pdf.internal.ui.component.BottomMenuBar
import com.yung.module_pdf.internal.ui.component.DeletePagePromptDialog
import com.yung.module_pdf.internal.ui.component.ExitEditPromptDialog
import com.yung.module_pdf.internal.ui.component.LoadingDialog
import com.yung.module_pdf.internal.ui.component.ManagerType
import com.yung.module_pdf.internal.ui.component.managerMenus
import com.yung.module_pdf.internal.ui.component.noRippleClickable
import com.yung.module_pdf.internal.ui.viewmodel.CollectPdfManagerSideEffects
import com.yung.module_pdf.internal.ui.viewmodel.PdfManagerViewModel
import com.yung.module_pdf.internal.domain.PdfItemInfo
import org.orbitmvi.orbit.compose.collectAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.ui.unit.IntOffset
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyGridState
import org.burnoutcrew.reorderable.reorderable
import java.io.File

private val PdfReorderPlacementSpec = spring(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessLow,
    visibilityThreshold = IntOffset.VisibilityThreshold,
)

class PdfManagerActivity : FragmentActivity() {
    companion object {
        const val KEY_FILE = "KEY_FILE"

        @JvmStatic
        fun start(context: Context, file: File) {
            val starter = Intent(context, PdfManagerActivity::class.java).putExtra(KEY_FILE, file)
            context.startActivity(starter)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val pdfFile = intent?.getSerializableExtra(PdfEditorActivity.KEY_FILE) as? File
        setContent {
            PdfManagerScreen(pdfFile)
        }
    }
}

@Preview
@Composable
private fun PdfManagerScreen(pdfFile: File? = null, viewModel: PdfManagerViewModel = viewModel()) {
    CollectPdfManagerSideEffects(viewModel)
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val uiState by viewModel.collectAsState()
    val isEditModel = uiState.isEditModel
    val itemBoxSize = uiState.itemBoxSize
    val pdfItemInfoList = uiState.pdfItemInfoList
    val pdfItemInfoSelectedList = uiState.pdfItemInfoSelectedList
    val showLoadingDialog = uiState.showLoadingDialog
    val showLoadingSaveDialog = uiState.showLoadingSaveDialog
    val showDeleteDialog = uiState.showDeleteDialog
    val showExitEditPromptDialog = uiState.showExitEditPromptDialog

    // 拖拽排序需同步更新列表；经 Orbit intent 异步写回会导致 onMove 与 LazyGrid 布局不同步
    var localPdfItems by remember { mutableStateOf(pdfItemInfoList) }
    val gridState = rememberLazyGridState()
    val syncOrderToViewModel by rememberUpdatedState<(List<PdfItemInfo>) -> Unit> {
        { viewModel.updatePdfItemOrder(it) }
    }

    val reorderState = rememberReorderableLazyGridState(
        gridState = gridState,
        onMove = { from, to ->
            localPdfItems = localPdfItems.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
        },
        onDragEnd = { _, _ ->
            syncOrderToViewModel(localPdfItems)
        },
    )

    // 仅在外部数据变化时同步；不要把 draggingItemKey 作为 key，否则松手时会用旧 VM 列表覆盖本地排序结果
    LaunchedEffect(pdfItemInfoList) {
        if (reorderState.draggingItemKey == null) {
            val vmById = pdfItemInfoList.associateBy { it.id }
            val localIds = localPdfItems.map { it.id }
            val vmIds = pdfItemInfoList.map { it.id }
            localPdfItems = when {
                localIds == vmIds -> pdfItemInfoList
                localIds.toSet() == vmIds.toSet() -> localPdfItems.map { vmById[it.id] ?: it }
                else -> pdfItemInfoList
            }
        }
    }

    val currentLocalItems by rememberUpdatedState(localPdfItems)

    fun clickEditMenu(type: ManagerType) {
        when (type) {
            ManagerType.ADD -> viewModel.addPage(currentLocalItems)
            ManagerType.DELETE -> viewModel.onDelete()
            ManagerType.ROTATE -> viewModel.rotatePage(currentLocalItems)
            ManagerType.SELECT_ALL -> viewModel.selectAll(currentLocalItems)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.insertPDFFile(pdfFile?.path)
    }

    LaunchedEffect(Unit) {
        viewModel.analysisPdf(pdfFile)
    }

    BackHandler {
        viewModel.exitPageManager()
    }

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
                Image(
                    painter = painterResource(id = R.mipmap.module_pdf_edit_btn_back),
                    contentDescription = null,
                    modifier = Modifier.noRippleClickable { viewModel.exitPageManager() })
            }
            Text(
                text = if (isEditModel) "已选择${pdfItemInfoSelectedList.size}页" else "页面管理",
                color = if (!isEditModel || pdfItemInfoSelectedList.isNotEmpty()) Color(0xff252525)
                else Color(0xff999999),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Text(
                text = if (isEditModel) "保存" else "编辑",
                color = if (isEditModel) Color.White else Color(0xff252525),
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(end = 20.dp)
                    .size(46.dp, 28.dp)
                    .background(
                        if (isEditModel) Color(0xffF5341A) else Color(0xffF7F7F7),
                        RoundedCornerShape(4.dp)
                    )
                    .noRippleClickable { viewModel.enterEditMode(currentLocalItems) }
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .wrapContentHeight(Alignment.CenterVertically))
        }

        if (isEditModel) {
            Text(
                text = "长按拖动可调整顺序",
                color = Color(0xff999999),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp, 5.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(10.dp))
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = gridState,
            contentPadding = PaddingValues(20.dp),
            horizontalArrangement = Arrangement.spacedBy(11.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .reorderable(reorderState)
                .fillMaxSize()
                .weight(1f)
                .background(Color.White),
        ) {
            items(items = localPdfItems, key = { it.id }) { item ->
                ReorderableItem(
                    reorderState,
                    key = item.id,
                    defaultDraggingModifier = Modifier.then(
                        if (isEditModel) {
                            Modifier.animateItem(
                                placementSpec = PdfReorderPlacementSpec,
                                fadeInSpec = null,
                                fadeOutSpec = null,
                            )
                        } else {
                            Modifier
                        },
                    )
                ) { isDragging ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(itemBoxSize.width.toFloat() / itemBoxSize.height)
                                .border(
                                    1.dp,
                                    if (pdfItemInfoSelectedList.any { it.id == item.id })
                                        Color(0xffFFB6AC) else Color(0xffE9E9E9),
                                    RoundedCornerShape(10.dp)
                                )
                                .clip(RoundedCornerShape(10.dp))
                                .then(
                                    if (isEditModel) {
                                        Modifier.detectReorderAfterLongPress(reorderState)
                                    } else {
                                        Modifier
                                    }
                                )
                        ) {
                            AsyncImage(
                                model = item.bitmap,
                                contentScale = ContentScale.Crop,
                                contentDescription = null,
                                modifier = Modifier
                                    .rotate(item.rotationAngle.toFloat())
                                    .padding(8.dp)
                                    .fillMaxSize(),
                            )
                            if (isEditModel) {
                                Image(
                                    painter = painterResource(
                                        if (pdfItemInfoSelectedList.any { it == item }) R.mipmap.module_pdf_edit_icon_xz_sel
                                        else R.mipmap.module_pdf_edit_icon_xz_nor
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .align(Alignment.TopEnd)
                                        .noRippleClickable { viewModel.addToSelectedList(item) }
                                )
                            }
                        }

                        Text(
                            text = "${localPdfItems.indexOfFirst { it.id == item.id }.plus(1)}",
                            color = Color(0xff252525),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .size(48.dp, 24.dp)
                                .background(Color(0xffF7F7F7), RoundedCornerShape(4.dp))
                                .wrapContentHeight(Alignment.CenterVertically)
                                .wrapContentWidth(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = Color(0xffE9E9E9))

        BottomMenuBar(managerMenus) {
            BottomItemMenuBar(
                name = it.name,
                resId = if (pdfItemInfoSelectedList.isEmpty()) it.norResId else it.selResId,
                modifier = Modifier.noRippleClickable { clickEditMenu(it) })
        }
    }


    if (showLoadingDialog) {
        LoadingDialog("解析中...")
    }

    if (showLoadingSaveDialog) {
        LoadingDialog("保存中...")
    }

    if (showDeleteDialog) {
        DeletePagePromptDialog(onDismiss = {
            viewModel.onDialogUnDeletePage()
        }, onDelete = {
            viewModel.onDialogDeletePage(currentLocalItems)
        })
    }

    if (showExitEditPromptDialog) {
        ExitEditPromptDialog(onDismiss = {
            viewModel.onDialogNotSave()
        }, onSave = {
            viewModel.onDialogSave(currentLocalItems)
        })
    }
}