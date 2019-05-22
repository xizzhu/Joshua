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
import android.content.DialogInterface
import android.util.AttributeSet
import android.widget.Toast
import androidx.viewpager.widget.ViewPager
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.ui.DialogHelper
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.utils.BaseSettingsView

interface VerseView : BaseSettingsView {
    fun onCurrentVerseIndexUpdated(currentVerseIndex: VerseIndex)

    fun onCurrentTranslationUpdated(currentTranslation: String)

    fun onParallelTranslationsUpdated(parallelTranslations: List<String>)

    fun onChapterSelectionFailed(bookIndex: Int, chapterIndex: Int)

    fun onVersesLoaded(bookIndex: Int, chapterIndex: Int, verses: List<BaseItem>)

    fun onVersesLoadFailed(bookIndex: Int, chapterIndex: Int)

    fun onVerseSelected(verseIndex: VerseIndex)

    fun onVerseDeselected(verseIndex: VerseIndex)

    fun onVersesCopied()

    fun onVersesCopyShareFailed()

    fun onNoteAdded(verseIndex: VerseIndex)

    fun onNoteRemoved(verseIndex: VerseIndex)
}

class VerseViewPager : ViewPager, VerseView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    private val versePagerAdapterListener = object : VersePagerAdapter.Listener {
        override fun onChapterRequested(bookIndex: Int, chapterIndex: Int) {
            presenter.loadVerses(bookIndex, chapterIndex)
        }

        override fun onCurrentVerseUpdated(bookIndex: Int, chapterIndex: Int, verseIndex: Int) {
            val updatedVerseIndex = VerseIndex(bookIndex, chapterIndex, verseIndex)
            if (currentVerseIndex == updatedVerseIndex) {
                return
            }
            currentVerseIndex = updatedVerseIndex
            presenter.saveCurrentVerseIndex(updatedVerseIndex)
        }
    }
    private val adapter = VersePagerAdapter(context, versePagerAdapterListener)
    private val onPageChangeListener = object : SimpleOnPageChangeListener() {
        override fun onPageSelected(position: Int) {
            if (currentVerseIndex.toPagePosition() == position) {
                return
            }
            presenter.selectChapter(position.toBookIndex(), position.toChapterIndex())
        }
    }

    init {
        setAdapter(adapter)
        addOnPageChangeListener(onPageChangeListener)
    }

    private lateinit var presenter: VersePresenter

    private var currentVerseIndex = VerseIndex.INVALID
    private var currentTranslation = ""
    private var parallelTranslations = emptyList<String>()

    fun setPresenter(presenter: VersePresenter) {
        this.presenter = presenter
    }

    override fun onSettingsUpdated(settings: Settings) {
        adapter.settings = settings
    }

    override fun onCurrentVerseIndexUpdated(currentVerseIndex: VerseIndex) {
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

    override fun onCurrentTranslationUpdated(currentTranslation: String) {
        if (this.currentTranslation == currentTranslation) {
            return
        }
        this.currentTranslation = currentTranslation
        refreshUi()
    }

    override fun onParallelTranslationsUpdated(parallelTranslations: List<String>) {
        if (this.parallelTranslations == parallelTranslations) {
            return
        }
        this.parallelTranslations = parallelTranslations
        refreshUi()
    }

    override fun onChapterSelectionFailed(bookIndex: Int, chapterIndex: Int) {
        DialogHelper.showDialog(context, true, R.string.dialog_chapter_selection_error,
                DialogInterface.OnClickListener { _, _ ->
                    presenter.selectChapter(bookIndex, chapterIndex)
                })
    }

    override fun onVersesLoaded(bookIndex: Int, chapterIndex: Int, verses: List<BaseItem>) {
        adapter.setVerses(bookIndex, chapterIndex, verses)
    }

    override fun onVersesLoadFailed(bookIndex: Int, chapterIndex: Int) {
        DialogHelper.showDialog(context, true, R.string.dialog_verse_load_error,
                DialogInterface.OnClickListener { _, _ ->
                    presenter.loadVerses(bookIndex, chapterIndex)
                })
    }

    override fun onVerseSelected(verseIndex: VerseIndex) {
        adapter.selectVerse(verseIndex)
    }

    override fun onVerseDeselected(verseIndex: VerseIndex) {
        adapter.deselectVerse(verseIndex)
    }

    override fun onVersesCopied() {
        Toast.makeText(context, R.string.toast_verses_copied, Toast.LENGTH_SHORT).show()
    }

    override fun onVersesCopyShareFailed() {
        Toast.makeText(context, R.string.toast_unknown_error, Toast.LENGTH_SHORT).show()
    }

    override fun onNoteAdded(verseIndex: VerseIndex) {
        adapter.notifyNoteAdded(verseIndex)
    }

    override fun onNoteRemoved(verseIndex: VerseIndex) {
        adapter.notifyNoteRemoved(verseIndex)
    }
}
