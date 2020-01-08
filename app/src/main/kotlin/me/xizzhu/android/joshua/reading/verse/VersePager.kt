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

import android.content.Context
import android.util.AttributeSet
import androidx.viewpager.widget.ViewPager
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.reading.VerseUpdate
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem

class VerseViewPager : ViewPager {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    private val adapter = VersePagerAdapter(context).apply { setAdapter(this) }

    fun setListeners(onChapterSelected: (Int, Int) -> Unit,
                     onChapterRequested: (Int, Int) -> Unit,
                     onCurrentVerseUpdated: (VerseIndex) -> Unit) {
        addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                onChapterSelected(position.toBookIndex(), position.toChapterIndex())
            }
        })

        adapter.onChapterRequested = onChapterRequested
        adapter.onCurrentVerseUpdated = onCurrentVerseUpdated
    }

    fun setSettings(settings: Settings) {
        adapter.settings = settings
    }

    fun setCurrent(currentVerseIndex: VerseIndex, currentTranslation: String, parallelTranslations: List<String>) {
        adapter.setCurrent(currentVerseIndex, currentTranslation, parallelTranslations)
        setCurrentItem(currentVerseIndex.toPagePosition(), false)
    }

    fun setVerses(bookIndex: Int, chapterIndex: Int, verses: List<BaseItem>) {
        adapter.setVerses(bookIndex, chapterIndex, verses)
    }

    fun selectVerse(verseIndex: VerseIndex) {
        adapter.selectVerse(verseIndex)
    }

    fun deselectVerse(verseIndex: VerseIndex) {
        adapter.deselectVerse(verseIndex)
    }

    fun notifyVerseUpdate(verseUpdate: VerseUpdate) {
        adapter.notifyVerseUpdate(verseUpdate)
    }
}
