/*
 * Copyright (C) 2020 Xizhi Zhu
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
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.reading.VerseDetailRequest
import me.xizzhu.android.joshua.reading.detail.pages.VerseDetailPagerAdapter
import me.xizzhu.android.joshua.ui.*

class VerseDetailViewLayout : FrameLayout {
    companion object {
        private val ON_COLOR_FILTER = PorterDuffColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY)
        private val OFF_COLOR_FILTER = PorterDuffColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY)
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    private val adapter = VerseDetailPagerAdapter(context)
    private val header: LinearLayout
    private val tabLayout: TabLayout
    private val viewPager: ViewPager2
    private val highlight: ImageView
    private val bookmark: ImageView

    init {
        View.inflate(context, R.layout.inner_verse_detail_view, this)
        header = findViewById(R.id.header)
        viewPager = findViewById<ViewPager2>(R.id.view_pager).apply { adapter = this@VerseDetailViewLayout.adapter }
        tabLayout = findViewById<TabLayout>(R.id.tab_layout).apply {
            TabLayoutMediator(this, viewPager) { tab, position ->
                tab.text = adapter.pageTitle(position)
            }.attach()
        }
        highlight = findViewById(R.id.highlight)
        bookmark = findViewById(R.id.bookmark)

        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                translationY = height.toFloat()
            }
        })
    }

    fun setListeners(onClicked: () -> Unit, onBookmarkClicked: () -> Unit, onHighlightClicked: () -> Unit,
                     onNoteUpdated: (String) -> Unit, onNoStrongNumberClicked: () -> Unit) {
        setOnClickListener { onClicked() }
        bookmark.setOnClickListener { onBookmarkClicked() }
        highlight.setOnClickListener { onHighlightClicked() }
        adapter.onNoteUpdated = onNoteUpdated
        adapter.onNoStrongNumberClicked = onNoStrongNumberClicked
    }

    fun setSettings(settings: Settings) {
        adapter.settings = settings

        header.setBackgroundColor(if (settings.nightModeOn) 0xFF222222.toInt() else 0xFFEEEEEE.toInt())
        viewPager.setBackgroundColor(settings.getBackgroundColor())
        resources.let { tabLayout.setTabTextColors(settings.getSecondaryTextColor(it), settings.getPrimaryTextColor(it)) }
    }

    fun setVerseDetail(verseDetail: VerseDetail) {
        adapter.verseDetail = verseDetail
        bookmark.colorFilter = if (verseDetail.bookmarked) ON_COLOR_FILTER else OFF_COLOR_FILTER
        highlight.colorFilter = if (verseDetail.highlightColor != Highlight.COLOR_NONE) ON_COLOR_FILTER else OFF_COLOR_FILTER
    }

    fun show(@VerseDetailRequest.Companion.Content content: Int) {
        animate().translationY(0.0F)
        viewPager.currentItem = when (content) {
            VerseDetailRequest.VERSES -> VerseDetailPagerAdapter.PAGE_VERSES
            VerseDetailRequest.NOTE -> VerseDetailPagerAdapter.PAGE_NOTE
            VerseDetailRequest.STRONG_NUMBER -> VerseDetailPagerAdapter.PAGE_STRONG_NUMBER
            else -> 0
        }
    }

    fun hide() {
        animate().translationY(height.toFloat())
        hideKeyboard()
    }
}
