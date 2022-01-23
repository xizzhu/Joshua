/*
 * Copyright (C) 2022 Xizhi Zhu
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
import androidx.viewpager2.widget.ViewPager2
import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.databinding.PageVerseBinding
import me.xizzhu.android.joshua.reading.VerseUpdate
import me.xizzhu.android.joshua.ui.fadeIn
import me.xizzhu.android.joshua.ui.fadeOut
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem

class VersePagerAdapter(
        context: Context, private val loadVerses: (Int, Int) -> Unit, private val updateCurrentVerse: (VerseIndex) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val inflater = LayoutInflater.from(context)
    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            val newBookIndex = position.toBookIndex()
            val newChapterIndex = position.toChapterIndex()
            if (currentVerseIndex.bookIndex != newBookIndex || currentVerseIndex.chapterIndex != newChapterIndex) {
                currentVerseIndex = VerseIndex(newBookIndex, newChapterIndex, 0)
            }
        }
    }

    private var attachedRecyclerView: RecyclerView? = null

    private var currentVerseIndex = VerseIndex.INVALID
    private var currentTranslation = ""
    private var parallelTranslations = emptyList<String>()

    var settings: Settings? = null
        set(value) {
            field = value
            notifyDataSetChangedSafely()
        }

    private fun notifyDataSetChangedSafely() {
        // RecyclerView throws IllegalStateException if it is computing layout or scrolling when notifying
        // item / data changes. This is a work-around for this issue.
        // See RecyclerView.assertNotInLayoutOrScroll() for details.
        attachedRecyclerView?.let { recyclerView ->
            if (recyclerView.isComputingLayout) {
                recyclerView.post { notifyDataSetChanged() }
                return
            }
        }
        notifyDataSetChanged()
    }

    fun setCurrent(verseIndex: VerseIndex, translation: String, parallel: List<String>) {
        val shouldNotifyDataSetChanged = currentVerseIndex.bookIndex != verseIndex.bookIndex
                || currentVerseIndex.chapterIndex != verseIndex.chapterIndex
                || currentTranslation != translation
                || parallelTranslations != parallel

        currentVerseIndex = verseIndex
        currentTranslation = translation
        parallelTranslations = parallel

        if (shouldNotifyDataSetChanged) notifyDataSetChangedSafely()
    }

    fun setVerses(bookIndex: Int, chapterIndex: Int, verses: List<BaseItem>) {
        notifyItemChangedSafely(indexToPagePosition(bookIndex, chapterIndex), Verses(verses, currentVerseIndex))
    }

    private fun notifyItemChangedSafely(position: Int, payload: Any) {
        // RecyclerView throws IllegalStateException if it is computing layout or scrolling when notifying
        // item / data changes. This is a work-around for this issue.
        // See RecyclerView.assertNotInLayoutOrScroll() for details.
        attachedRecyclerView?.let { recyclerView ->
            if (recyclerView.isComputingLayout) {
                recyclerView.post { notifyItemChanged(position, payload) }
                return
            }
        }
        notifyItemChanged(position, payload)
    }

    fun selectVerse(verseIndex: VerseIndex) {
        notifyVerseUpdate(VerseUpdate(verseIndex, VerseUpdate.VERSE_SELECTED))
    }

    fun deselectVerse(verseIndex: VerseIndex) {
        notifyVerseUpdate(VerseUpdate(verseIndex, VerseUpdate.VERSE_DESELECTED))
    }

    fun notifyVerseUpdate(verseUpdate: VerseUpdate) {
        notifyItemChangedSafely(verseUpdate.verseIndex.toPagePosition(), verseUpdate)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        attachedRecyclerView = recyclerView
        (recyclerView.parent as ViewPager2).registerOnPageChangeCallback(pageChangeCallback)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        attachedRecyclerView = null
        (recyclerView.parent as ViewPager2).unregisterOnPageChangeCallback(pageChangeCallback)
    }

    override fun getItemCount(): Int =
            if (currentVerseIndex.isValid() && currentTranslation.isNotEmpty() && settings != null) Bible.TOTAL_CHAPTER_COUNT else 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            Page(inflater, parent, loadVerses, updateCurrentVerse)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as Page).bind(position.toBookIndex(), position.toChapterIndex(), settings!!)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            (holder as Page).bind(payloads)
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }
}

private class Verses(val verses: List<BaseItem>, val currentVerseIndex: VerseIndex)

private class Page(
        inflater: LayoutInflater, container: ViewGroup, private val loadVerses: (Int, Int) -> Unit, updateCurrentVerse: (VerseIndex) -> Unit
) : RecyclerView.ViewHolder(PageVerseBinding.inflate(inflater, container, false).root) {
    private val viewBinding = PageVerseBinding.bind(itemView).apply {
        verseList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    updateCurrentVerse(VerseIndex(
                            bookIndex = bookIndex,
                            chapterIndex = chapterIndex,
                            verseIndex = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    ))
                }
            }
        })
    }

    private var bookIndex = -1
    private var chapterIndex = -1
    private var verses: List<BaseItem>? = null

    fun bind(bookIndex: Int, chapterIndex: Int, settings: Settings) {
        this.bookIndex = bookIndex
        this.chapterIndex = chapterIndex

        viewBinding.verseList.setSettings(settings)

        loadVerses(bookIndex, chapterIndex)

        viewBinding.verseList.visibility = View.GONE
        viewBinding.loadingSpinner.visibility = View.VISIBLE
    }

    fun bind(payloads: MutableList<Any>) {
        payloads.forEach { payload ->
            when (payload) {
                is VerseUpdate -> if (payload.operation == VerseUpdate.VERSE_SELECTED) {
                    selectVerse(payload)
                } else {
                    notifyVerseUpdate(payload)
                }
                is Verses -> setVerses(payload)
            }
        }
    }

    private fun setVerses(verses: Verses) {
        viewBinding.verseList.fadeIn()
        viewBinding.loadingSpinner.fadeOut()

        this.verses = verses.verses
        viewBinding.verseList.setItems(verses.verses)

        if (verses.currentVerseIndex.verseIndex > 0
                && verses.currentVerseIndex.bookIndex == bookIndex
                && verses.currentVerseIndex.chapterIndex == chapterIndex) {
            viewBinding.verseList.post {
                (viewBinding.verseList.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(verses.currentVerseIndex.toItemPosition(), 0)
            }
        } else {
            viewBinding.verseList.scrollToPosition(0)
        }
    }

    // now we skip empty verses, so need to find the correct position
    private fun VerseIndex.toItemPosition(): Int {
        verses?.forEachIndexed { index, item ->
            when (item) {
                is SimpleVerseItem -> if (item.verse.verseIndex == this) return index
                is VerseItem -> if (item.verse.verseIndex == this) return index
            }
        }
        return RecyclerView.NO_POSITION
    }

    private fun selectVerse(verseUpdate: VerseUpdate) {
        if (verses == null) {
            // when reading activity is opened from e.g. notes list, this is likely called before
            // verses are set, therefore postpone notifying update
            viewBinding.verseList.adapter?.let { adapter ->
                adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                    override fun onChanged() {
                        adapter.unregisterAdapterDataObserver(this)
                        viewBinding.verseList.post { notifyVerseUpdate(verseUpdate) }
                    }
                })
            }
        } else {
            notifyVerseUpdate(verseUpdate)
        }
    }

    private fun notifyVerseUpdate(verseUpdate: VerseUpdate) {
        viewBinding.verseList.adapter?.notifyItemChanged(verseUpdate.verseIndex.toItemPosition(), verseUpdate)
    }
}
