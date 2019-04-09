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

package me.xizzhu.android.joshua.notes

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.ui.getBodyTextSize
import me.xizzhu.android.joshua.ui.getCaptionTextSize
import me.xizzhu.android.joshua.ui.getPrimaryTextColor

class NotesListAdapter(context: Context, private val listener: Listener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    interface Listener {
        fun onNoteClicked(verseIndex: VerseIndex)
    }

    private val inflater = LayoutInflater.from(context)
    private val resources = context.resources

    private var notes: Notes? = null
    private var settings: Settings? = null

    fun setNotes(notes: Notes) {
        this.notes = notes
        notifyDataSetChanged()
    }

    fun setSettings(settings: Settings) {
        this.settings = settings
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = if (notes == null || settings == null) 0 else notes!!.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            NoteItemViewHolder(inflater, parent, resources, listener)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as NoteItemViewHolder).bind(notes!![position], settings!!)
    }
}

private class NoteItemViewHolder(inflater: LayoutInflater, parent: ViewGroup,
                                 private val resources: Resources, private val listener: NotesListAdapter.Listener)
    : RecyclerView.ViewHolder(inflater.inflate(R.layout.item_note, parent, false)), View.OnClickListener {
    private val verse: TextView = itemView.findViewById(R.id.verse)
    private val text: TextView = itemView.findViewById(R.id.text)
    private var currentNote: NoteForDisplay? = null

    init {
        itemView.setOnClickListener(this)
    }

    fun bind(note: NoteForDisplay, settings: Settings) {
        currentNote = note

        val textColor = settings.getPrimaryTextColor(resources)
        with(verse) {
            setTextColor(textColor)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, settings.getCaptionTextSize(resources).toFloat())
            text = note.getVerseForDisplay()
        }
        with(text) {
            setTextColor(textColor)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, settings.getBodyTextSize(resources).toFloat())
            text = note.note
        }
    }

    override fun onClick(v: View) {
        currentNote?.let { note -> listener.onNoteClicked(note.verseIndex) }
    }
}
