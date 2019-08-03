/*
 * Copyright (C) 2019 Xizhi Zhu
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

package me.xizzhu.android.joshua.reading.verse

import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import androidx.annotation.ColorInt
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.ui.append

private val indexStyleSpan = StyleSpan(Typeface.BOLD)
private val indexSizeSpan = RelativeSizeSpan(0.75F)
private val parallelVerseSizeSpan = RelativeSizeSpan(0.95F)

fun SpannableStringBuilder.format(verse: Verse, followingEmptyVerseCount: Int,
                                  @ColorInt highlightColor: Int): CharSequence {
    clear()
    clearSpans()

    if (verse.parallel.isEmpty()) {
        val verseIndex = verse.verseIndex.verseIndex
        append(verseIndex + 1)
        if (followingEmptyVerseCount > 0) {
            append('-').append(verseIndex + followingEmptyVerseCount + 1)
        }
        setSpan(indexStyleSpan, 0, length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        setSpan(indexSizeSpan, 0, length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)

        append(' ').append(verse.text.text).setHighlight(verse, highlightColor)
    } else {
        // format:
        // <primary translation> <chapter verseIndex>:<verse verseIndex> <verse text>
        // <empty line>
        // <parallel translation 1> <chapter verseIndex>:<verse verseIndex> <verse text>
        // <parallel translation 2> <chapter verseIndex>:<verse verseIndex> <verse text>
        append(verse.verseIndex, verse.text, followingEmptyVerseCount).setHighlight(verse, highlightColor)

        val primaryTextLength = length
        for (text in verse.parallel) {
            append(verse.verseIndex, text, followingEmptyVerseCount)
        }
        setSpan(parallelVerseSizeSpan, primaryTextLength, length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
    }

    return subSequence(0, length)
}

private fun SpannableStringBuilder.setHighlight(verse: Verse, @ColorInt highlightColor: Int): SpannableStringBuilder {
    if (highlightColor != Highlight.COLOR_NONE) {
        val end = length
        val start = end - verse.text.text.length
        setSpan(BackgroundColorSpan(highlightColor), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        setSpan(ForegroundColorSpan(if (highlightColor == Highlight.COLOR_BLUE) Color.WHITE else Color.BLACK),
                start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
    }
    return this
}

private fun SpannableStringBuilder.append(verseIndex: VerseIndex, text: Verse.Text,
                                          followingEmptyVerseCount: Int): SpannableStringBuilder {
    if (isNotEmpty()) {
        append('\n').append('\n')
    }
    // format:
    // <primary translation> <chapter verseIndex>:<verse verseIndex> <verse text>
    val start = length
    append(text.translationShortName).append(' ')
            .append(verseIndex.chapterIndex + 1).append(':').append(verseIndex.verseIndex + 1)
    if (followingEmptyVerseCount > 0) {
        append('-').append(verseIndex.verseIndex + followingEmptyVerseCount + 1)
    }
    val end = length
    setSpan(StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
    setSpan(RelativeSizeSpan(0.75F), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)

    append(' ').append(text.text)

    return this
}
