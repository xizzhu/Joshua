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

package me.xizzhu.android.joshua.annotated

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.annotated.bookmarks.list.BookmarkItem
import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.BaseViewHolder

data class TestVerseAnnotation(override val verseIndex: VerseIndex, override val timestamp: Long) : VerseAnnotation(verseIndex, timestamp)

data class TestVerseItem(val verseIndex: VerseIndex, val bookName: String,
                         val bookShortName: String, @Constants.SortOrder val sortOrder: Int)
    : BaseItem(R.layout.item_bookmark, { inflater, parent -> TestVerseItemViewHolder(inflater, parent) })

class TestVerseItemViewHolder(inflater: LayoutInflater, parent: ViewGroup)
    : BaseViewHolder<BookmarkItem>(inflater.inflate(R.layout.item_bookmark, parent, false)) {
    override fun bind(settings: Settings, item: BookmarkItem, payloads: List<Any>) {}
}

class TestVerseAnnotationPresenter(
        activity: BaseAnnotatedVersesActivity<TestVerseAnnotation>, navigator: Navigator,
        @StringRes noItemText: Int, annotatedVersesViewModel: BaseAnnotatedVersesViewModel<TestVerseAnnotation>,
        lifecycle: Lifecycle, lifecycleCoroutineScope: LifecycleCoroutineScope
) : BaseAnnotatedVersesPresenter<TestVerseAnnotation>(activity, navigator, noItemText, annotatedVersesViewModel, lifecycle, lifecycleCoroutineScope) {
    override fun TestVerseAnnotation.toBaseItem(bookName: String, bookShortName: String, verseText: String, sortOrder: Int): BaseItem =
            TestVerseItem(verseIndex, bookName, bookShortName, sortOrder)
}

class TestVerseAnnotationViewModel(bibleReadingManager: BibleReadingManager,
                                   verseAnnotationManager: VerseAnnotationManager<TestVerseAnnotation>,
                                   settingsManager: SettingsManager)
    : BaseAnnotatedVersesViewModel<TestVerseAnnotation>(bibleReadingManager, verseAnnotationManager, settingsManager)
