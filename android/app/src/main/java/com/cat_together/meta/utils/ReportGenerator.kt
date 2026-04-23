package com.cat_together.meta.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportGenerator {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // 保存图片到相册
    fun saveBitmapToGallery(context: Context, bitmap: Bitmap, fileName: String): Boolean {
        return try {
            val outputStream: OutputStream?
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/猫咪伴侣")
                }
                val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                outputStream = uri?.let { context.contentResolver.openOutputStream(it) }
            } else {
                @Suppress("DEPRECATION")
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val appDir = File(imagesDir, "猫咪伴侣")
                if (!appDir.exists()) appDir.mkdirs()
                val imageFile = File(appDir, fileName)
                outputStream = FileOutputStream(imageFile)
            }
            outputStream?.use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // 保存Bitmap到临时文件，返回文件路径
    fun saveBitmapToTempFile(context: Context, bitmap: Bitmap, fileName: String): String? {
        return try {
            val cacheDir = context.cacheDir
            val file = File(cacheDir, fileName)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 从临时文件加载Bitmap
    fun loadBitmapFromFile(filePath: String): Bitmap? {
        return try {
            BitmapFactory.decodeFile(filePath)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 删除临时文件
    fun deleteTempFile(filePath: String) {
        try {
            File(filePath).delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 生成健康分析报告图片
    fun generateHealthReport(
        context: Context,
        cat: com.cat_together.meta.model.Cat,
        records: List<com.cat_together.meta.model.HealthRecord>,
        aiAdvice: String
    ): Bitmap {
        val width = 1080
        val height = 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 背景渐变
        val bgPaint = Paint()
        val bgGradient = LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            intArrayOf(Color.parseColor("#8B7355"), Color.parseColor("#6B5344")),
            null,
            Shader.TileMode.CLAMP
        )
        bgPaint.shader = bgGradient
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // 顶部圆角装饰
        val topDecorPaint = Paint().apply {
            color = Color.WHITE
            alpha = 30
        }
        val topPath = Path().apply {
            moveTo(0f, 200f)
            cubicTo(width * 0.2f, 100f, width * 0.8f, 100f, width.toFloat(), 200f)
            lineTo(width.toFloat(), 0f)
            lineTo(0f, 0f)
            close()
        }
        canvas.drawPath(topPath, topDecorPaint)

        // 标题
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 56f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("健康分析报告", width / 2f, 180f, titlePaint)

        // 猫咪名字
        val catNamePaint = Paint().apply {
            color = Color.WHITE
            textSize = 72f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(cat.name, width / 2f, 280f, catNamePaint)

        // 报告日期
        val datePaint = Paint().apply {
            color = Color.WHITE
            alpha = 180
            textSize = 32f
            textAlign = Paint.Align.CENTER
        }
        val today = dateFormat.format(Date())
        canvas.drawText(today, width / 2f, 340f, datePaint)

        // 白色卡片区域
        val cardPaint = Paint().apply {
            color = Color.WHITE
        }
        val cardTop = 400f
        val cardBottom = height - 200f
        val cardRect = RectF(40f, cardTop, width - 40f, cardBottom)
        canvas.drawRoundRect(cardRect, 40f, 40f, cardPaint)

        // 卡片标题装饰条
        val accentPaint = Paint().apply {
            color = Color.parseColor("#FFB7B2")
        }
        canvas.drawRoundRect(RectF(60f, cardTop + 30f, 260f, cardTop + 50f), 10f, 10f, accentPaint)

        val cardTitlePaint = Paint().apply {
            color = Color.parseColor("#1A1A1A")
            textSize = 40f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("数据概览", 80f, cardTop + 100f, cardTitlePaint)

        // 统计数据
        val statsPaint = Paint().apply {
            color = Color.parseColor("#666666")
            textSize = 36f
        }
        val valuePaint = Paint().apply {
            color = Color.parseColor("#1A1A1A")
            textSize = 48f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val weightRecords = records.filter { it.recordType == com.cat_together.meta.model.HealthRecord.TYPE_WEIGHT }
        val lastWeight = weightRecords.lastOrNull()?.value ?: 0f
        val firstWeight = weightRecords.firstOrNull()?.value ?: 0f
        val weightChange = if (firstWeight > 0) lastWeight - firstWeight else 0f

        var yPos = cardTop + 180f
        canvas.drawText("体重记录", 80f, yPos, statsPaint)
        yPos += 60f
        canvas.drawText("${String.format("%.1f", lastWeight)} kg", 80f, yPos, valuePaint)

        val changeText = if (weightChange > 0) "+${String.format("%.1f", weightChange)} kg" else "${String.format("%.1f", weightChange)} kg"
        val changePaint = Paint().apply {
            color = if (weightChange > 0) Color.parseColor("#4CAF50") else Color.parseColor("#F44336")
            textSize = 32f
        }
        canvas.drawText("较上周: $changeText", 320f, yPos, changePaint)

        yPos += 100f
        val heightRecords = records.filter { it.recordType == com.cat_together.meta.model.HealthRecord.TYPE_HEIGHT }
        val lastHeight = heightRecords.lastOrNull()?.value ?: 0f
        canvas.drawText("身高记录", 80f, yPos, statsPaint)
        yPos += 60f
        canvas.drawText("${String.format("%.1f", lastHeight)} cm", 80f, yPos, valuePaint)

        yPos += 100f
        val vaccineRecords = records.filter { it.recordType == com.cat_together.meta.model.HealthRecord.TYPE_VACCINE }
        val dewormRecords = records.filter { it.recordType == com.cat_together.meta.model.HealthRecord.TYPE_DEWORMING }
        canvas.drawText("疫苗记录: ${vaccineRecords.size}次  |  驱虫记录: ${dewormRecords.size}次", 80f, yPos, statsPaint)

        yPos += 80f
        val linePaint = Paint().apply {
            color = Color.parseColor("#E5E5E5")
            strokeWidth = 2f
        }
        canvas.drawLine(60f, yPos, width - 60f, yPos, linePaint)

        yPos += 60f
        val aiTitlePaint = Paint().apply {
            color = Color.parseColor("#8B7355")
            textSize = 40f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("AI 健康建议", 80f, yPos, aiTitlePaint)

        yPos += 70f
        val aiContentPaint = Paint().apply {
            color = Color.parseColor("#333333")
            textSize = 32f
        }

        val maxY = cardBottom - 100f
        val lineHeight = 50f

        val adviceLines = wrapTextSimple(aiAdvice, aiContentPaint, width - 160f)
        for (line in adviceLines) {
            if (yPos > maxY) break
            canvas.drawText(line, 80f, yPos, aiContentPaint)
            yPos += lineHeight
        }

        val catPaint = Paint().apply {
            color = Color.parseColor("#8B7355")
            alpha = 50
        }
        drawSimpleCat(canvas, width / 2f - 80f, cardBottom - 180f, 160f, catPaint)

        val footerPaint = Paint().apply {
            color = Color.parseColor("#999999")
            textSize = 28f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("由猫咪伴侣 AI 生成", width / 2f, height - 60f, footerPaint)

        return bitmap
    }

    // 生成饮食分析报告图片
    fun generateDietReport(
        context: Context,
        cat: com.cat_together.meta.model.Cat,
        records: List<com.cat_together.meta.model.DietRecord>,
        aiAdvice: String
    ): Bitmap {
        val width = 1080
        val height = 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgPaint = Paint()
        val bgGradient = LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            intArrayOf(Color.parseColor("#7CB342"), Color.parseColor("#558B2F")),
            null,
            Shader.TileMode.CLAMP
        )
        bgPaint.shader = bgGradient
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val topDecorPaint = Paint().apply {
            color = Color.WHITE
            alpha = 30
        }
        val topPath = Path().apply {
            moveTo(0f, 200f)
            cubicTo(width * 0.2f, 100f, width * 0.8f, 100f, width.toFloat(), 200f)
            lineTo(width.toFloat(), 0f)
            lineTo(0f, 0f)
            close()
        }
        canvas.drawPath(topPath, topDecorPaint)

        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 56f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("饮食分析报告", width / 2f, 180f, titlePaint)

        val catNamePaint = Paint().apply {
            color = Color.WHITE
            textSize = 72f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(cat.name, width / 2f, 280f, catNamePaint)

        val datePaint = Paint().apply {
            color = Color.WHITE
            alpha = 180
            textSize = 32f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(dateFormat.format(Date()), width / 2f, 340f, datePaint)

        val cardPaint = Paint().apply {
            color = Color.WHITE
        }
        val cardTop = 400f
        val cardBottom = height - 200f
        canvas.drawRoundRect(RectF(40f, cardTop, width - 40f, cardBottom), 40f, 40f, cardPaint)

        val accentPaint = Paint().apply {
            color = Color.parseColor("#7CB342")
        }
        canvas.drawRoundRect(RectF(60f, cardTop + 30f, 260f, cardTop + 50f), 10f, 10f, accentPaint)

        val cardTitlePaint = Paint().apply {
            color = Color.parseColor("#1A1A1A")
            textSize = 40f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("饮食统计", 80f, cardTop + 100f, cardTitlePaint)

        val waterCount = records.count { it.type == com.cat_together.meta.model.DietRecord.TYPE_WATER }
        val foodCount = records.count { it.type == com.cat_together.meta.model.DietRecord.TYPE_FOOD }
        val snackCount = records.count { it.type == com.cat_together.meta.model.DietRecord.TYPE_SNACK }
        val treatCount = records.count { it.type == com.cat_together.meta.model.DietRecord.TYPE_TREAT }
        val total = records.size

        var yPos = cardTop + 180f
        val iconPaint = Paint().apply { textSize = 48f; textAlign = Paint.Align.CENTER }
        val labelPaint = Paint().apply { color = Color.parseColor("#666666"); textSize = 32f; textAlign = Paint.Align.CENTER }
        val countPaint = Paint().apply { color = Color.parseColor("#1A1A1A"); textSize = 48f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); textAlign = Paint.Align.CENTER }
        val percentPaint = Paint().apply { color = Color.parseColor("#999999"); textSize = 28f; textAlign = Paint.Align.CENTER }

        val colWidth = (width - 80f) / 4f
        canvas.drawText("💧", colWidth * 0.5f, yPos, iconPaint)
        canvas.drawText("喝水", colWidth * 0.5f, yPos + 40f, labelPaint)
        canvas.drawText(waterCount.toString(), colWidth * 0.5f, yPos + 90f, countPaint)
        canvas.drawText("${if (total > 0) (waterCount * 100 / total) else 0}%", colWidth * 0.5f, yPos + 125f, percentPaint)

        canvas.drawText("🍖", colWidth * 1.5f, yPos, iconPaint)
        canvas.drawText("猫粮", colWidth * 1.5f, yPos + 40f, labelPaint)
        canvas.drawText(foodCount.toString(), colWidth * 1.5f, yPos + 90f, countPaint)
        canvas.drawText("${if (total > 0) (foodCount * 100 / total) else 0}%", colWidth * 1.5f, yPos + 125f, percentPaint)

        canvas.drawText("🍪", colWidth * 2.5f, yPos, iconPaint)
        canvas.drawText("零食", colWidth * 2.5f, yPos + 40f, labelPaint)
        canvas.drawText(snackCount.toString(), colWidth * 2.5f, yPos + 90f, countPaint)
        canvas.drawText("${if (total > 0) (snackCount * 100 / total) else 0}%", colWidth * 2.5f, yPos + 125f, percentPaint)

        canvas.drawText("🐟", colWidth * 3.5f, yPos, iconPaint)
        canvas.drawText("猫条", colWidth * 3.5f, yPos + 40f, labelPaint)
        canvas.drawText(treatCount.toString(), colWidth * 3.5f, yPos + 90f, countPaint)
        canvas.drawText("${if (total > 0) (treatCount * 100 / total) else 0}%", colWidth * 3.5f, yPos + 125f, percentPaint)

        yPos += 180f
        val totalPaint = Paint().apply { color = Color.parseColor("#8B7355"); textSize = 36f; textAlign = Paint.Align.CENTER }
        canvas.drawText("共 ${total} 条饮食记录", width / 2f, yPos, totalPaint)

        yPos += 60f
        val linePaint = Paint().apply { color = Color.parseColor("#E5E5E5"); strokeWidth = 2f }
        canvas.drawLine(60f, yPos, width - 60f, yPos, linePaint)

        yPos += 60f
        val aiTitlePaint = Paint().apply { color = Color.parseColor("#558B2F"); textSize = 40f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
        canvas.drawText("AI 营养建议", 80f, yPos, aiTitlePaint)

        yPos += 70f
        val aiContentPaint = Paint().apply { color = Color.parseColor("#333333"); textSize = 32f }

        val maxY = cardBottom - 100f
        val lineHeight = 50f

        val adviceLines = wrapTextSimple(aiAdvice, aiContentPaint, width - 160f)
        for (line in adviceLines) {
            if (yPos > maxY) break
            canvas.drawText(line, 80f, yPos, aiContentPaint)
            yPos += lineHeight
        }

        val footerPaint = Paint().apply { color = Color.parseColor("#999999"); textSize = 28f; textAlign = Paint.Align.CENTER }
        canvas.drawText("由猫咪伴侣 AI 生成", width / 2f, height - 60f, footerPaint)

        return bitmap
    }

    private fun drawSimpleCat(canvas: Canvas, x: Float, y: Float, size: Float, paint: Paint) {
        canvas.drawCircle(x + size / 2f, y + size / 3f, size / 3f, paint)
        val earPath = Path().apply {
            moveTo(x + size * 0.2f, y + size * 0.2f)
            lineTo(x + size * 0.1f, y)
            lineTo(x + size * 0.35f, y + size * 0.15f)
            close()
        }
        canvas.drawPath(earPath, paint)
        val earPath2 = Path().apply {
            moveTo(x + size * 0.8f, y + size * 0.2f)
            lineTo(x + size * 0.9f, y)
            lineTo(x + size * 0.65f, y + size * 0.15f)
            close()
        }
        canvas.drawPath(earPath2, paint)
    }

    private fun wrapTextSimple(text: String, paint: Paint, maxWidth: Float): List<String> {
        val lines = mutableListOf<String>()
        val paragraphs = text.split("\n")

        for (paragraph in paragraphs) {
            if (paragraph.isEmpty()) {
                lines.add("")
                continue
            }
            var currentLine = StringBuilder()
            for (char in paragraph.toCharArray()) {
                val testLine = currentLine.toString() + char
                if (paint.measureText(testLine) > maxWidth) {
                    if (currentLine.isNotEmpty()) {
                        lines.add(currentLine.toString())
                        currentLine = StringBuilder(char.toString())
                    } else {
                        lines.add(char.toString())
                        currentLine = StringBuilder()
                    }
                } else {
                    currentLine = StringBuilder(testLine)
                }
            }
            if (currentLine.isNotEmpty()) {
                lines.add(currentLine.toString())
            }
        }
        return lines
    }
}
