package com.yung.module_pdf.internal.core.permission

data class PermissionParis(
    val name: String,
    val description: String,
    val res: Int,
    val list: List<String>,
    val requestTip: String = description,
)
