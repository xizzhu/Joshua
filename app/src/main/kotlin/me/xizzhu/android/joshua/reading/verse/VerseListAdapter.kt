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
import android.content.res.Resources
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.IntDef
import androidx.recyclerview.widget.RecyclerView
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.ui.getBodyTextSize
import me.xizzhu.android.joshua.ui.getPrimaryTextColor

class VerseListAdapter(context: Context, private val inflater: LayoutInflater) : RecyclerView.Adapter<VerseItemViewHolder>() {
    companion object {
        const val VERSE_SELECTED = 1
        const val VERSE_DESELECTED = 2

        @IntDef(VERSE_SELECTED, VERSE_DESELECTED)
        @Retention(AnnotationRetention.SOURCE)
        annotation class Payload
    }

    private val resources = context.resources
    private val verses = ArrayList<VerseForReading>()
    private val selected = ArrayList<Boolean>()
    private var settings: Settings? = null

    fun setVerses(verses: List<VerseForReading>) {
        this.verses.clear()
        this.verses.addAll(verses)

        selected.ensureCapacity(verses.size)
        for (i in 0 until selected.size) {
            selected[i] = false
        }
        for (i in selected.size until verses.size) {
            selected.add(false)
        }

        notifyDataSetChanged()
    }

    fun getVerse(position: Int): VerseForReading? = verses.getOrNull(position)

    fun setSettings(settings: Settings) {
        this.settings = settings
        notifyDataSetChanged()
    }

    fun selectVerse(verseIndex: VerseIndex) {
        selected[verseIndex.verseIndex] = true
        notifyItemChanged(verseIndex.verseIndex, VERSE_SELECTED)
    }

    fun deselectVerse(verseIndex: VerseIndex) {
        selected[verseIndex.verseIndex] = false
        notifyItemChanged(verseIndex.verseIndex, VERSE_DESELECTED)
    }

    override fun getItemCount(): Int = if (settings != null) verses.size else 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerseItemViewHolder =
            VerseItemViewHolder(inflater, parent, resources)

    override fun onBindViewHolder(holder: VerseItemViewHolder, position: Int) {
        holder.bind(verses[position], selected[position], settings!!)
    }

    override fun onBindViewHolder(holder: VerseItemViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
            return
        }

        payloads.forEach { payload ->
            when (payload as Int) {
                VERSE_SELECTED -> holder.setSelected(true)
                VERSE_DESELECTED -> holder.setSelected(false)
            }
        }
    }
}

class VerseItemViewHolder(inflater: LayoutInflater, parent: ViewGroup, private val resources: Resources)
    : RecyclerView.ViewHolder(inflater.inflate(R.layout.item_verse, parent, false)) {
    private val index = itemView.findViewById(R.id.index) as TextView
    private val text = itemView.findViewById(R.id.text) as TextView
    private val divider = itemView.findViewById(R.id.divider) as View

    fun bind(verse: VerseForReading, selected: Boolean, settings: Settings) {
        text.text = verse.textForDisplay

        val textColor = settings.getPrimaryTextColor(resources)
        val textSize = settings.getBodyTextSize(resources)
        text.setTextColor(textColor)
        text.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        if (verse.verse.parallel.isEmpty()) {
            index.text = verse.indexForDisplay
            index.setTextColor(textColor)
            index.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
            index.visibility = View.VISIBLE
            divider.visibility = View.GONE
        } else {
            index.visibility = View.GONE
            divider.visibility = View.VISIBLE
        }

        setSelected(selected)
    }

    fun setSelected(selected: Boolean) {
        itemView.isSelected = selected
    }
}
