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

package me.xizzhu.android.joshua.reading.detail

import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.VerseIndex

data class VerseDetail(val verseIndex: VerseIndex, val verseTextItems: List<VerseTextItem>,
                       val bookmarked: Boolean, @Highlight.Companion.AvailableColor val highlightColor: Int,
                       val note: String, val strongNumberItems: List<StrongNumberItem>) {
    companion object {
        val INVALID: VerseDetail = VerseDetail(VerseIndex.INVALID, emptyList(), false, Highlight.COLOR_NONE, "", emptyList())
    }
}
