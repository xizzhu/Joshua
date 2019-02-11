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

class ChapterSelectionView : ExpandableListView, ExpandableListView.OnGroupClickListener {
    interface Listener {
        fun onChapterSelected(currentVerseIndex: VerseIndex)
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    private val adapter = ChapterListAdapter(context)
    private var lastExpandedGroup: Int = -1

    init {
        setBackgroundColor(Color.BLACK)
        divider = ColorDrawable(ContextCompat.getColor(context, android.R.color.darker_gray))
        dividerHeight = 1
        setGroupIndicator(null)
        setOnGroupClickListener(this)
        setChildDivider(ColorDrawable(Color.TRANSPARENT))

        setAdapter(adapter)
    }

    fun setListener(listener: Listener) {
        adapter.listener = listener
    }

    fun setBookNames(bookNames: List<String>) {
        adapter.bookNames = bookNames
        refreshUi()
    }

    fun setCurrentVerseIndex(currentVerseIndex: VerseIndex) {
        adapter.currentVerseIndex = currentVerseIndex
        refreshUi()
    }

    private fun refreshUi() {
        if (adapter.bookNames.isEmpty() || !adapter.currentVerseIndex.isValid()) {
            return
        }
        adapter.notifyDataSetChanged()

        val currentBookIndex = adapter.currentVerseIndex.bookIndex
        lastExpandedGroup = currentBookIndex
        expandGroup(currentBookIndex)
        setSelectedGroup(currentBookIndex)
    }

    override fun onGroupClick(parent: ExpandableListView, v: View, groupPosition: Int, id: Long): Boolean {
        parent.smoothScrollToPosition(groupPosition)
        if (parent.isGroupExpanded(groupPosition)) {
            parent.collapseGroup(groupPosition)
        } else {
            parent.expandGroup(groupPosition)
            if (lastExpandedGroup != groupPosition) {
                parent.collapseGroup(lastExpandedGroup)
                lastExpandedGroup = groupPosition
            }
        }

        return true
    }
}

private data class ViewTag(val textViews: Array<TextView>)
private data class ChapterTag(var bookIndex: Int, var chapterIndex: Int)

private class ChapterListAdapter(context: Context) : BaseExpandableListAdapter(), View.OnClickListener {
    companion object {
        const val ROW_CHILD_COUNT = 5
    }

    private val inflater = LayoutInflater.from(context)

    var bookNames: List<String> = emptyList()
    var currentVerseIndex = VerseIndex.INVALID

    var listener: ChapterSelectionView.Listener? = null

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
        val viewTag: ViewTag
        if (convertView == null) {
            linearLayout = inflater.inflate(R.layout.item_chapter_row, parent, false) as LinearLayout

            val textViews = Array(ROW_CHILD_COUNT) {
                val textView = linearLayout.getChildAt(it) as TextView
                textView.setOnClickListener(this@ChapterListAdapter)
                textView.tag = ChapterTag(-1, -1)
                textView
            }
            viewTag = ViewTag(textViews)
            linearLayout.tag = viewTag
        } else {
            linearLayout = convertView as LinearLayout
            viewTag = linearLayout.tag as ViewTag
        }

        val chapterCount = Bible.getChapterCount(groupPosition)
        for (i in 0 until ROW_CHILD_COUNT) {
            val chapter = childPosition * ROW_CHILD_COUNT + i
            val textView = viewTag.textViews[i]
            if (chapter >= chapterCount) {
                textView.visibility = View.GONE
            } else {
                textView.visibility = View.VISIBLE
                textView.isSelected = groupPosition == currentVerseIndex.bookIndex
                        && chapter == currentVerseIndex.chapterIndex
                textView.text = (chapter + 1).toString()

                val chapterTag = textView.tag as ChapterTag
                chapterTag.bookIndex = groupPosition
                chapterTag.chapterIndex = chapter
            }
        }

        return linearLayout
    }

    override fun onClick(v: View) {
        val chapterTag = v.tag as ChapterTag
        if (chapterTag.bookIndex != currentVerseIndex.bookIndex
                || chapterTag.chapterIndex != currentVerseIndex.chapterIndex) {
            listener?.onChapterSelected(VerseIndex(chapterTag.bookIndex, chapterTag.chapterIndex, 0))
        }
    }
}
