package com.zakrni.app.clean.ui.compose.quran

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import android.graphics.Color as AndroidColor

/** Renders the given ayah onto a shareable square image (teal card) and fires a share chooser. */
fun shareVerseAsImage(context: Context, ayahText: String, reference: String, appName: String) {
    val bitmap = renderVerseBitmap(ayahText, reference, appName)
    val dir = File(context.cacheDir, "shared_images").apply { mkdirs() }
    val file = File(dir, "verse.png")
    FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, null))
}

private fun renderVerseBitmap(ayahText: String, reference: String, appName: String): Bitmap {
    val size = 1080
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Teal gradient background.
    val bg = Paint().apply {
        shader = LinearGradient(
            0f, 0f, 0f, size.toFloat(),
            AndroidColor.parseColor("#0F766E"),
            AndroidColor.parseColor("#0B3B37"),
            Shader.TileMode.CLAMP,
        )
    }
    canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), bg)

    // Top accent rule.
    val accent = Paint().apply {
        color = AndroidColor.parseColor("#5EEAD4")
        strokeWidth = 7f
        isAntiAlias = true
    }
    canvas.drawLine(size / 2f - 70f, 150f, size / 2f + 70f, 150f, accent)

    // Ayah text (centered, wrapped, RTL-aware).
    val padding = 120
    val textPaint = TextPaint().apply {
        color = AndroidColor.WHITE
        isAntiAlias = true
        textSize = 66f
        typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
    }
    val layout = StaticLayout.Builder
        .obtain(ayahText, 0, ayahText.length, textPaint, size - padding * 2)
        .setAlignment(Layout.Alignment.ALIGN_CENTER)
        .setTextDirection(TextDirectionHeuristics.RTL)
        .setLineSpacing(18f, 1f)
        .build()
    canvas.save()
    canvas.translate(padding.toFloat(), (size - layout.height) / 2f)
    layout.draw(canvas)
    canvas.restore()

    // Reference + app name footer.
    val refPaint = Paint().apply {
        color = AndroidColor.parseColor("#D6F0EB")
        isAntiAlias = true
        textSize = 42f
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText(reference, size / 2f, size - 160f, refPaint)
    val appPaint = Paint().apply {
        color = AndroidColor.parseColor("#8FD3C8")
        isAntiAlias = true
        textSize = 34f
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText(appName, size / 2f, size - 95f, appPaint)

    return bitmap
}
