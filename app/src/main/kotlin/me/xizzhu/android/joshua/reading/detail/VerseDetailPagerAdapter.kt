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

package me.xizzhu.android.joshua.reading.detail

import android.content.Context
import android.content.res.Resources
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.google.android.material.textfield.TextInputEditText
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.ui.getBodyTextSize

class VerseDetailPagerAdapter(context: Context, private val listener: Listener) : PagerAdapter() {
    interface Listener {
        fun onNoteUpdated(note: String)
    }

    companion object {
        private const val PAGE_VERSES = 0
        private const val PAGE_NOTE = 1
        private const val PAGE_COUNT = 2
    }

    private val resources: Resources = context.resources
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private var settings: Settings? = null
    private var verseDetail: VerseDetail? = null

    fun setSettings(settings: Settings) {
        this.settings = settings
        notifyDataSetChanged()
    }

    fun setVerse(verseDetail: VerseDetail) {
        this.verseDetail = verseDetail
        notifyDataSetChanged()
    }

    override fun getCount(): Int = if (settings != null && verseDetail != null) PAGE_COUNT else 0

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = when (position) {
            PAGE_VERSES -> createVerseDetailView(container)
            PAGE_NOTE -> createNoteView(container)
            else -> throw IllegalArgumentException("Unsupported position: $position")
        }
        container.addView(view)
        return view
    }

    private fun createVerseDetailView(container: ViewGroup): View {
        return inflater.inflate(R.layout.page_verse_detail_verses, container, false).apply {
            with(findViewById<TextView>(R.id.detail)) {
                setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        settings!!.getBodyTextSize(this@VerseDetailPagerAdapter.resources).toFloat())
                text = verseDetail!!.getTextForDisplay()
            }
        }
    }

    private fun createNoteView(container: ViewGroup): View {
        return inflater.inflate(R.layout.page_verse_detail_note, container, false).apply {
            with(findViewById<TextInputEditText>(R.id.note)) {
                addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

                    override fun afterTextChanged(s: Editable) {
                        listener.onNoteUpdated(s.toString())
                    }
                })

                setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        settings!!.getBodyTextSize(this@VerseDetailPagerAdapter.resources).toFloat())
                setText(verseDetail!!.note)
            }
        }
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View)
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            PAGE_VERSES -> resources.getString(R.string.text_verse_comparison)
            PAGE_NOTE -> resources.getString(R.string.text_note)
            else -> ""
        }
    }

    override fun getItemPosition(obj: Any): Int = POSITION_NONE
}
