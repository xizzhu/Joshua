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

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Verse
import java.lang.StringBuilder

class VerseListAdapter(private val inflater: LayoutInflater) : RecyclerView.Adapter<VerseItemViewHolder>() {
    private val verses = ArrayList<Verse>()

    fun setVerses(verses: List<Verse>) {
        this.verses.clear()
        this.verses.addAll(verses)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = verses.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerseItemViewHolder =
            VerseItemViewHolder(inflater, parent)

    override fun onBindViewHolder(holder: VerseItemViewHolder, position: Int) {
        holder.bind(verses[position])
    }
}

class VerseItemViewHolder(inflater: LayoutInflater, parent: ViewGroup)
    : RecyclerView.ViewHolder(inflater.inflate(R.layout.item_verse, parent, false)) {
    private val stringBuilder = StringBuilder()
    private val index = itemView.findViewById(R.id.index) as TextView
    private val text = itemView.findViewById(R.id.text) as TextView

    fun bind(verse: Verse) {
        stringBuilder.setLength(0)
        val verseIndex = verse.verseIndex.verseIndex
        if (verseIndex + 1 < 10) {
            stringBuilder.append("  ")
        } else if (verseIndex + 1 < 100) {
            stringBuilder.append(" ")
        }
        stringBuilder.append(verseIndex + 1)
        index.text = stringBuilder.toString()

        text.text = verse.text.text
    }
}
