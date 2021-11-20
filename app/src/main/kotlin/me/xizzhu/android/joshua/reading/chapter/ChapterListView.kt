/*
 * Copyright (C) 2021 Xizhi Zhu
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
import android.widget.TextView
import androidx.core.content.ContextCompat
import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.databinding.ItemBookNameBinding
import me.xizzhu.android.joshua.databinding.ItemChapterRowBinding

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
        if (isGroupExpanded(groupPosition)) {
            collapseGroup(groupPosition)
        } else {
            expandGroup(groupPosition)
            if (lastExpandedGroup != groupPosition) {
                collapseGroup(lastExpandedGroup)
                lastExpandedGroup = groupPosition
            }
        }
        smoothScrollToPosition(groupPosition)
        return true
    }

    fun initialize(selectChapter: (Int, Int) -> Unit) {
        adapter = ChapterListAdapter(context, selectChapter)
        setAdapter(adapter)
    }

    fun setData(currentVerseIndex: VerseIndex, bookNames: List<String>) {
        adapter.setData(currentVerseIndex, bookNames)
        expandBook(currentVerseIndex.bookIndex)
    }

    fun expandBook(bookIndex: Int) {
        lastExpandedGroup = bookIndex
        expandGroup(bookIndex)
        setSelectedGroup(bookIndex)
    }
}

private class ChapterTag(var bookIndex: Int, var chapterIndex: Int)

private class ChapterListAdapter(context: Context, selectChapter: (Int, Int) -> Unit) : BaseExpandableListAdapter() {
    companion object {
        private const val ROW_CHILD_COUNT = 5
    }

    private val inflater = LayoutInflater.from(context)

    private val onChapterClickedListener = View.OnClickListener { view ->
        val chapterTag = view.tag as ChapterTag
        if (chapterTag.bookIndex != currentVerseIndex.bookIndex
                || chapterTag.chapterIndex != currentVerseIndex.chapterIndex) {
            selectChapter(chapterTag.bookIndex, chapterTag.chapterIndex)
        }
    }

    private val bookNames = ArrayList<String>()
    private var currentVerseIndex = VerseIndex.INVALID

    override fun hasStableIds(): Boolean = false

    override fun getGroupCount(): Int = bookNames.size

    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

    override fun getGroup(groupPosition: Int): String = bookNames[groupPosition]

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View =
            (convertView?.let { ItemBookNameBinding.bind(it) } ?: ItemBookNameBinding.inflate(inflater, parent, false))
                    .apply { root.text = getGroup(groupPosition) }
                    .root

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true

    override fun getChildrenCount(groupPosition: Int): Int {
        val chapterCount = Bible.getChapterCount(groupPosition)
        return chapterCount / ROW_CHILD_COUNT + if (chapterCount % ROW_CHILD_COUNT == 0) 0 else 1
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long = (groupPosition * 1000 + childPosition).toLong()

    override fun getChild(groupPosition: Int, childPosition: Int): Int = childPosition

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        val chapterRow: ItemChapterRowBinding = if (convertView == null) {
            ItemChapterRowBinding.inflate(inflater, parent, false).apply {
                repeat(root.childCount) { i ->
                    with(root.getChildAt(i)) {
                        setOnClickListener(onChapterClickedListener)
                        tag = ChapterTag(-1, -1)
                    }
                }
            }
        } else {
            ItemChapterRowBinding.bind(convertView)
        }

        val chapterCount = Bible.getChapterCount(groupPosition)
        repeat(ROW_CHILD_COUNT) { i ->
            val chapter = childPosition * ROW_CHILD_COUNT + i
            with(chapterRow.root.getChildAt(i) as TextView) {
                if (chapter >= chapterCount) {
                    visibility = View.GONE
                } else {
                    visibility = View.VISIBLE
                    isSelected = groupPosition == currentVerseIndex.bookIndex
                            && chapter == currentVerseIndex.chapterIndex
                    text = (chapter + 1).toString()

                    with((tag as ChapterTag)) {
                        bookIndex = groupPosition
                        chapterIndex = chapter
                    }
                }
            }
        }

        return chapterRow.root
    }

    fun setData(currentVerseIndex: VerseIndex, bookNames: List<String>) {
        this.currentVerseIndex = currentVerseIndex

        this.bookNames.clear()
        this.bookNames.addAll(bookNames)

        notifyDataSetChanged()
    }
}
