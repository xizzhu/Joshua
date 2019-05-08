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
import android.content.DialogInterface
import android.util.AttributeSet
import android.view.View
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.ui.DialogHelper
import me.xizzhu.android.joshua.ui.recyclerview.BaseRecyclerView
import me.xizzhu.android.joshua.ui.recyclerview.NoteItem
import me.xizzhu.android.joshua.ui.recyclerview.NoteItemViewHolder
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import me.xizzhu.android.joshua.utils.BaseSettingsView

interface NotesView : BaseSettingsView {
    fun onNotesLoaded(notes: List<NoteItem>)

    fun onNoNotesAvailable()

    fun onNotesLoadFailed()

    fun onVerseSelectionFailed(verseToSelect: VerseIndex)
}

class NotesListView : BaseRecyclerView, NotesView {
    private lateinit var presenter: NotesPresenter
    private val onClickListener = OnClickListener { view ->
        ((getChildViewHolder(view) as NoteItemViewHolder).item)?.let {
            presenter.selectVerse(it.verseIndex)
        }
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun setPresenter(presenter: NotesPresenter) {
        this.presenter = presenter
    }

    override fun onChildAttachedToWindow(child: View) {
        super.onChildAttachedToWindow(child)
        if (getChildViewHolder(child) is NoteItemViewHolder) {
            child.setOnClickListener(onClickListener)
        }
    }

    override fun onChildDetachedFromWindow(child: View) {
        super.onChildDetachedFromWindow(child)
        child.setOnClickListener(null)
    }

    override fun onNotesLoaded(notes: List<NoteItem>) {
        setItems(notes)
    }

    override fun onNoNotesAvailable() {
        setItems(listOf(TitleItem(resources.getString(R.string.text_no_note))))
    }

    override fun onNotesLoadFailed() {
        DialogHelper.showDialog(context, true, R.string.dialog_load_notes_error,
                DialogInterface.OnClickListener { _, _ ->
                    presenter.loadNotes()
                })
    }

    override fun onVerseSelectionFailed(verseToSelect: VerseIndex) {
        DialogHelper.showDialog(context, true, R.string.dialog_verse_selection_error,
                DialogInterface.OnClickListener { _, _ ->
                    presenter.selectVerse(verseToSelect)
                })
    }
}
