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

    private var currentVerseIndex = VerseIndex.INVALID
    private var currentTranslation = ""
    private var parallelTranslations = emptyList<String>()

    fun setOnChapterSelectedListener(onChapterSelected: (Int, Int) -> Unit) {
        addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                onChapterSelected(position.toBookIndex(), position.toChapterIndex())
            }
        })
    }

    fun setOnChapterRequestedListener(onChapterRequested: (Int, Int) -> Unit) {
        adapter.onChapterRequested = onChapterRequested
    }

    fun setOnCurrentVerseUpdatedListener(onCurrentVerseUpdated: (VerseIndex) -> Unit) {
        adapter.onCurrentVerseUpdated = onCurrentVerseUpdated
    }

    fun onSettingsUpdated(settings: Settings) {
        adapter.settings = settings
    }

    fun onCurrentVerseIndexUpdated(currentVerseIndex: VerseIndex) {
        if (this.currentVerseIndex == currentVerseIndex) {
            return
        }
        this.currentVerseIndex = currentVerseIndex
        refreshUi()
    }

    private fun refreshUi() {
        if (currentTranslation.isEmpty() || !currentVerseIndex.isValid()) {
            return
        }

        adapter.setCurrent(currentVerseIndex, currentTranslation, parallelTranslations)
        setCurrentItem(currentVerseIndex.toPagePosition(), false)
    }

    fun onCurrentTranslationUpdated(currentTranslation: String) {
        if (this.currentTranslation == currentTranslation) {
            return
        }
        this.currentTranslation = currentTranslation
        refreshUi()
    }

    fun onParallelTranslationsUpdated(parallelTranslations: List<String>) {
        if (this.parallelTranslations == parallelTranslations) {
            return
        }
        this.parallelTranslations = parallelTranslations
        refreshUi()
    }

    fun onVersesLoaded(bookIndex: Int, chapterIndex: Int, verses: List<BaseItem>) {
        adapter.setVerses(bookIndex, chapterIndex, verses)
    }

    fun onVerseSelected(verseIndex: VerseIndex) {
        adapter.selectVerse(verseIndex)
    }

    fun onVerseDeselected(verseIndex: VerseIndex) {
        adapter.deselectVerse(verseIndex)
    }

    fun onVerseUpdated(verseUpdate: VerseUpdate) {
        adapter.notifyVerseUpdate(verseUpdate)
    }
}
