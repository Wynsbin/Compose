package com.yung.module_pdf.internal.ui.component

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yung.module_pdf.internal.db.FileInfoEntity
import com.yung.module_pdf.R
import com.yung.module_pdf.internal.core.ext.formatFileSize
import com.yung.module_pdf.internal.core.ext.formatFileTime
import com.yung.module_pdf.internal.core.ext.getResId
import com.yung.module_pdf.internal.ui.viewmodel.CollectFileManagerSideEffects
import com.yung.module_pdf.internal.ui.viewmodel.FileManagerViewModel
import org.orbitmvi.orbit.compose.collectAsState

fun Modifier.noRippleClickable(enabled: Boolean = true, onClick: () -> Unit): Modifier =
    composed {
        this.clickable(
            enabled = enabled,
            interactionSource = remember {
                MutableInteractionSource()
            },
            indication = null
        ) { onClick() }
    }

@Composable
fun PdfListItem(
    entity: FileInfoEntity,
    onClickFile: (FileInfoEntity) -> Unit,
    onClickMore: (FileInfoEntity) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp)
            .noRippleClickable { onClickFile(entity) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(entity.format.getResId()),
            contentDescription = null,
            modifier = Modifier.size(32.dp)
        )
        Column(
            modifier = Modifier
                .padding(start = 12.dp)
                .fillMaxWidth()
                .weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = entity.name,
                        color = Color(0xff252525),
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${formatFileTime(entity.time)}丨${formatFileSize(entity.size)}",
                        color = Color(0xffB4B4B4),
                        fontSize = 10.sp
                    )
                }
                Image(
                    painter = painterResource(id = R.mipmap.module_pdf_edit_icon_gd),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 20.dp)
                        .noRippleClickable { onClickMore(entity) }
                )
            }
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp),
                color = Color(0xffF2F3F5)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CusEditView(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = TextFieldDefaults.shape,
    colors: TextFieldColors = TextFieldDefaults.colors(
        focusedPlaceholderColor = Color(0xff766560),
        unfocusedPlaceholderColor = Color(0xff766560),
        disabledPlaceholderColor = Color(0xff766560),
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
    ),
    horizontalPaddingValues: Dp = 0.dp,
    verticalPaddingValues: Dp = 0.dp,
    textStyle: TextStyle = TextStyle.Default,
) {
    BasicTextField(
        value = value,
        modifier = modifier
            .defaultMinSize(
                minWidth = TextFieldDefaults.MinWidth,
                minHeight = TextFieldDefaults.MinHeight,
            ),
        textStyle = textStyle,
        onValueChange = onValueChange,
        enabled = enabled,
        readOnly = readOnly,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        interactionSource = interactionSource,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        decorationBox = @Composable { innerTextField ->
            TextFieldDefaults.DecorationBox(
                value = value,
                visualTransformation = visualTransformation,
                innerTextField = innerTextField,
                placeholder = placeholder,
                label = label,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                prefix = prefix,
                suffix = suffix,
                supportingText = supportingText,
                shape = shape,
                singleLine = singleLine,
                enabled = enabled,
                isError = isError,
                interactionSource = interactionSource,
                colors = colors,
                contentPadding = PaddingValues(
                    horizontal = horizontalPaddingValues,
                    vertical = verticalPaddingValues
                )
            )
        }
    )
}


@Composable
fun FileMoreManagerDialog(context: Context, fileManagerViewModel: FileManagerViewModel) {
    CollectFileManagerSideEffects(fileManagerViewModel)
    val state by fileManagerViewModel.collectAsState()

    if (state.showFileMoreBottomSheet) {
        FileMoreBottomSheet(entity = state.moreFileInfoEntity,
            onDismiss = { fileManagerViewModel.switchFileMoreBottomSheet(false) },
            onMoreEvent = { type, entity ->
                fileManagerViewModel.onFileMoreEvent(context, type, entity)
                fileManagerViewModel.switchFileMoreBottomSheet(false)
            })
    }

    if (state.showFileRenameDialog) {
        FileRenameDialog(onDismiss = { fileManagerViewModel.switchFileRenameDialog(false) },
            onComplete = {
                fileManagerViewModel.onRenameFile(it)
                fileManagerViewModel.switchFileRenameDialog(false)
            })
    }

    if (state.showFileDeleteDialog) {
        FileDeleteDialog(
            onDismiss = { fileManagerViewModel.switchFileDeleteDialog(false) },
            onDelete = {
                fileManagerViewModel.onDeleteFile()
                fileManagerViewModel.switchFileDeleteDialog(false)
            })
    }
}