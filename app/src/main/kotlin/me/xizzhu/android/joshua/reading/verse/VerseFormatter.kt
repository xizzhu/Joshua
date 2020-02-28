/*
 * Copyright (C) 2020 Xizhi Zhu
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
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import androidx.annotation.ColorInt
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.ui.*

private val bookNameSizeSpan = createTitleSizeSpan()
private val bookNameStyleSpan = createTitleStyleSpan()
private val parallelVerseSizeSpan = RelativeSizeSpan(0.95F)

fun SpannableStringBuilder.format(verse: Verse, followingEmptyVerseCount: Int,
                                  simpleReadingModeOn: Boolean, @ColorInt highlightColor: Int): CharSequence {
    clearAll()

    if (verse.parallel.isEmpty()) {
        if (!simpleReadingModeOn) {
            // format:
            // <chapter verseIndex>:<verse verseIndex> <verse text>
            append(verse.verseIndex.chapterIndex + 1).append(':').append(verse.verseIndex.verseIndex + 1)
            if (followingEmptyVerseCount > 0) {
                append('-').append(verse.verseIndex.verseIndex + followingEmptyVerseCount + 1)
            }
            append(' ')

            setSpan(bookNameStyleSpan, bookNameSizeSpan, 0, length)
        }

        append(verse.text.text).setHighlight(verse, highlightColor)
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
        setSpan(parallelVerseSizeSpan, primaryTextLength, length)
    }

    return toCharSequence()
}

private fun SpannableStringBuilder.setHighlight(verse: Verse, @ColorInt highlightColor: Int): SpannableStringBuilder {
    if (highlightColor != Highlight.COLOR_NONE) {
        setSpan(BackgroundColorSpan(highlightColor),
                ForegroundColorSpan(if (highlightColor == Highlight.COLOR_BLUE) Color.WHITE else Color.BLACK),
                length - verse.text.text.length, length)
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
    return setSpan(createTitleSizeSpan(), createTitleStyleSpan(), start, end)
            .append('\n').append(text.text)
}
