/*
 * Copyright (C) 2021 Xizhi Zhu
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
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.annotation.IntDef
import androidx.annotation.RequiresApi
import com.google.android.material.tabs.TabLayoutMediator
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.databinding.InnerVerseDetailViewBinding
import me.xizzhu.android.joshua.reading.VerseDetailViewData
import me.xizzhu.android.joshua.ui.*

class VerseDetailViewLayout : FrameLayout {
    companion object {
        const val VERSE_DETAIL_VERSES = 0
        const val VERSE_DETAIL_NOTE = 1
        const val VERSE_DETAIL_STRONG_NUMBER = 2

        @IntDef(VERSE_DETAIL_VERSES, VERSE_DETAIL_NOTE, VERSE_DETAIL_STRONG_NUMBER)
        @Retention(AnnotationRetention.SOURCE)
        annotation class VerseDetail

        private val ON_COLOR_FILTER = PorterDuffColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY)
        private val OFF_COLOR_FILTER = PorterDuffColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY)
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    private val adapter = VerseDetailPagerAdapter(context)
    private val viewBinding = InnerVerseDetailViewBinding.inflate(LayoutInflater.from(context), this).apply {
        viewPager.adapter = adapter
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = adapter.pageTitle(position)
        }.attach()

        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                translationY = height.toFloat()
            }
        })
    }

    var verseDetail: VerseDetailViewData?
        get() = adapter.verseDetail
        set(value) {
            adapter.verseDetail = value
            viewBinding.bookmark.colorFilter = if (verseDetail?.bookmarked == true) ON_COLOR_FILTER else OFF_COLOR_FILTER
            viewBinding.highlight.colorFilter = if (verseDetail?.highlightColor != Highlight.COLOR_NONE) ON_COLOR_FILTER else OFF_COLOR_FILTER
        }

    fun initialize(
            onClicked: () -> Boolean, updateBookmark: (VerseIndex, Boolean) -> Unit,
            updateHighlight: (VerseIndex, Int) -> Unit, updateNote: (VerseIndex, String) -> Unit,
            requestStrongNumber: () -> Unit, hide: Boolean
    ) {
        setOnClickListener { onClicked() }
        viewBinding.bookmark.setOnClickListener {
            adapter.verseDetail?.let { updateBookmark(it.verseIndex, it.bookmarked) }
        }
        viewBinding.highlight.setOnClickListener {
            adapter.verseDetail?.let { updateHighlight(it.verseIndex, it.highlightColor) }
        }
        adapter.initialize(
                updateNote = updateNote,
                requestStrongNumber = requestStrongNumber
        )

        if (hide) post { hide() }
    }

    fun setSettings(settings: Settings) {
        adapter.settings = settings

        viewBinding.header.setBackgroundColor(if (settings.nightModeOn) 0xFF222222.toInt() else 0xFFEEEEEE.toInt())
        viewBinding.viewPager.setBackgroundColor(settings.getBackgroundColor())
        resources.let { viewBinding.tabLayout.setTabTextColors(settings.getSecondaryTextColor(it), settings.getPrimaryTextColor(it)) }
    }

    fun setBookmarked(bookmarked: Boolean) {
        adapter.verseDetail?.bookmarked = bookmarked
        viewBinding.bookmark.colorFilter = if (bookmarked) ON_COLOR_FILTER else OFF_COLOR_FILTER
    }

    fun setHighlightColor(@Highlight.Companion.AvailableColor highlightColor: Int) {
        adapter.verseDetail?.highlightColor = highlightColor
        viewBinding.highlight.colorFilter = if (highlightColor != Highlight.COLOR_NONE) ON_COLOR_FILTER else OFF_COLOR_FILTER
    }

    fun setNote(note: String) {
        adapter.verseDetail?.note = note
    }

    fun setStrongNumbers(strongNumbers: List<StrongNumberItem>) {
        adapter.verseDetail?.strongNumberItems = strongNumbers
        adapter.notifyItemChanged(VerseDetailPagerAdapter.PAGE_STRONG_NUMBER)
    }

    fun show(@VerseDetail verseDetail: Int) {
        animate().translationY(0.0F)
        viewBinding.viewPager.currentItem = when (verseDetail) {
            VERSE_DETAIL_VERSES -> VerseDetailPagerAdapter.PAGE_VERSES
            VERSE_DETAIL_NOTE -> VerseDetailPagerAdapter.PAGE_NOTE
            VERSE_DETAIL_STRONG_NUMBER -> VerseDetailPagerAdapter.PAGE_STRONG_NUMBER
            else -> 0
        }
    }

    /**
     * @return true if verse detail view was open, or false otherwise
     */
    fun hide(): Boolean {
        animate().translationY(height.toFloat())
        hideKeyboard()

        return adapter.verseDetail?.let {
            adapter.verseDetail = null
            true
        } ?: false
    }
}
