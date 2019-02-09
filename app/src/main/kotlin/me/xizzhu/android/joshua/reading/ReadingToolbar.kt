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

package me.xizzhu.android.joshua.reading

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.Toolbar
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.model.VerseIndex
import java.lang.StringBuilder

class ReadingToolbar : Toolbar {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        setTitle(R.string.app_name)
    }

    private val titleBuilder = StringBuilder()
    private val bookNames = ArrayList<String>()
    private var verseIndex = VerseIndex.INVALID

    fun setBookNames(bookNames: List<String>) {
        this.bookNames.clear()
        this.bookNames.addAll(bookNames)
        updateTitle()
    }

    fun setVerseIndex(verseIndex: VerseIndex) {
        this.verseIndex = verseIndex
        updateTitle()
    }

    private fun updateTitle() {
        if (bookNames.isEmpty() || !verseIndex.isValid()) {
            return
        }

        titleBuilder.setLength(0)
        titleBuilder.append(bookNames[verseIndex.bookIndex]).append(", ")
                .append(verseIndex.bookIndex + 1).append(":").append(verseIndex.chapterIndex + 1)
        title = titleBuilder.toString()
    }
}
