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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.isVisible
import me.xizzhu.android.joshua.databinding.ItemBookNameBinding
import me.xizzhu.android.joshua.databinding.ItemChapterRowBinding

class ChapterListAdapter(context: Context, onViewEvent: (ChapterSelectionView.ViewEvent) -> Unit) : BaseExpandableListAdapter() {
    companion object {
        private const val ROW_CHILD_COUNT = 5
    }

    private class Tag(var bookIndex: Int, var chapterIndex: Int)

    private val inflater = LayoutInflater.from(context)

    private val onChapterClickedListener = View.OnClickListener { view ->
        val tag = view.tag as Tag
        if (tag.bookIndex != viewState.currentBookIndex || tag.chapterIndex != viewState.currentChapterIndex) {
            onViewEvent(ChapterSelectionView.ViewEvent.SelectChapter(tag.bookIndex, tag.chapterIndex))
        }
    }

    private var viewState: ChapterSelectionView.ViewState = ChapterSelectionView.ViewState.INVALID

    fun setViewState(viewState: ChapterSelectionView.ViewState) {
        this.viewState = viewState
        notifyDataSetChanged()
    }

    override fun hasStableIds(): Boolean = false

    override fun getGroupCount(): Int = if (viewState.isValid()) viewState.chapterSelectionItems.size else 0

    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

    override fun getGroup(groupPosition: Int): ChapterSelectionView.ViewState.ChapterSelectionItem = viewState.chapterSelectionItems[groupPosition]

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        val binding = convertView?.let { ItemBookNameBinding.bind(it) }
            ?: ItemBookNameBinding.inflate(inflater, parent, false)
        binding.root.text = getGroup(groupPosition).bookName
        return binding.root
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true

    override fun getChildrenCount(groupPosition: Int): Int {
        val chapterCount = getGroup(groupPosition).chapterCount
        return chapterCount / ROW_CHILD_COUNT + if (chapterCount % ROW_CHILD_COUNT == 0) 0 else 1
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long = (groupPosition * 1000 + childPosition).toLong()

    override fun getChild(groupPosition: Int, childPosition: Int): Int = childPosition

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        val binding = getChildViewBinding(convertView, parent)
        binding.bind(groupPosition, childPosition)
        return binding.root
    }

    private fun getChildViewBinding(convertView: View?, parent: ViewGroup?): ItemChapterRowBinding {
        if (convertView != null) {
            return ItemChapterRowBinding.bind(convertView)
        }

        val binding = ItemChapterRowBinding.inflate(inflater, parent, false)
        binding.root.children.forEach { child ->
            child.setOnClickListener(onChapterClickedListener)
            child.tag = Tag(-1, -1)
        }
        return binding
    }

    private fun ItemChapterRowBinding.bind(groupPosition: Int, childPosition: Int) {
        val bookIndex = groupPosition
        val chapterCount = getGroup(groupPosition).chapterCount
        repeat(ROW_CHILD_COUNT) { i ->
            val chapterIndex = childPosition * ROW_CHILD_COUNT + i
            with(root.getChildAt(i) as TextView) {
                if (chapterIndex >= chapterCount) {
                    isVisible = false
                } else {
                    isVisible = true
                    isSelected = bookIndex == viewState.currentBookIndex && chapterIndex == viewState.currentChapterIndex
                    text = (chapterIndex + 1).toString()

                    with(tag as Tag) {
                        this.bookIndex = bookIndex
                        this.chapterIndex = chapterIndex
                    }
                }
            }
        }
    }
}
