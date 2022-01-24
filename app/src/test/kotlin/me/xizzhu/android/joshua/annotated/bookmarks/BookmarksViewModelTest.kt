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
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.emptyFlow
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.Bookmark
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.VerseAnnotationManager
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class BookmarksViewModelTest : BaseUnitTest() {
    private lateinit var bibleReadingManager: BibleReadingManager
    private lateinit var bookmarksManager: VerseAnnotationManager<Bookmark>
    private lateinit var settingsManager: SettingsManager
    private lateinit var application: Application
    private lateinit var bookmarksViewModel: BookmarksViewModel

    @BeforeTest
    override fun setup() {
        super.setup()

        bibleReadingManager = mockk()
        every { bibleReadingManager.currentTranslation() } returns emptyFlow()
        bookmarksManager = mockk()
        every { bookmarksManager.sortOrder() } returns emptyFlow()
        settingsManager = mockk()
        application = mockk()
        bookmarksViewModel = BookmarksViewModel(bibleReadingManager, bookmarksManager, settingsManager, application)
    }

    @Test
    fun `test buildBaseItem`() {
        val actual = bookmarksViewModel.buildBaseItem(
                annotatedVerse = Bookmark(VerseIndex(0, 0, 0), 1L),
                bookName = MockContents.kjvBookNames[0],
                bookShortName = MockContents.kjvBookShortNames[0],
                verseText = MockContents.kjvVerses[0].text.text,
                sortOrder = Constants.SORT_BY_BOOK
        )
        assertTrue(actual is BookmarkItem)
        assertEquals(VerseIndex(0, 0, 0), actual.verseIndex)
        assertEquals("Gen. 1:1 In the beginning God created the heaven and the earth.", actual.textForDisplay.toString())
    }
}
