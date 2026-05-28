package com.yung.module_pdf.common

import com.yung.module_pdf.R

sealed class PreviewMenuType(val name: String, val resId: Int) {
    data object CATALOGUE : PreviewMenuType("目录", R.mipmap.module_pdf_edit_btn_ml)
    data object EDIT : PreviewMenuType("编辑", R.mipmap.module_pdf_edit_btn_bj)
    data object WATERMARK : PreviewMenuType("添加水印", R.mipmap.module_pdf_edit_btn_tjsy)
    data object SHARE : PreviewMenuType("分享", R.mipmap.module_pdf_edit_btn_fx)
}

sealed class EditMenuType(val name: String, var norResId: Int, var selResId: Int) {
    data object PAGE_MANAGEMENT : EditMenuType(
        "页面管理", R.mipmap.module_pdf_edit_btn_ymgl, R.mipmap.module_pdf_edit_btn_ymgl
    )

    data object INSERT_TEXT : EditMenuType(
        "插入文字", R.mipmap.module_pdf_edit_btn_crwz, R.mipmap.module_pdf_edit_btn_crwz_sel
    )

    data object INSERT_IMAGE : EditMenuType(
        "插入图片", R.mipmap.module_pdf_edit_btn_crtp, R.mipmap.module_pdf_edit_btn_crtp_sel
    )
}


sealed class ManagerType(val name: String, var norResId: Int, var selResId: Int) {
    data object SELECT_ALL : ManagerType(
        "全选", R.mipmap.module_pdf_edit_btn_quanxuan, R.mipmap.module_pdf_edit_btn_quanxuan
    )

    data object ADD : ManagerType(
        "添加", R.mipmap.module_pdf_edit_btn_tj_dis, R.mipmap.module_pdf_edit_btn_tj_sel
    )

    data object ROTATE : ManagerType(
        "旋转", R.mipmap.module_pdf_edit_btn_xz_dis, R.mipmap.module_pdf_edit_btn_xz_sel
    )

    data object DELETE : ManagerType(
        "删除", R.mipmap.module_pdf_edit_btn_sc_dis, R.mipmap.module_pdf_edit_btn_sc_sel
    )
}

val previewMenus = listOf(
    PreviewMenuType.CATALOGUE,
    PreviewMenuType.EDIT,
    PreviewMenuType.WATERMARK,
    PreviewMenuType.SHARE
)

val editMenus = listOf(
    EditMenuType.PAGE_MANAGEMENT,
    EditMenuType.INSERT_TEXT,
    EditMenuType.INSERT_IMAGE,
)

val managerMenus = listOf(
    ManagerType.SELECT_ALL,
    ManagerType.ADD,
    ManagerType.ROTATE,
    ManagerType.DELETE,
)

enum class PDFSelectType(val type: String) { LOCAL("本地文件"), RECENT("最近文件") }

enum class PdfSelectMode { PREVIEW, MANAGEMENT }

sealed class FileMenuType(val type: String, val resId: Int) {
    data object SHARE : FileMenuType("分享给好友", R.mipmap.module_pdf_edit_em_icon_fx)
    data object RENAME : FileMenuType("重命名", R.mipmap.module_pdf_edit_em_icon_cmm)
    data object DELETE : FileMenuType("删除文档", R.mipmap.module_pdf_edit_em_icon_scwd)
}


val fileMenus = listOf(FileMenuType.SHARE,FileMenuType.RENAME,FileMenuType.DELETE)
