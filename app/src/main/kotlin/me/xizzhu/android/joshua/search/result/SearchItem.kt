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

package me.xizzhu.android.joshua.search.result

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.ui.createTitleSizeSpan
import me.xizzhu.android.joshua.ui.createTitleStyleSpan
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.BaseViewHolder
import me.xizzhu.android.joshua.ui.updateSettingsWithPrimaryText
import java.util.*

data class SearchItem(val verseIndex: VerseIndex, private val bookShortName: String,
                      private val text: String, private val query: String,
                      val onClicked: (VerseIndex) -> Unit)
    : BaseItem(R.layout.item_search_result, { inflater, parent -> SearchItemViewHolder(inflater, parent) }) {
    companion object {
        // We don't expect users to change locale that frequently.
        @SuppressLint("ConstantLocale")
        private val DEFAULT_LOCALE = Locale.getDefault()

        private val BOOK_NAME_SIZE_SPAN = createTitleSizeSpan()
        private val BOOK_NAME_STYLE_SPAN = createTitleStyleSpan()
        private val KEYWORD_SIZE_SPAN = createKeywordSizeSpan()
        private val KEYWORD_STYLE_SPAN = createKeywordStyleSpan()
        private val SPANNABLE_STRING_BUILDER = SpannableStringBuilder()

        private fun createKeywordSizeSpan() = RelativeSizeSpan(1.2F)
        private fun createKeywordStyleSpan() = StyleSpan(Typeface.BOLD)
    }

    val textForDisplay: CharSequence by lazy {
        SPANNABLE_STRING_BUILDER.clear()
        SPANNABLE_STRING_BUILDER.clearSpans()

        // format:
        // <short book name> <chapter verseIndex>:<verse verseIndex>
        // <verse text>
        SPANNABLE_STRING_BUILDER.append(bookShortName)
                .append(' ')
                .append((verseIndex.chapterIndex + 1).toString())
                .append(':')
                .append((verseIndex.verseIndex + 1).toString())
                .append('\n')
                .append(text)

        // makes the book name & verse index smaller
        val textStartIndex = SPANNABLE_STRING_BUILDER.length - text.length
        SPANNABLE_STRING_BUILDER.setSpan(BOOK_NAME_SIZE_SPAN, 0, textStartIndex, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        SPANNABLE_STRING_BUILDER.setSpan(BOOK_NAME_STYLE_SPAN, 0, textStartIndex, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)

        // highlights the keywords
        val lowerCase = SPANNABLE_STRING_BUILDER.toString().toLowerCase(DEFAULT_LOCALE)
        for ((index, keyword) in query.trim().replace("\\s+", " ").split(" ").withIndex()) {
            val start = lowerCase.indexOf(keyword.toLowerCase(DEFAULT_LOCALE), textStartIndex)
            if (start > 0) {
                val end = start + keyword.length
                if (index == 0) {
                    SPANNABLE_STRING_BUILDER.setSpan(KEYWORD_SIZE_SPAN, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                    SPANNABLE_STRING_BUILDER.setSpan(KEYWORD_STYLE_SPAN, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                } else {
                    SPANNABLE_STRING_BUILDER.setSpan(createKeywordSizeSpan(), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                    SPANNABLE_STRING_BUILDER.setSpan(createKeywordStyleSpan(), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                }
            }
        }

        return@lazy SPANNABLE_STRING_BUILDER.subSequence(0, SPANNABLE_STRING_BUILDER.length)
    }
}

private class SearchItemViewHolder(inflater: LayoutInflater, parent: ViewGroup)
    : BaseViewHolder<SearchItem>(inflater.inflate(R.layout.item_search_result, parent, false)) {
    private val text = itemView as TextView

    init {
        itemView.setOnClickListener { item?.let { it.onClicked(it.verseIndex) } }
    }

    override fun bind(settings: Settings, item: SearchItem, payloads: List<Any>) {
        with(text) {
            updateSettingsWithPrimaryText(settings)
            text = item.textForDisplay
        }
    }
}
