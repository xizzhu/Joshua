/*
 * Copyright (C) 2022 Xizhi Zhu
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

package me.xizzhu.android.joshua.annotated.bookmarks

import android.app.Application
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.annotated.AnnotatedVersesActivity
import me.xizzhu.android.joshua.annotated.AnnotatedVersesViewModel
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.Bookmark
import me.xizzhu.android.joshua.core.provider.CoroutineDispatcherProvider
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.VerseAnnotationManager
import me.xizzhu.android.joshua.core.provider.TimeProvider
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    bibleReadingManager: BibleReadingManager,
    bookmarksManager: VerseAnnotationManager<Bookmark>,
    settingsManager: SettingsManager,
    coroutineDispatcherProvider: CoroutineDispatcherProvider,
    timeProvider: TimeProvider,
    application: Application
) : AnnotatedVersesViewModel<Bookmark>(
    bibleReadingManager = bibleReadingManager,
    verseAnnotationManager = bookmarksManager,
    noItemText = R.string.text_no_bookmarks,
    settingsManager = settingsManager,
    coroutineDispatcherProvider = coroutineDispatcherProvider,
    timeProvider = timeProvider,
    application = application
) {
    override fun buildBaseItem(annotatedVerse: Bookmark, bookName: String, bookShortName: String, verseText: String, sortOrder: Int): BaseItem =
        BookmarkItem(annotatedVerse.verseIndex, bookName, bookShortName, verseText, sortOrder)
}

@AndroidEntryPoint
class BookmarksActivity : AnnotatedVersesActivity<Bookmark, BookmarksViewModel>(R.string.title_bookmarks) {
    override val viewModel: BookmarksViewModel by viewModels()
}
