/*
 * Copyright (C) 2023 Xizhi Zhu
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
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.util.AttributeSet
import android.widget.ExpandableListView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

class ChapterListView : ExpandableListView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    private lateinit var adapter: ChapterListAdapter
    private var lastExpandedGroup: Int = -1

    init {
        divider = ColorDrawable(ContextCompat.getColor(context, android.R.color.darker_gray))
        dividerHeight = 1

        setGroupIndicator(null)
        setOnGroupClickListener { _, _, groupPosition, _ ->
            if (isGroupExpanded(groupPosition)) {
                collapseGroup(groupPosition)
            } else {
                expandBook(groupPosition)
            }

            return@setOnGroupClickListener true
        }

        setChildDivider(ColorDrawable(Color.TRANSPARENT))
    }

    fun initialize(onViewEvent: (ChapterSelectionView.ViewEvent) -> Unit) {
        adapter = ChapterListAdapter(context, onViewEvent)
        setAdapter(adapter)
    }

    fun setViewState(viewState: ChapterSelectionView.ViewState) {
        adapter.setViewState(viewState)
        expandBook(viewState.currentBookIndex)
    }

    fun expandBook(bookIndex: Int) {
        val groupToExpand = bookIndex
        if (isGroupExpanded(groupToExpand)) {
            return
        }

        expandGroup(groupToExpand)
        if (lastExpandedGroup != groupToExpand) {
            collapseGroup(lastExpandedGroup)
            lastExpandedGroup = groupToExpand
        }
        setSelectedGroup(groupToExpand)
    }

    fun scrollToPosition(position: Int) {
        smoothScrollToPositionFromTop(position, 0, 100)
    }
}
