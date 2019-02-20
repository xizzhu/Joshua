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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.utils.MVPView
import me.xizzhu.android.joshua.ui.fadeIn
import me.xizzhu.android.joshua.ui.fadeOut

interface VerseView : MVPView {
    fun onCurrentVerseIndexUpdated(currentVerseIndex: VerseIndex)

    fun onCurrentTranslationUpdated(currentTranslation: String)

    fun onVersesLoaded(bookIndex: Int, chapterIndex: Int, verses: List<Verse>)

    fun onError(e: Exception)
}

class VerseViewPager : ViewPager, VerseView, VersePagerAdapter.Listener {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    private val adapter = VersePagerAdapter(context, this)
    private val onPageChangeListener = object : SimpleOnPageChangeListener() {
        override fun onPageSelected(position: Int) {
            if (currentVerseIndex.toPagePosition() == position) {
                return
            }
            presenter.updateCurrentVerseIndex(VerseIndex(
                    position.toBookIndex(), position.toChapterIndex(), 0))
        }
    }

    init {
        setAdapter(adapter)
        addOnPageChangeListener(onPageChangeListener)
    }

    private lateinit var presenter: VersePresenter

    private var currentTranslation = ""
    private var currentVerseIndex = VerseIndex.INVALID

    fun setPresenter(presenter: VersePresenter) {
        this.presenter = presenter
    }

    override fun onCurrentVerseIndexUpdated(currentVerseIndex: VerseIndex) {
        this.currentVerseIndex = currentVerseIndex
        refreshUi()
    }

    private fun refreshUi() {
        if (currentTranslation.isEmpty() || !currentVerseIndex.isValid()) {
            return
        }

        adapter.currentTranslation = currentTranslation
        adapter.notifyDataSetChanged()
        setCurrentItem(currentVerseIndex.toPagePosition(), false)
    }

    override fun onCurrentTranslationUpdated(currentTranslation: String) {
        this.currentTranslation = currentTranslation
        refreshUi()
    }

    override fun onVersesLoaded(bookIndex: Int, chapterIndex: Int, verses: List<Verse>) {
        adapter.setVerses(bookIndex, chapterIndex, verses)
    }

    override fun onError(e: Exception) {
        // TODO
    }

    override fun onChapterRequested(bookIndex: Int, chapterIndex: Int) {
        presenter.loadVerses(currentTranslation, bookIndex, chapterIndex)
    }
}

private class VersePagerAdapter(private val context: Context, private val listener: Listener) : PagerAdapter() {
    interface Listener {
        fun onChapterRequested(bookIndex: Int, chapterIndex: Int)
    }

    private val inflater = LayoutInflater.from(context)
    private val pages = ArrayList<Page>()

    var currentTranslation = ""

    fun setVerses(bookIndex: Int, chapterIndex: Int, verses: List<Verse>) {
        for (page in pages) {
            if (page.bookIndex == bookIndex && page.chapterIndex == chapterIndex) {
                page.verseList.fadeIn()
                page.loadingSpinner.fadeOut()

                page.adapter.setVerses(verses)
                page.verseList.scrollToPosition(0)
                break
            }
        }
    }

    override fun getCount(): Int = if (currentTranslation.isNotEmpty()) Bible.TOTAL_CHAPTER_COUNT else 0

    override fun getItemPosition(obj: Any): Int {
        val page = obj as Page
        return if (page.translation == currentTranslation) {
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
            page = Page(context, inflater, container)
            pages.add(page)
        }

        container.addView(page.rootView, 0)
        page.verseList.visibility = View.GONE
        page.loadingSpinner.visibility = View.VISIBLE

        page.translation = currentTranslation
        page.bookIndex = position.toBookIndex()
        page.chapterIndex = position.toChapterIndex()
        page.inUse = true

        listener.onChapterRequested(page.bookIndex, page.chapterIndex)

        return page
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        val page = obj as Page
        container.removeView(page.rootView)
        page.inUse = false
    }
}

private class Page(context: Context, inflater: LayoutInflater, container: ViewGroup) {
    val rootView = inflater.inflate(R.layout.page_verse, container, false)
    val verseList = rootView.findViewById(R.id.verse_list) as RecyclerView
    val loadingSpinner = rootView.findViewById<View>(R.id.loading_spinner)

    var translation = ""
    var bookIndex = -1
    var chapterIndex = -1
    var inUse = false

    val adapter = VerseListAdapter(inflater)

    init {
        verseList.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        verseList.adapter = adapter
    }
}
