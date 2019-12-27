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

package me.xizzhu.android.joshua.reading.detail.pages

import android.content.res.Resources
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.android.material.textfield.TextInputEditText
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.reading.detail.VerseDetail
import me.xizzhu.android.joshua.ui.getBodyTextSize
import me.xizzhu.android.joshua.ui.getPrimaryTextColor

class NotePage(resources: Resources, inflater: LayoutInflater, container: ViewGroup,
               settings: Settings, onNoteUpdated: (String) -> Unit)
    : VerseDetailPage(inflater.inflate(R.layout.page_verse_detail_note, container, false)) {
    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable) {
            onNoteUpdated(s.toString())
        }
    }

    private val note: TextInputEditText = view.findViewById<TextInputEditText>(R.id.note).apply {
        addTextChangedListener(textWatcher)
        setTextSize(TypedValue.COMPLEX_UNIT_PX, settings.getBodyTextSize(resources))
        setTextColor(settings.getPrimaryTextColor(resources))
    }

    override fun bind(verseDetail: VerseDetail) {
        with(note) {
            removeTextChangedListener(textWatcher)
            setText(verseDetail.note)
            addTextChangedListener(textWatcher)
        }
    }
}
