/*
 * Copyright (C) 2023 Xizhi Zhu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.xizzhu.android.joshua.ui

import android.content.Context
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.text.style.CharacterStyle
import android.text.style.DynamicDrawableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import me.xizzhu.android.joshua.core.Highlight
import kotlin.math.roundToInt

fun createTitleStyleSpan() = StyleSpan(Typeface.BOLD)
fun createTitleSpans(): Array<CharacterStyle> = arrayOf(RelativeSizeSpan(0.85F), createTitleStyleSpan())

fun createDrawableSpan(
        context: Context,
        @DrawableRes drawableId: Int,
        verticalAlignment: Int = DynamicDrawableSpan.ALIGN_BOTTOM,
        colorFilter: ColorFilter? = null,
        scale: Float = 1.0F
): ImageSpan? {
    val drawable = ContextCompat.getDrawable(context, drawableId) ?: return null
    drawable.setBounds(0, 0, (drawable.intrinsicWidth * scale).roundToInt(), (drawable.intrinsicHeight * scale).roundToInt())
    colorFilter?.let { drawable.colorFilter = it }
    return ImageSpan(drawable, verticalAlignment)
}

fun createHighlightSpans(@ColorInt highlightColor: Int): Array<CharacterStyle> =
        if (highlightColor != Highlight.COLOR_NONE) {
            val foregroundColor = if (highlightColor == Highlight.COLOR_BLUE || highlightColor == Highlight.COLOR_RED) Color.WHITE else Color.BLACK
            arrayOf(BackgroundColorSpan(highlightColor), ForegroundColorSpan(foregroundColor))
        } else {
            emptyArray()
        }

fun createKeywordSpans(): Array<CharacterStyle> = arrayOf(RelativeSizeSpan(1.2F), StyleSpan(Typeface.BOLD))

fun SpannableStringBuilder.clearAll(): SpannableStringBuilder {
    clear()
    clearSpans()
    return this
}

fun SpannableStringBuilder.append(i: Int): SpannableStringBuilder = append(i.toString())

fun SpannableStringBuilder.setSpan(span: CharacterStyle, start: Int = 0, end: Int = length): SpannableStringBuilder {
    setSpan(span, start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
    return this
}

fun SpannableStringBuilder.setSpans(spans: Array<CharacterStyle>, start: Int = 0, end: Int = length): SpannableStringBuilder {
    spans.forEach { setSpan(it, start, end) }
    return this
}

fun SpannableStringBuilder.toCharSequence(): CharSequence = subSequence(0, length)
