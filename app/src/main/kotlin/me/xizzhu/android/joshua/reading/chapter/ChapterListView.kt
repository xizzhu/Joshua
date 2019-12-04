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

package me.xizzhu.android.joshua.reading.chapter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ExpandableListView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.core.VerseIndex

class ChapterListView : ExpandableListView, ExpandableListView.OnGroupClickListener {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    private lateinit var adapter: ChapterListAdapter
    private var lastExpandedGroup: Int = -1

    init {
        setBackgroundColor(Color.BLACK)
        divider = ColorDrawable(ContextCompat.getColor(context, android.R.color.darker_gray))
        dividerHeight = 1
        setGroupIndicator(null)
        setOnGroupClickListener(this)
        setChildDivider(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onGroupClick(parent: ExpandableListView, v: View, groupPosition: Int, id: Long): Boolean {
        smoothScrollToPosition(groupPosition)
        if (isGroupExpanded(groupPosition)) {
            collapseGroup(groupPosition)
        } else {
            expandGroup(groupPosition)
            if (lastExpandedGroup != groupPosition) {
                collapseGroup(lastExpandedGroup)
                lastExpandedGroup = groupPosition
            }
        }
        return true
    }

    fun setOnChapterSelectedListener(onChapterSelected: (Int, Int) -> Unit) {
        adapter = ChapterListAdapter(context, onChapterSelected)
        setAdapter(adapter)
    }

    fun setData(currentVerseIndex: VerseIndex, bookNames: List<String>) {
        adapter.setData(currentVerseIndex, bookNames)

        val currentBookIndex = currentVerseIndex.bookIndex
        lastExpandedGroup = currentBookIndex
        expandGroup(currentBookIndex)
        setSelectedGroup(currentBookIndex)
    }
}

private data class ChapterTag(var bookIndex: Int, var chapterIndex: Int)

private class ChapterListAdapter(context: Context, private val onChapterSelected: (Int, Int) -> Unit)
    : BaseExpandableListAdapter() {
    companion object {
        private const val ROW_CHILD_COUNT = 5
    }

    private val inflater = LayoutInflater.from(context)

    private val onChapterClickedListener = View.OnClickListener { view ->
        val chapterTag = view.tag as ChapterTag
        if (chapterTag.bookIndex != currentVerseIndex.bookIndex
                || chapterTag.chapterIndex != currentVerseIndex.chapterIndex) {
            onChapterSelected(chapterTag.bookIndex, chapterTag.chapterIndex)
        }
    }

    private val bookNames = ArrayList<String>()
    private var currentVerseIndex = VerseIndex.INVALID

    fun setData(currentVerseIndex: VerseIndex, bookNames: List<String>) {
        this.currentVerseIndex = currentVerseIndex

        this.bookNames.clear()
        this.bookNames.addAll(bookNames)

        notifyDataSetChanged()
    }

    override fun hasStableIds(): Boolean = false

    override fun getGroupCount(): Int = bookNames.size

    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

    override fun getGroup(groupPosition: Int): String = bookNames[groupPosition]

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        val textView = (convertView
                ?: inflater.inflate(R.layout.item_book_name, parent, false)) as TextView
        textView.text = getGroup(groupPosition)
        return textView
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true

    override fun getChildrenCount(groupPosition: Int): Int {
        val chapterCount = Bible.getChapterCount(groupPosition)
        return chapterCount / ROW_CHILD_COUNT + if (chapterCount % ROW_CHILD_COUNT == 0) 0 else 1
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long =
            (groupPosition * 1000 + childPosition).toLong()

    override fun getChild(groupPosition: Int, childPosition: Int): Int = childPosition

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        val linearLayout: LinearLayout
        if (convertView == null) {
            linearLayout = (inflater.inflate(R.layout.item_chapter_row, parent, false) as LinearLayout).apply {
                for (i in 0 until childCount) {
                    with(getChildAt(i)) {
                        setOnClickListener(onChapterClickedListener)
                        tag = ChapterTag(-1, -1)
                    }
                }
            }
        } else {
            linearLayout = convertView as LinearLayout
        }

        val chapterCount = Bible.getChapterCount(groupPosition)
        for (i in 0 until ROW_CHILD_COUNT) {
            val chapter = childPosition * ROW_CHILD_COUNT + i
            with(linearLayout.getChildAt(i) as TextView) {
                if (chapter >= chapterCount) {
                    visibility = View.GONE
                } else {
                    visibility = View.VISIBLE
                    isSelected = groupPosition == currentVerseIndex.bookIndex
                            && chapter == currentVerseIndex.chapterIndex
                    text = (chapter + 1).toString()

                    (tag as ChapterTag).apply {
                        bookIndex = groupPosition
                        chapterIndex = chapter
                    }
                }
            }
        }

        return linearLayout
    }
}
