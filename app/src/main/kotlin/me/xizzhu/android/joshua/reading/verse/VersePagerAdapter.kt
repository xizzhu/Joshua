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

        fun onVerseClicked(verseIndex: VerseIndex)

        fun onVerseLongClicked(verseIndex: VerseIndex)
    }

    private val inflater = LayoutInflater.from(context)
    private val pages = ArrayList<Page>()

    var currentTranslation = ""

    fun setVerses(bookIndex: Int, chapterIndex: Int, verses: List<Verse>) {
        for (page in pages) {
            if (page.bookIndex == bookIndex && page.chapterIndex == chapterIndex) {
                page.setVerses(verses)
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
            page = Page(context, inflater, container, listener)
            pages.add(page)
        }

        container.addView(page.rootView, 0)
        page.bind(currentTranslation, position.toBookIndex(), position.toChapterIndex())

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

    var translation = ""
        private set
    var bookIndex = -1
        private set
    var chapterIndex = -1
        private set
    var inUse = false
        private set

    private val adapter = VerseListAdapter(inflater)
    private var verses: List<Verse>? = null

    init {
        verseList.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        verseList.adapter = adapter
        verseList.addOnChildAttachStateChangeListener(this)
    }

    fun bind(translation: String, bookIndex: Int, chapterIndex: Int) {
        this.translation = translation
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

    fun setVerses(verses: List<Verse>) {
        verseList.fadeIn()
        loadingSpinner.fadeOut()

        this.verses = verses
        adapter.setVerses(verses)
        verseList.scrollToPosition(0)
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

        listener.onVerseClicked(verses!![position].verseIndex)
    }

    override fun onLongClick(v: View): Boolean {
        val position = verseList.getChildAdapterPosition(v)
        if (position == RecyclerView.NO_POSITION || verses == null) {
            return false
        }

        listener.onVerseLongClicked(verses!![position].verseIndex)

        return true
    }
}
