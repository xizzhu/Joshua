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

package me.xizzhu.android.joshua.annotated.bookmarks

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import me.xizzhu.android.joshua.annotated.bookmarks.list.BookmarkItem
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.Bookmark
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.repository.BookmarksRepository
import me.xizzhu.android.joshua.tests.EspressoTestRule
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.tests.robots.BookmarksActivityRobot
import org.junit.Rule
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
@LargeTest
class BookmarksActivityTest {
    @get:Rule
    val activityRule = EspressoTestRule(BookmarksActivity::class.java)

    @Test
    fun testEmptyBookmarks() {
        BibleReadingManager.currentTranslation.value = MockContents.kjvShortName
        val robot = BookmarksActivityRobot(activityRule.activity)
                .isNoBookmarksDisplayed()

        BookmarksRepository._sortOrder.value = Constants.SORT_BY_BOOK
        robot.isNoBookmarksDisplayed()
    }

    @Test
    fun testBookmarks() {
        val now = System.currentTimeMillis()
        val bookmarks = listOf(
                Bookmark(VerseIndex(0, 0, 4), now),
                Bookmark(VerseIndex(0, 0, 0), now)
        )
        BibleReadingManager.currentTranslation.value = MockContents.kjvShortName
        BookmarksRepository.annotations = bookmarks
        BookmarksActivityRobot(activityRule.activity)
                .areBookmarksDisplayed(bookmarks.map { bookmark ->
                    BookmarkItem(
                            bookmark.verseIndex,
                            MockContents.kjvBookNames[bookmark.verseIndex.bookIndex],
                            MockContents.kjvBookShortNames[bookmark.verseIndex.bookIndex],
                            MockContents.kjvVerses[bookmark.verseIndex.verseIndex].text.text,
                            Constants.DEFAULT_SORT_ORDER,
                            {}
                    )
                })
                .clickBookmark(0)

        assertEquals(VerseIndex(0, 0, 4), BibleReadingManager.currentVerseIndex.value)
    }
}
