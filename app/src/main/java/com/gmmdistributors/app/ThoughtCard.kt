package com.gmmdistributors.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Native Android Canvas implementation of GMM's "Thought Card Maker" (`apps/thoughts`),
 * built using the exact `ShareCard.kt` pattern from `bas1minute-android`.
 * Draws a 1080x1350 branded poster with the farmer's quote, name, GMM logo, and organic farming pledge,
 * ready to share straight to WhatsApp / Facebook via `FileProvider`.
 */
object ThoughtCard {

    enum class Template(val labelEn: String, val labelBn: String, val topColor: Int, val botColor: Int, val accentColor: Int) {
        FOREST("Emerald Organic", "সবুজ কৃষি", 0xFF0B2417.toInt(), 0xFF1F6B3A.toInt(), 0xFFF59A23.toInt()),
        HARVEST("Golden Harvest", "সোনালি ফসল", 0xFF2B1D08.toInt(), 0xFF6B4423.toInt(), 0xFFFFB93B.toInt()),
        EARTH("Living Soil", "মাটির প্রাণ", 0xFF14201A.toInt(), 0xFF2B3A24.toInt(), 0xFF7FD6A0.toInt()),
    }

    fun render(
        context: Context,
        quote: String,
        authorName: String,
        location: String,
        template: Template,
    ): Bitmap {
        val w = 1080
        val h = 1350
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)

        val display = font(context, R.font.anek_extrabold, Typeface.DEFAULT_BOLD)
        val body = font(context, R.font.mukta_medium, Typeface.DEFAULT)
        val bengali = font(context, R.font.noto_bengali_bold, display)

        // 1. Gradient background
        c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), Paint().apply {
            shader = LinearGradient(0f, 0f, 0f, h.toFloat(), template.topColor, template.botColor, Shader.TileMode.CLAMP)
        })

        // 2. Subtle decorative glow circles
        c.drawCircle(w * 0.85f, h * 0.16f, 220f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = template.accentColor
            alpha = 32
        })
        c.drawCircle(w * 0.15f, h * 0.82f, 260f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = template.accentColor
            alpha = 24
        })

        // 3. Outer border frame
        val frame = RectF(54f, 54f, w - 54f, h - 54f)
        c.drawRoundRect(frame, 42f, 42f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            color = template.accentColor
            alpha = 110
        })

        // 4. Header: GMM Logo + Brand Title
        val logoBmp = runCatching { BitmapFactory.decodeResource(context.resources, R.drawable.gmm_logo) }.getOrNull()
        if (logoBmp != null) {
            val dst = RectF(100f, 100f, 196f, 196f)
            c.drawRoundRect(dst, 48f, 48f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt() })
            val scaled = Bitmap.createScaledBitmap(logoBmp, 96, 96, true)
            c.drawBitmap(scaled, 100f, 100f, null)
        } else {
            c.drawCircle(148f, 148f, 48f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = template.accentColor })
        }

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            textSize = 44f
            typeface = display
        }
        c.drawText("GMM Distributors Pvt. Ltd.", 220f, 142f, textPaint)
        textPaint.apply {
            color = template.accentColor
            textSize = 28f
            typeface = body
        }
        c.drawText("100% Organic Fertilizer & Bio-Pesticide · Zero Chemicals", 220f, 182f, textPaint)

        // 5. Quote mark
        textPaint.apply {
            color = template.accentColor
            textSize = 140f
            typeface = display
            alpha = 180
        }
        c.drawText("“", 104f, 340f, textPaint)

        // 6. Multiline Quote Body (supports both English & Bengali scripts cleanly)
        val cleanQuote = quote.trim().ifBlank {
            "মাটি বাঁচলে বাঁচবে চাষ, জৈব সারেই মিটবে আশ। রাসায়নিকমুক্ত চাষাবাদই আগামী প্রজন্মের সুস্বাস্থ্যের ভিত্তি।"
        }
        val quoteTp = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            textSize = if (cleanQuote.length > 180) 46f else 54f
            typeface = bengali
        }
        val quoteWidth = w - 220
        val staticLayout = StaticLayout.Builder
            .obtain(cleanQuote, 0, cleanQuote.length, quoteTp, quoteWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(12f, 1.15f)
            .setIncludePad(false)
            .build()

        c.save()
        c.translate(110f, 380f)
        staticLayout.draw(c)
        c.restore()

        // 7. Author & Location Pill
        val authorY = (430f + staticLayout.height + 70f).coerceAtMost(h - 270f)
        c.drawLine(110f, authorY - 36f, 290f, authorY - 36f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = template.accentColor
            strokeWidth = 6f
        })
        val displayAuthor = authorName.trim().ifBlank { "একজন সচেতন কৃষক (Organic Farmer)" }
        textPaint.apply {
            color = template.accentColor
            textSize = 40f
            typeface = bengali
            alpha = 255
        }
        c.drawText("— $displayAuthor", 110f, authorY + 12f, textPaint)

        if (location.isNotBlank()) {
            textPaint.apply {
                color = 0xFFD7E4DA.toInt()
                textSize = 30f
                typeface = body
            }
            c.drawText(location.trim(), 110f, authorY + 56f, textPaint)
        }

        // 8. Footer Strip
        val footY = h - 120f
        textPaint.apply {
            color = 0xFFD7E4DA.toInt()
            textSize = 28f
            typeface = body
        }
        val dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH))
        c.drawText("Arambagh, Hooghly · WhatsApp: +91 90837 32101 · gmmdistributors.xyz · $dateStr", 104f, footY, textPaint)

        return bmp
    }

    private fun font(context: Context, id: Int, fallback: Typeface): Typeface =
        try { ResourcesCompat.getFont(context, id) ?: fallback } catch (e: Exception) { fallback }
}
