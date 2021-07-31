/*
 * Copyright (C) 2021 Xizhi Zhu
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

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.CharacterStyle
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan

fun createTitleSizeSpan() = RelativeSizeSpan(0.85F)
fun createTitleStyleSpan() = StyleSpan(Typeface.BOLD)

fun createKeywordSizeSpan() = RelativeSizeSpan(1.2F)
fun createKeywordStyleSpan() = StyleSpan(Typeface.BOLD)

fun SpannableStringBuilder.clearAll(): SpannableStringBuilder {
    clear()
    clearSpans()
    return this
}

fun SpannableStringBuilder.append(i: Int): SpannableStringBuilder = append(i.toString())

fun SpannableStringBuilder.setSpan(span: CharacterStyle): SpannableStringBuilder = setSpan(span, 0, length)

fun SpannableStringBuilder.setSpan(span1: CharacterStyle, span2: CharacterStyle): SpannableStringBuilder =
        setSpan(span1, 0, length).setSpan(span2, 0, length)

fun SpannableStringBuilder.setSpan(span: CharacterStyle, start: Int, end: Int): SpannableStringBuilder {
    setSpan(span, start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
    return this
}

fun SpannableStringBuilder.setSpan(span1: CharacterStyle, span2: CharacterStyle, start: Int, end: Int): SpannableStringBuilder =
        setSpan(span1, start, end).setSpan(span2, start, end)

fun SpannableStringBuilder.toCharSequence(): CharSequence = subSequence(0, length)
