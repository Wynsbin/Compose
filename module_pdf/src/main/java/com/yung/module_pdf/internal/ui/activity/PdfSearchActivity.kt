package com.yung.module_pdf.internal.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yung.module_pdf.internal.core.ext.SoftKeyBoardListener
import com.yung.module_pdf.internal.core.ext.SoftKeyBoardListener.OnSoftKeyBoardChangeListener
import com.yung.module_pdf.R
import com.yung.module_pdf.internal.ui.component.CusEditView
import com.yung.module_pdf.internal.ui.component.FileMoreManagerDialog
import com.yung.module_pdf.internal.ui.component.PdfListItem
import com.yung.module_pdf.internal.ui.component.noRippleClickable
import com.yung.module_pdf.internal.ui.viewmodel.CollectPdfPreviewSideEffects
import com.yung.module_pdf.internal.ui.viewmodel.FileManagerViewModel
import com.yung.module_pdf.internal.ui.viewmodel.PdfPreviewViewModel
import org.orbitmvi.orbit.compose.collectAsState

class PdfSearchActivity : FragmentActivity() {
    companion object {
        @JvmStatic
        fun start(context: Context) {
            val starter = Intent(context, PdfSearchActivity::class.java)
            context.startActivity(starter)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PdfSearchScreen()
        }
    }
}

@Preview
@Composable
private fun PdfSearchScreen(
    vm: PdfPreviewViewModel = viewModel(),
    fileManagerViewModel: FileManagerViewModel = viewModel(),
) {
    CollectPdfPreviewSideEffects(vm)
    val context = LocalContext.current
    val activity = context as FragmentActivity
    val previewState by vm.collectAsState()
    val searchKeyword = previewState.searchKeyword
    val pdfList = previewState.pdfLocalList

    val focusManager = LocalFocusManager.current
    val softKeyBoard = LocalSoftwareKeyboardController.current

    SoftKeyBoardListener.setListener(activity, object : OnSoftKeyBoardChangeListener {
        override fun keyBoardShow(p0: Int) {
        }

        override fun keyBoardHide() {
            focusManager.clearFocus()
            vm.searchPdfList()
        }
    })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .height(44.dp)
                .padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .padding(vertical = 5.dp)
                    .weight(1f)
                    .background(Color(0xffF3F4F7), CircleShape)
                    .border(1.dp, Color(0xffF2F3F5), CircleShape)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.module_pdf_edit_icon_search),
                    contentDescription = null
                )
                CusEditView(
                    value = searchKeyword,
                    onValueChange = { vm.setSearchKeyWord(it) },
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .weight(1f),
                    textStyle = TextStyle.Default.copy(
                        color = Color(0xFF333333),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    placeholder = {
                        Text(
                            text = "请输入文件名称",
                            color = Color(0xFF999999),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        softKeyBoard?.hide()
                    })
                )
                Image(painter = painterResource(id = R.mipmap.module_pdf_edit_icon_qx),
                    contentDescription = null,
                    modifier = Modifier.noRippleClickable { vm.setSearchKeyWord("") })
            }

            Text(
                text = "取消",
                color = Color(0xff666666),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .padding(start = 18.dp)
                    .noRippleClickable { activity?.finish() }
            )
        }
        HorizontalDivider(color = Color(0xffF2F3F5))

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            if (pdfList.isEmpty() && searchKeyword.isNotEmpty()) {
                EmptyFileList()
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(items = pdfList) {
                        PdfListItem(entity = it, onClickFile = {
                            fileManagerViewModel.openFile(context, it)
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

@Composable
fun EmptyFileList() {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.mipmap.module_pdf_edit_img_kzt),
            contentDescription = null
        )
        Text(
            text = "没有找到相关文件",
            color = Color(0xff999999),
            fontSize = 16.sp, fontWeight = FontWeight.Medium
        )
    }
}