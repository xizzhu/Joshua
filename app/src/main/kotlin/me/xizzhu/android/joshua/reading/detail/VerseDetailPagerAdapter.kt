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
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.ui.getBodyTextSize

class VerseDetailPagerAdapter(context: Context) : PagerAdapter() {
    companion object {
        private const val PAGE_VERSES = 0
    }

    private val resources: Resources = context.resources
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private var settings: Settings? = null
    private var verseDetail: VerseDetail? = null

    override fun getCount(): Int {
        return if (verseDetail != null && settings != null) 1 else 0
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = inflater.inflate(R.layout.page_verse_detail, container, false)
        with(view.findViewById(R.id.detail) as TextView) {
            setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    settings!!.getBodyTextSize(this@VerseDetailPagerAdapter.resources).toFloat())
            text = when (position) {
                PAGE_VERSES -> verseDetail!!.getTextForDisplay()
                else -> ""
            }
        }

        container.addView(view)
        return view
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
            else -> ""
        }
    }

    override fun getItemPosition(obj: Any): Int = POSITION_NONE

    fun setSettings(settings: Settings) {
        this.settings = settings
        notifyDataSetChanged()
    }

    fun setVerse(verseDetail: VerseDetail) {
        this.verseDetail = verseDetail
        notifyDataSetChanged()
    }
}
