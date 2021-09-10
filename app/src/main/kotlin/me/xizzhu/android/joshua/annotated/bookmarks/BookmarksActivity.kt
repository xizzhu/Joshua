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

package me.xizzhu.android.joshua.annotated.bookmarks

import android.app.Application
import dagger.hilt.android.AndroidEntryPoint
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.annotated.AnnotatedVersesActivity
import me.xizzhu.android.joshua.annotated.AnnotatedVersesViewModel
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.Bookmark
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.VerseAnnotationManager
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem

class BookmarksViewModel(
        bibleReadingManager: BibleReadingManager,
        bookmarksManager: VerseAnnotationManager<Bookmark>,
        settingsManager: SettingsManager,
        application: Application
) : AnnotatedVersesViewModel<Bookmark>(bibleReadingManager, bookmarksManager, R.string.text_no_bookmarks, settingsManager, application) {
    override fun buildBaseItem(annotatedVerse: Bookmark, bookName: String, bookShortName: String, verseText: String, sortOrder: Int): BaseItem =
            BookmarkItem(annotatedVerse.verseIndex, bookName, bookShortName, verseText, sortOrder)
}

@AndroidEntryPoint
class BookmarksActivity : AnnotatedVersesActivity<Bookmark>(R.string.title_bookmarks)
