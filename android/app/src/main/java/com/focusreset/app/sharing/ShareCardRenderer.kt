package com.focusreset.app.sharing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ShareCardRenderer {
    fun create(context: Context, message: String): Uri {
        val bitmap = Bitmap.createBitmap(1080, 1350, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawColor(Color.rgb(11, 23, 40))

        paint.color = Color.rgb(46, 217, 163)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 28f
        paint.strokeCap = Paint.Cap.ROUND
        canvas.drawArc(RectF(250f, 245f, 830f, 825f), -90f, 294f, false, paint)

        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
        paint.textSize = 52f
        canvas.drawText("FOCUS RESET", 540f, 130f, paint)

        paint.color = Color.WHITE
        paint.textSize = 84f
        canvas.drawText("NO REELS", 540f, 500f, paint)
        paint.textSize = 44f
        paint.color = Color.rgb(148, 164, 184)
        canvas.drawText("Protect your attention", 540f, 575f, paint)

        paint.color = Color.WHITE
        paint.textSize = 46f
        drawWrapped(canvas, paint, message.substringBefore("https://").trim(), 540f, 930f, 880f, 62f)

        paint.color = Color.rgb(46, 217, 163)
        paint.textSize = 34f
        canvas.drawText("One honest day at a time", 540f, 1240f, paint)

        val directory = File(context.cacheDir, "share_cards").apply { mkdirs() }
        val file = File(directory, "focus-reset-share.png")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 95, it) }
        bitmap.recycle()
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    private fun drawWrapped(canvas: Canvas, paint: Paint, text: String, x: Float, startY: Float, width: Float, lineHeight: Float) {
        val words = text.ifBlank { "I chose focus over the feed today." }.split(Regex("\\s+"))
        val lines = mutableListOf<String>()
        var line = ""
        words.forEach { word ->
            val candidate = if (line.isBlank()) word else "$line $word"
            if (paint.measureText(candidate) <= width) line = candidate
            else { if (line.isNotBlank()) lines += line; line = word }
        }
        if (line.isNotBlank()) lines += line
        lines.take(4).forEachIndexed { index, value -> canvas.drawText(value, x, startY + index * lineHeight, paint) }
    }
}
