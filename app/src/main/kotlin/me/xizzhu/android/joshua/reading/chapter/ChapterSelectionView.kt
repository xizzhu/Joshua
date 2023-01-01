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

package me.xizzhu.android.joshua.reading.chapter

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.AbsListView
import android.widget.ExpandableListView
import androidx.annotation.IntDef
import androidx.constraintlayout.widget.ConstraintLayout
import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.databinding.InnerChapterSelectionViewBinding

class ChapterSelectionView : ConstraintLayout {
    companion object {
        private const val SELECTION_OLD_TESTAMENT = 1
        private const val SELECTION_NEW_TESTAMENT = 2

        @IntDef(SELECTION_OLD_TESTAMENT, SELECTION_NEW_TESTAMENT)
        @Retention(AnnotationRetention.SOURCE)
        private annotation class Selection
    }

    sealed class ViewEvent {
        data class SelectChapter(val bookIndex: Int, val chapterIndex: Int) : ViewEvent()
    }

    data class ViewState(
        val currentBookIndex: Int,
        val currentChapterIndex: Int,
        val chapterSelectionItems: List<ChapterSelectionItem>,
    ) {
        companion object {
            val INVALID: ViewState = ViewState(currentBookIndex = -1, currentChapterIndex = -1, chapterSelectionItems = emptyList())
        }

        data class ChapterSelectionItem(val bookIndex: Int, val bookName: String, val chapterCount: Int)

        fun isValid(): Boolean =
            currentBookIndex in 0 until Bible.BOOK_COUNT &&
                currentChapterIndex in 0 until Bible.getChapterCount(currentBookIndex) &&
                chapterSelectionItems.size == Bible.BOOK_COUNT
    }

    private val viewBinding = InnerChapterSelectionViewBinding.inflate(LayoutInflater.from(context), this)

    @Selection
    private var selection = SELECTION_OLD_TESTAMENT

    private var viewState: ViewState = ViewState.INVALID

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        setBackgroundColor(Color.BLACK)

        viewBinding.chapterSelectionOldTestament.setOnClickListener { updateSelection(SELECTION_OLD_TESTAMENT, true) }
        viewBinding.chapterSelectionNewTestament.setOnClickListener { updateSelection(SELECTION_NEW_TESTAMENT, true) }
        viewBinding.chapterListView.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {}

            override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                val firstVisibleGroup = ExpandableListView.getPackedPositionGroup(
                    viewBinding.chapterListView.getExpandableListPosition(firstVisibleItem))
                updateSelection(if (firstVisibleGroup < Bible.OLD_TESTAMENT_COUNT) SELECTION_OLD_TESTAMENT else SELECTION_NEW_TESTAMENT, false)
            }
        })
        updateSelection(SELECTION_OLD_TESTAMENT, false)
    }

    private fun updateSelection(@Selection newSelection: Int, scrollToFirstItem: Boolean) {
        selection = newSelection
        when (selection) {
            SELECTION_OLD_TESTAMENT -> {
                viewBinding.chapterSelectionOldTestament.isSelected = true
                viewBinding.chapterSelectionNewTestament.isSelected = false
                if (scrollToFirstItem) viewBinding.chapterListView.scrollToPosition(0)
            }
            SELECTION_NEW_TESTAMENT -> {
                viewBinding.chapterSelectionOldTestament.isSelected = false
                viewBinding.chapterSelectionNewTestament.isSelected = true
                if (scrollToFirstItem) viewBinding.chapterListView.scrollToPosition(Bible.OLD_TESTAMENT_COUNT)
            }
            else -> throw IllegalArgumentException("Unsupported selection type - $selection")
        }
    }

    fun initialize(onViewEvent: (ViewEvent) -> Unit) {
        viewBinding.chapterListView.initialize(onViewEvent)
    }

    fun setViewState(viewState: ViewState) {
        this.viewState = viewState
        viewBinding.chapterListView.setViewState(viewState)
    }

    fun expandCurrentBook() {
        if (viewState.isValid()) {
            viewBinding.chapterListView.expandBook(viewState.currentBookIndex)
        }
    }
}
