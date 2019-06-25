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
import android.os.Handler
import android.util.AttributeSet
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.BaseRecyclerView

class VerseListView : BaseRecyclerView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun selectVerse(verseIndex: VerseIndex) {
        adapter?.let { adapter ->
            if (adapter.itemCount == 0) {
                // this is the case when reading activity is opened from notes list, and this is likely
                // called before adapter is updated
                adapter.registerAdapterDataObserver(object : AdapterDataObserver() {
                    override fun onChanged() {
                        adapter.unregisterAdapterDataObserver(this)
                        Handler().post {
                            adapter.notifyItemChanged(verseIndex.verseIndex, VerseItemViewHolder.VERSE_SELECTED)
                        }
                    }
                })
            } else {
                adapter.notifyItemChanged(verseIndex.verseIndex, VerseItemViewHolder.VERSE_SELECTED)
            }
        }
    }

    fun deselectVerse(verseIndex: VerseIndex) {
        adapter?.notifyItemChanged(verseIndex.verseIndex, VerseItemViewHolder.VERSE_DESELECTED)
    }

    fun notifyVerseUpdate(verseIndex: VerseIndex, operation: Int) {
        adapter?.notifyItemChanged(verseIndex.verseIndex, operation)
    }

    fun setVerses(verses: List<BaseItem>) {
        setItems(verses)
    }
}
