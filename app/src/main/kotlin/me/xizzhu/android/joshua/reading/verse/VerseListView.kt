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
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.VerseIndex

class VerseListView : RecyclerView {
    private val adapter = VerseListAdapter(context)
    private lateinit var listener: VersePagerAdapter.Listener

    private val onClickListener = OnClickListener { view ->
        adapter.getVerse(getChildAdapterPosition(view))?.let {
            listener.onVerseClicked(it)
        }
    }
    private val onLongClickListener = OnLongClickListener { view ->
        adapter.getVerse(getChildAdapterPosition(view))?.let {
            listener.onVerseLongClicked(it)
            return@OnLongClickListener true
        }
        return@OnLongClickListener false
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        layoutManager = LinearLayoutManager(context, VERTICAL, false)
        setAdapter(adapter)
    }

    fun setListener(listener: VersePagerAdapter.Listener) {
        this.listener = listener
    }

    fun selectVerse(verseIndex: VerseIndex) {
        adapter.selectVerse(verseIndex)
    }

    fun deselectVerse(verseIndex: VerseIndex) {
        adapter.deselectVerse(verseIndex)
    }

    fun setSettings(settings: Settings) {
        adapter.setSettings(settings)
    }

    fun setVerses(verses: List<VerseForReading>) {
        adapter.setVerses(verses)
    }

    override fun onChildAttachedToWindow(child: View) {
        super.onChildAttachedToWindow(child)
        child.setOnClickListener(onClickListener)
        child.setOnLongClickListener(onLongClickListener)
    }

    override fun onChildDetachedFromWindow(child: View) {
        super.onChildDetachedFromWindow(child)
        child.setOnClickListener(null)
        child.setOnLongClickListener(null)
    }
}
