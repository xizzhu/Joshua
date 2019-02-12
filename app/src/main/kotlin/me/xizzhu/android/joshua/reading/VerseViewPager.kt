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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.utils.fadeIn
import me.xizzhu.android.joshua.utils.fadeOut
import java.lang.StringBuilder

class VerseViewPager : ViewPager {
    interface Listener {
        fun onChapterSelected(currentVerseIndex: VerseIndex)

        fun onChapterRequested(bookIndex: Int, chapterIndex: Int)
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    private val adapter = VersePagerAdapter(context)
    private val onPageChangeListener = object : SimpleOnPageChangeListener() {
        override fun onPageSelected(position: Int) {
            adapter.listener?.onChapterSelected(VerseIndex(pagePositionToBookIndex(position),
                    pagePositionToChapterIndex(position), 0))
        }
    }

    init {
        setAdapter(adapter)
        addOnPageChangeListener(onPageChangeListener)
    }

    fun setListener(listener: Listener) {
        adapter.listener = listener
    }

    fun setCurrentVerseIndex(currentVerseIndex: VerseIndex) {
        adapter.currentVerseIndex = currentVerseIndex
        adapter.notifyDataSetChanged()
        setCurrentItem(currentVerseIndex.toPagePosition(), false)
    }

    fun setVerses(bookIndex: Int, chapterIndex: Int, verses: List<Verse>) {
        adapter.setVerses(bookIndex, chapterIndex, verses)
    }
}

private fun VerseIndex.toPagePosition(): Int = indexToPagePosition(bookIndex, chapterIndex)

private fun indexToPagePosition(bookIndex: Int, chapterIndex: Int): Int {
    var position = 0
    for (i in 0 until bookIndex) {
        position += Bible.getChapterCount(i)
    }
    return position + chapterIndex
}

private class VersePagerAdapter(private val context: Context) : PagerAdapter() {
    private val inflater = LayoutInflater.from(context)
    private val pages = ArrayList<Page>()

    var listener: VerseViewPager.Listener? = null

    var currentVerseIndex = VerseIndex.INVALID

    fun setVerses(bookIndex: Int, chapterIndex: Int, verses: List<Verse>) {
        for (page in pages) {
            if (page.bookIndex == bookIndex && page.chapterIndex == chapterIndex) {
                page.verseList.fadeIn()
                page.loadingSpinner.fadeOut()

                page.adapter.setVerses(verses)
                break
            }
        }
    }

    override fun getCount(): Int = if (currentVerseIndex.isValid()) Bible.TOTAL_CHAPTER_COUNT else 0

    override fun getItemPosition(obj: Any): Int {
        val page = obj as Page
        return indexToPagePosition(page.bookIndex, page.chapterIndex)
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

        page.bookIndex = pagePositionToBookIndex(position)
        page.chapterIndex = pagePositionToChapterIndex(position)
        page.inUse = true

        listener?.onChapterRequested(page.bookIndex, page.chapterIndex)

        return page
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        val page = obj as Page
        container.removeView(page.rootView)
        page.inUse = false
    }
}

private fun pagePositionToBookIndex(position: Int): Int {
    if (position < 0) {
        throw IllegalArgumentException("Invalid position: $position")
    }

    var pos = position
    for (bookIndex in 0 until Bible.BOOK_COUNT) {
        pos -= Bible.getChapterCount(bookIndex)
        if (pos < 0) {
            return bookIndex
        }
    }

    throw IllegalArgumentException("Invalid position: $position")
}

private fun pagePositionToChapterIndex(position: Int): Int {
    if (position < 0) {
        throw IllegalArgumentException("Invalid position: $position")
    }

    var pos = position
    for (bookIndex in 0 until Bible.BOOK_COUNT) {
        val chapterCount = Bible.getChapterCount(bookIndex)
        if (pos < chapterCount) {
            return pos
        }
        pos -= chapterCount
    }

    throw IllegalArgumentException("Invalid position: $position")
}

private class Page(context: Context, inflater: LayoutInflater, container: ViewGroup) {
    val rootView = inflater.inflate(R.layout.page_verse, container, false)
    val verseList = rootView.findViewById(R.id.verse_list) as RecyclerView
    val loadingSpinner = rootView.findViewById<View>(R.id.loading_spinner)

    var bookIndex = -1
    var chapterIndex = -1
    var inUse = false

    val adapter = VerseListAdapter(inflater)

    init {
        verseList.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        verseList.adapter = adapter
    }
}

private class VerseListAdapter(private val inflater: LayoutInflater) : RecyclerView.Adapter<VerseItemViewHolder>() {
    private val verses = ArrayList<Verse>()

    fun setVerses(verses: List<Verse>) {
        this.verses.clear()
        this.verses.addAll(verses)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = verses.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerseItemViewHolder =
            VerseItemViewHolder(inflater, parent)

    override fun onBindViewHolder(holder: VerseItemViewHolder, position: Int) {
        holder.bind(verses[position])
    }
}

private class VerseItemViewHolder(inflater: LayoutInflater, parent: ViewGroup)
    : RecyclerView.ViewHolder(inflater.inflate(R.layout.item_verse, parent, false)) {
    companion object {
        private val STRING_BUILDER = StringBuilder()
    }

    private val index = itemView.findViewById(R.id.index) as TextView
    private val text = itemView.findViewById(R.id.text) as TextView

    fun bind(verse: Verse) {
        STRING_BUILDER.setLength(0)
        val verseIndex = verse.verseIndex.verseIndex
        if (verseIndex + 1 < 10) {
            STRING_BUILDER.append("  ")
        } else if (verseIndex + 1 < 100) {
            STRING_BUILDER.append(" ")
        }
        STRING_BUILDER.append(verseIndex + 1)
        index.text = STRING_BUILDER.toString()

        text.text = verse.text
    }
}
