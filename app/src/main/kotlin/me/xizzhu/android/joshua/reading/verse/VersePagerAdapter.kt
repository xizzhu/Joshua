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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.ui.fadeIn
import me.xizzhu.android.joshua.ui.fadeOut

class VersePagerAdapter(private val context: Context, private val listener: Listener) : PagerAdapter() {
    interface Listener {
        fun onChapterRequested(bookIndex: Int, chapterIndex: Int)

        fun onCurrentVerseUpdated(bookIndex: Int, chapterIndex: Int, verseIndex: Int)

        fun onVerseClicked(verse: VerseForReading)

        fun onVerseLongClicked(verse: VerseForReading)
    }

    private val inflater = LayoutInflater.from(context)
    private val pages = ArrayList<Page>()

    private var currentVerseIndex = VerseIndex.INVALID
    private var currentTranslation = ""
    private var parallelTranslations = emptyList<String>()
    var settings: Settings? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun setCurrent(currentVerseIndex: VerseIndex, currentTranslation: String, parallelTranslations: List<String>) {
        this.currentVerseIndex = currentVerseIndex
        this.currentTranslation = currentTranslation
        this.parallelTranslations = parallelTranslations
        notifyDataSetChanged()
    }

    fun setVerses(bookIndex: Int, chapterIndex: Int, verses: List<VerseForReading>) {
        findPage(bookIndex, chapterIndex)?.setVerses(verses, currentVerseIndex)
    }

    private fun findPage(bookIndex: Int, chapterIndex: Int): Page? {
        pages.forEach { page ->
            if (page.bookIndex == bookIndex && page.chapterIndex == chapterIndex) {
                return page
            }
        }
        return null
    }

    fun selectVerse(verseIndex: VerseIndex) {
        findPage(verseIndex.bookIndex, verseIndex.chapterIndex)?.selectVerse(verseIndex)
    }

    fun deselectVerse(verseIndex: VerseIndex) {
        findPage(verseIndex.bookIndex, verseIndex.chapterIndex)?.deselectVerse(verseIndex)
    }

    override fun getCount(): Int = if (currentTranslation.isNotEmpty() && settings != null) Bible.TOTAL_CHAPTER_COUNT else 0

    override fun getItemPosition(obj: Any): Int {
        val page = obj as Page
        return if (page.currentTranslation == currentTranslation
                && page.parallelTranslations == parallelTranslations
                && page.settings == settings) {
            indexToPagePosition(page.bookIndex, page.chapterIndex)
        } else {
            POSITION_NONE
        }
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean =
            (obj as Page).rootView == view

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val page: Page = (pages.firstOrNull { p -> !p.inUse }
                ?: Page(inflater, container, listener).apply { pages.add(this) })
                .also { page ->
                    page.bind(currentTranslation, parallelTranslations,
                            position.toBookIndex(), position.toChapterIndex(), settings!!)
                }
        container.addView(page.rootView, 0)
        return page
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        val page = obj as Page
        container.removeView(page.rootView)
        page.unbind()
    }
}

private class Page(inflater: LayoutInflater, container: ViewGroup, private val listener: VersePagerAdapter.Listener) {
    var currentTranslation = ""
        private set
    var parallelTranslations = emptyList<String>()
        private set
    var bookIndex = -1
        private set
    var chapterIndex = -1
        private set
    var inUse = false
        private set
    var settings: Settings? = null
        private set

    val rootView: View = inflater.inflate(R.layout.page_verse, container, false)
    private val loadingSpinner = rootView.findViewById<View>(R.id.loading_spinner)
    private val verseList = rootView.findViewById<VerseListView>(R.id.verse_list).apply {
        setListener(listener)
        addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (inUse && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    listener.onCurrentVerseUpdated(bookIndex, chapterIndex,
                            (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition())
                }
            }
        })
    }

    fun bind(currentTranslation: String, parallelTranslations: List<String>, bookIndex: Int, chapterIndex: Int, settings: Settings) {
        this.currentTranslation = currentTranslation
        this.parallelTranslations = parallelTranslations
        this.bookIndex = bookIndex
        this.chapterIndex = chapterIndex
        this.settings = settings.also { verseList.setSettings(it) }

        listener.onChapterRequested(bookIndex, chapterIndex)

        verseList.visibility = View.GONE
        loadingSpinner.visibility = View.VISIBLE
        inUse = true
    }

    fun unbind() {
        inUse = false
    }

    fun setVerses(verses: List<VerseForReading>, currentVerseIndex: VerseIndex) {
        verseList.fadeIn()
        loadingSpinner.fadeOut()

        verseList.setVerses(verses)

        if (currentVerseIndex.verseIndex > 0
                && currentVerseIndex.bookIndex == bookIndex
                && currentVerseIndex.chapterIndex == chapterIndex) {
            verseList.post {
                (verseList.layoutManager as LinearLayoutManager)
                        .scrollToPositionWithOffset(currentVerseIndex.verseIndex, 0)
            }
        } else {
            verseList.scrollToPosition(0)
        }
    }

    fun selectVerse(verseIndex: VerseIndex) {
        verseList.selectVerse(verseIndex)
    }

    fun deselectVerse(verseIndex: VerseIndex) {
        verseList.deselectVerse(verseIndex)
    }
}
