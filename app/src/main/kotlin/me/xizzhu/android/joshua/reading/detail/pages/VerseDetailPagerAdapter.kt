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

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.reading.detail.VerseDetail

class VerseDetailPagerAdapter(context: Context) : PagerAdapter() {
    companion object {
        const val PAGE_VERSES = 0
        const val PAGE_NOTE = 1
        const val PAGE_STRONG_NUMBER = 2
        private const val PAGE_COUNT = 3
    }

    private val resources: Resources = context.resources
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val verseDetailPages: Array<VerseDetailPage?> = arrayOfNulls(PAGE_COUNT)

    var onNoteUpdated: ((String) -> Unit)? = null
    var settings: Settings? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var verseDetail: VerseDetail = VerseDetail.INVALID
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getCount(): Int = if (settings != null && onNoteUpdated != null) PAGE_COUNT else 0

    override fun instantiateItem(container: ViewGroup, position: Int): Any = verseDetailPages[position]
            ?: when (position) {
                PAGE_VERSES -> VersesPage(inflater, container, settings!!)
                PAGE_NOTE -> NotePage(resources, inflater, container, settings!!, onNoteUpdated!!)
                PAGE_STRONG_NUMBER -> StrongNumberPage(inflater, container, settings!!)
                else -> throw IllegalArgumentException("Unsupported position: $position")
            }.apply {
                bind(verseDetail)
                container.addView(view)
            }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView((obj as VerseDetailPage).view)
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == (obj as VerseDetailPage).view
    }

    override fun getPageTitle(position: Int): CharSequence = when (position) {
        PAGE_VERSES -> resources.getString(R.string.text_verse_comparison)
        PAGE_NOTE -> resources.getString(R.string.text_note)
        PAGE_STRONG_NUMBER -> resources.getString(R.string.text_strong_number)
        else -> ""
    }

    override fun getItemPosition(obj: Any): Int = POSITION_NONE
}
