package com.yung.module_pdf.internal.core.pdf

import android.graphics.RectF
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set


class StickerClickHandler {
    // 存储所有可点击区域（按页面分组）
    private val clickableRegions = mutableMapOf<Long, RegionInfo>()

    // 添加水印点击区域
    fun addClickableRegion(page: Int, stickerId: Long, region: RectF) {
        clickableRegions[stickerId] = RegionInfo(page, region)
    }

    // 检测点击事件
    fun handleTap(page: Int, x: Float, y: Float): Long {
        clickableRegions.filter { it.value.page == page }
            .forEach { (stickerId, regionInfo) ->
                // regionInfo 现在包含原始矩形和变换矩阵
                if (regionInfo.rect.contains(x, y)) {
                    return stickerId
                }
            }
        return 0L
    }

    // 清除指定页面的点击区域
    fun clearClickableRegion(stickerId: Long) {
        clickableRegions.remove(stickerId)
    }

    // 清除所有点击区域
    fun clearAll() {
        clickableRegions.clear()
    }
}


// 修改添加可点击区域的方式
data class RegionInfo(val page: Int, val rect: RectF)
