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
import me.xizzhu.android.joshua.core.Verse
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

    var currentVerseIndex = VerseIndex.INVALID
    var currentTranslation = ""
    var parallelTranslations = emptyList<String>()

    fun setVerses(bookIndex: Int, chapterIndex: Int, verses: List<VerseForReading>) {
        findPage(bookIndex, chapterIndex)?.setVerses(verses, currentVerseIndex)
    }

    private fun findPage(bookIndex: Int, chapterIndex: Int): Page? {
        for (page in pages) {
            if (page.bookIndex == bookIndex && page.chapterIndex == chapterIndex) {
                return page
            }
        }
        return null
    }

    fun selectVerse(verse: Verse) {
        findPage(verse.verseIndex.bookIndex, verse.verseIndex.chapterIndex)?.selectVerse(verse.verseIndex)
    }

    fun deselectVerse(verse: Verse) {
        findPage(verse.verseIndex.bookIndex, verse.verseIndex.chapterIndex)?.deselectVerse(verse.verseIndex)
    }

    override fun getCount(): Int = if (currentTranslation.isNotEmpty()) Bible.TOTAL_CHAPTER_COUNT else 0

    override fun getItemPosition(obj: Any): Int {
        val page = obj as Page
        return if (page.currentTranslation == currentTranslation
                && page.parallelTranslations == parallelTranslations) {
            indexToPagePosition(page.bookIndex, page.chapterIndex)
        } else {
            POSITION_NONE
        }
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean =
            (obj as Page).rootView == view

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        var page: Page? = null
        for (p in pages) {
            if (!p.inUse) {
                page = p
                break
            }
        }
        if (page == null) {
            page = Page(context, inflater, container, listener)
            pages.add(page)
        }

        container.addView(page.rootView, 0)
        page.bind(currentTranslation, parallelTranslations, position.toBookIndex(), position.toChapterIndex())

        return page
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        val page = obj as Page
        container.removeView(page.rootView)
        page.unbind()
    }
}

private class Page(context: Context, inflater: LayoutInflater, container: ViewGroup, private val listener: VersePagerAdapter.Listener)
    : RecyclerView.OnChildAttachStateChangeListener, View.OnClickListener, View.OnLongClickListener {
    val rootView: View = inflater.inflate(R.layout.page_verse, container, false)
    private val verseList = rootView.findViewById(R.id.verse_list) as RecyclerView
    private val loadingSpinner = rootView.findViewById<View>(R.id.loading_spinner)

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

    private val adapter = VerseListAdapter(inflater)
    private var verses: List<VerseForReading>? = null

    init {
        verseList.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        verseList.adapter = adapter
        verseList.addOnChildAttachStateChangeListener(this)
        verseList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (inUse && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    listener.onCurrentVerseUpdated(bookIndex, chapterIndex,
                            (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition())
                }
            }
        })
    }

    fun bind(currentTranslation: String, parallelTranslations: List<String>, bookIndex: Int, chapterIndex: Int) {
        this.currentTranslation = currentTranslation
        this.parallelTranslations = parallelTranslations
        this.bookIndex = bookIndex
        this.chapterIndex = chapterIndex
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

        this.verses = verses
        adapter.setVerses(verses)

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
        adapter.selectVerse(verseIndex)
    }

    fun deselectVerse(verseIndex: VerseIndex) {
        adapter.deselectVerse(verseIndex)
    }

    override fun onChildViewAttachedToWindow(view: View) {
        view.setOnClickListener(this)
        view.setOnLongClickListener(this)
    }

    override fun onChildViewDetachedFromWindow(view: View) {
        view.setOnClickListener(null)
        view.setOnLongClickListener(null)
    }

    override fun onClick(v: View) {
        val position = verseList.getChildAdapterPosition(v)
        if (position == RecyclerView.NO_POSITION || verses == null) {
            return
        }

        listener.onVerseClicked(verses!![position])
    }

    override fun onLongClick(v: View): Boolean {
        val position = verseList.getChildAdapterPosition(v)
        if (position == RecyclerView.NO_POSITION || verses == null) {
            return false
        }

        listener.onVerseLongClicked(verses!![position])

        return true
    }
}
