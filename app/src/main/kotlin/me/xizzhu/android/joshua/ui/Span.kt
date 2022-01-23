/*
 * Copyright (C) 2022 Xizhi Zhu
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

import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.text.style.CharacterStyle
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import androidx.annotation.ColorInt
import me.xizzhu.android.joshua.core.Highlight

fun createTitleStyleSpan() = StyleSpan(Typeface.BOLD)
fun createTitleSpans(): Array<CharacterStyle> = arrayOf(RelativeSizeSpan(0.85F), createTitleStyleSpan())

fun createHighlightSpans(@ColorInt highlightColor: Int): Array<CharacterStyle> =
        if (highlightColor != Highlight.COLOR_NONE) {
            arrayOf(
                    BackgroundColorSpan(highlightColor),
                    ForegroundColorSpan(if (highlightColor == Highlight.COLOR_BLUE) Color.WHITE else Color.BLACK)
            )
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
