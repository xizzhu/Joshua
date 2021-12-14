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

package me.xizzhu.android.joshua.search

import android.annotation.SuppressLint
import android.text.SpannableStringBuilder
import me.xizzhu.android.joshua.ui.createKeywordSpans
import me.xizzhu.android.joshua.ui.setSpans
import java.util.*

// We don't expect users to change locale that frequently.
@SuppressLint("ConstantLocale")
private val DEFAULT_LOCALE = Locale.getDefault()

fun SpannableStringBuilder.highlightKeyword(query: String, start: Int): SpannableStringBuilder {
    if (query.isBlank()) return this

    val lowercase = toString().lowercase(DEFAULT_LOCALE)
    for (keyword in query.trim().lowercase(DEFAULT_LOCALE).replace("\\s+", " ").split(" ")) {
        var startIndex = start
        var keywordStart: Int
        while (true) {
            keywordStart = lowercase.indexOf(keyword, startIndex)
            if (keywordStart == -1) {
                break
            }

            val keywordEnd = keywordStart + keyword.length
            setSpans(createKeywordSpans(), keywordStart, keywordEnd)
            startIndex = keywordEnd + 1
        }
    }
    return this
}
