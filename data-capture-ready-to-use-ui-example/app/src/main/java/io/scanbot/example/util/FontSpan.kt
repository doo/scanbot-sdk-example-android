package io.scanbot.example.util

import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.MetricAffectingSpan

open class FontSpan(private val font: Typeface) : MetricAffectingSpan() {

    override fun updateMeasureState(textPaint: TextPaint) = update(textPaint)

    override fun updateDrawState(textPaint: TextPaint) = update(textPaint)

    private fun update(textPaint: TextPaint) {
        textPaint.apply {
            val old = typeface
            val oldStyle = old?.style ?: Typeface.NORMAL
            val normalizedStyle = when (oldStyle) {
                Typeface.BOLD,
                Typeface.ITALIC,
                Typeface.BOLD_ITALIC -> oldStyle
                else -> Typeface.NORMAL
            }

            // keep the style set before
            val updatedTypeface = Typeface.create(font, normalizedStyle)
            typeface = updatedTypeface
        }
    }
}
