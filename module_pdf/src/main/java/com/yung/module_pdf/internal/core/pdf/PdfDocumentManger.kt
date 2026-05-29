package com.yung.module_pdf.internal.core.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.pdf.PdfRenderer
import android.media.MediaScannerConnection
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.util.Log
import com.blankj.utilcode.util.ScreenUtils
import com.yung.module_pdf.api.PdfSdk
import com.yung.module_pdf.internal.core.ext.IdGen
import com.yung.module_pdf.internal.ui.sticker.Sticker
import com.yung.module_pdf.internal.domain.PdfItemInfo
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream.AppendMode
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject
import com.tom_roush.pdfbox.util.Matrix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * 使用com.tom-roush:pdfbox-android:2.0.27.0
 */
object PdfDocumentManger {

    private const val PDF_EDIT_DIR = "pdf"

    val SAVE_SUCCESS_TIPS = "已保存到${Environment.DIRECTORY_DOWNLOADS}/pdf文件夹"

    val pdfDir = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        PDF_EDIT_DIR
    )

    fun analysisPDPages(file: File?) = flow {
        runCatching {
            val list = arrayListOf<PdfItemInfo>()
            val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val pdfRenderer = PdfRenderer(pfd)
            val count = pdfRenderer.pageCount
            val width = ScreenUtils.getScreenWidth()
            for (i in 0 until count) {
                val page: PdfRenderer.Page = pdfRenderer.openPage(i)
                val height = width * page.height / page.width
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawColor(Color.WHITE)
                canvas.drawBitmap(bitmap, 0f, 0f, null)
                val r = Rect(0, 0, width, height)
                page.render(bitmap, r, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                val pdfItemInfo = PdfItemInfo(IdGen.next(), bitmap, width, height, page.index)
                list.add(pdfItemInfo)
                page.close()
            }
            pdfRenderer.close()
            emit(list)
        }.onFailure {
            throw it
        }
    }.flowOn(Dispatchers.IO)

    /**
     * 用canvas承载某一页上的所有文字和图片贴纸
     */
    private fun createPDImageXObjectFromBitmap(
        document: PDDocument,
        bitmap: Bitmap,
    ): PDImageXObject {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()
        return PDImageXObject.createFromByteArray(document, byteArray, null)
    }

    /**
     * 绘制所有贴纸并导出
     */
    fun drawableStickerToPDDocument(
        context: Context,
        originalPdfFile: File,
        outputPdfFile: File,
        stickers: List<Sticker>,
    ) = flow {
        runCatching {
            val document = PDDocument.load(originalPdfFile)
            document.pages.forEachIndexed { index, pdPage ->
                val mediaBox = pdPage.mediaBox
                val pageWidth = mediaBox.width
                val pageHeight = mediaBox.height
                val rotation = pdPage.rotation

                // 计算屏幕到PDF的缩放比例（考虑旋转）
                val canvasWidth = ScreenUtils.getScreenWidth()
                // 绘制图像（考虑旋转后的坐标）
                val (drawWidth, drawHeight) = when (rotation) {
                    90, 270 -> pageHeight to pageWidth
                    else -> pageWidth to pageHeight
                }
                val canvasHeight = (canvasWidth * drawHeight / drawWidth).toInt()
                println("drawableStickerToPDDocument: $rotation $drawWidth $drawHeight")

                PDPageContentStream(
                    document, pdPage, AppendMode.APPEND, true, true
                ).use {
                    // 应用水印逻辑
                    val stickerList = stickers.filter { it.curPage == index }
                    if (stickerList.isNotEmpty()) {
                        val bitmap =
                            Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bitmap)
                        // 保存当前图形状态
                        it.saveGraphicsState()

                        stickerList.forEach { sticker ->
                            canvas.save()
                            PdfDrawManager.drawSticker(context, sticker, canvas, index)
                            canvas.restore()
                        }
                        // 关键部分：根据页面旋转角度应用变换
                        // 根据页面旋转应用变换矩阵
                        when (rotation) {
                            90 -> {
                                // 顺时针旋转270度（或逆时针90度）：先平移X轴，再旋转
                                it.transform(Matrix.getTranslateInstance(pageWidth, 0f))
                                it.transform(
                                    Matrix.getRotateInstance(-Math.PI * 3 / 2, 0.0f, 0.0f)
                                ) // 或使用 Math.PI / 2 并调整平移顺序
                            }

                            180 -> {
                                // 旋转180度：先平移到页面右下角，再旋转
                                it.transform(Matrix.getTranslateInstance(pageWidth, pageHeight))
                                it.transform(Matrix.getRotateInstance(Math.PI, 0.0f, 0.0f))
                            }

                            270 -> {
                                // 顺时针旋转90度：先平移Y轴，再旋转
                                it.transform(Matrix.getTranslateInstance(0f, pageHeight))
                                it.transform(Matrix.getRotateInstance(-Math.PI / 2, 0.0f, 0.0f))
                            }
                            // 对于0度，不需要额外变换
                        }

                        val pdImage = createPDImageXObjectFromBitmap(document, bitmap)
//                        it.drawImage(pdImage, 0f, 0f, pdPage.bBox.width, pdPage.bBox.height)
                        it.drawImage(pdImage, 0f, 0f, drawWidth, drawHeight)
                        it.restoreGraphicsState()
                        bitmap.recycle()
                    }
                    emit(index)
                }
            }
            document.save(outputPdfFile)
            scanFile(outputPdfFile)
            document.close()
        }.onFailure {
            throw it
        }
    }.flowOn(Dispatchers.IO)

    /**
     * 创建新的PDF
     * 备注：无论原始页面方向如何，都使用统一的页面尺寸
     */
    fun createNewPDDocument(
        originalPdfFile: File,
        outputPdfFile: File,
        pdItems: List<PdfItemInfo>,
    ) = flow {
        runCatching {
            val newDoc = PDDocument()
            val srcDoc = PDDocument.load(originalPdfFile)
            // 获取基础尺寸（使用第一页的尺寸）
            val firstPage = srcDoc.getPage(0)
            val baseWidth = firstPage.mediaBox.width
            val baseHeight = firstPage.mediaBox.height

            pdItems.forEach { item ->
                if (item.bitmap == null) {
                    val newPage = PDPage(PDRectangle(baseWidth, baseHeight))
                    newPage.rotation = item.rotationAngle
                    newDoc.addPage(newPage)
                } else {
                    val newPage = srcDoc.getPage(item.page)
                    newPage.rotation = (item.rotationAngle + newPage.rotation) % 360
                    newDoc.addPage(newPage)
                }
            }

            newDoc.save(outputPdfFile)
            emit(outputPdfFile)
            scanFile(outputPdfFile)
            srcDoc.close()
            newDoc.close() // 务必关闭文档释放资源
        }.onFailure {
            throw it
        }
    }.flowOn(Dispatchers.IO)


    // 创建PDF文件
    fun createPdfFile(): File {
        pdfDir.apply {
            if (!exists()) mkdirs()
        }
        // 2. 生成带时间戳的文件名
        val fileName = "OUT_PDF_${System.currentTimeMillis()}.pdf"
        // 3. 获取或创建目录
        // 4. 创建文件
        val pdfFile = File(pdfDir, fileName)
        // 文件创建成功
        pdfFile.createNewFile()
        return pdfFile
    }

    private fun scanFile(file: File) {
        MediaScannerConnection.scanFile(
            PdfSdk.requireApp(),
            arrayOf(file.absolutePath),
            null
        ) { path, uri ->
            Log.d("MediaScanner", "Scanned $path, uri: $uri")
        }
    }
}