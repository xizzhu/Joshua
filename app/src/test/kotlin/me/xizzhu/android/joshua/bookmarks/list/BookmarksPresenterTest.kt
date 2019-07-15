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

package me.xizzhu.android.joshua.bookmarks.list

import android.content.res.Resources
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.bookmarks.BookmarksInteractor
import me.xizzhu.android.joshua.core.Bookmark
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.ui.recyclerview.TextItem
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*

class BookmarksPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var bookmarksInteractor: BookmarksInteractor
    @Mock
    private lateinit var bookmarksView: BookmarksView
    @Mock
    private lateinit var resources: Resources

    private lateinit var settingsChannel: BroadcastChannel<Settings>
    private lateinit var bookmarksSortOrder: BroadcastChannel<Int>
    private lateinit var bookmarksPresenter: BookmarksPresenter

    @Before
    override fun setup() {
        super.setup()

        runBlocking {
            settingsChannel = ConflatedBroadcastChannel(Settings.DEFAULT)
            bookmarksSortOrder = ConflatedBroadcastChannel(Constants.SORT_BY_DATE)
            `when`(bookmarksInteractor.observeSettings()).thenReturn(settingsChannel.openSubscription())
            `when`(bookmarksInteractor.observeBookmarksSortOrder()).thenReturn(bookmarksSortOrder.openSubscription())
            `when`(bookmarksInteractor.readCurrentTranslation()).thenReturn(MockContents.kjvShortName)
            `when`(resources.getString(anyInt())).thenReturn("")
            `when`(resources.getString(anyInt(), anyString(), anyInt(), anyInt())).thenReturn("")
            `when`(resources.getStringArray(anyInt())).thenReturn(Array(12) { "" })

            bookmarksPresenter = spy(BookmarksPresenter(bookmarksInteractor, resources))
        }
    }

    @Test
    fun testLoadEmptyBookmarks() {
        runBlocking {
            `when`(bookmarksInteractor.readBookmarks(Constants.SORT_BY_DATE)).thenReturn(emptyList())

            // loadBookmarks() is called by onViewAttached(), so no need to call again
            bookmarksPresenter.attachView(bookmarksView)
            verify(bookmarksView, times(1)).onBookmarksLoaded(listOf(TextItem("")))
            verify(bookmarksView, never()).onBookmarksLoadFailed(anyInt())

            bookmarksPresenter.detachView()
        }
    }

    @Test
    fun testLoadBookmarksSortByDate() {
        runBlocking {
            `when`(bookmarksInteractor.readBookmarks(Constants.SORT_BY_DATE)).thenReturn(listOf(
                    Bookmark(VerseIndex(0, 0, 4), 2L * 365L * 24L * 3600L * 1000L),
                    Bookmark(VerseIndex(0, 0, 1), 36L * 3600L * 1000L),
                    Bookmark(VerseIndex(0, 0, 3), 36L * 3600L * 1000L - 1000L),
                    Bookmark(VerseIndex(0, 0, 2), 0L)
            ))
            `when`(bookmarksInteractor.readCurrentTranslation()).thenReturn(MockContents.kjvShortName)
            `when`(bookmarksInteractor.readVerse(MockContents.kjvShortName, VerseIndex(0, 0, 1)))
                    .thenReturn(MockContents.kjvVerses[1])
            `when`(bookmarksInteractor.readVerse(MockContents.kjvShortName, VerseIndex(0, 0, 2)))
                    .thenReturn(MockContents.kjvVerses[2])
            `when`(bookmarksInteractor.readVerse(MockContents.kjvShortName, VerseIndex(0, 0, 3)))
                    .thenReturn(MockContents.kjvVerses[3])
            `when`(bookmarksInteractor.readVerse(MockContents.kjvShortName, VerseIndex(0, 0, 4)))
                    .thenReturn(MockContents.kjvVerses[4])
            `when`(bookmarksInteractor.readBookNames(MockContents.kjvShortName)).thenReturn(MockContents.kjvBookNames)
            `when`(bookmarksInteractor.readBookShortNames(MockContents.kjvShortName)).thenReturn(MockContents.kjvBookShortNames)

            // loadBookmarks() is called by onViewAttached(), so no need to call again
            bookmarksPresenter.attachView(bookmarksView)

            with(inOrder(bookmarksInteractor, bookmarksView)) {
                verify(bookmarksInteractor, times(1)).notifyLoadingStarted()
                verify(bookmarksView, times(1)).onBookmarksLoadingStarted()
                verify(bookmarksView, times(1)).onBookmarksLoaded(listOf(
                        TitleItem("", false),
                        BookmarkItem(VerseIndex(0, 0, 4), MockContents.kjvBookNames[0], MockContents.kjvBookShortNames[0], MockContents.kjvVerses[4].text.text, Constants.SORT_BY_DATE, bookmarksPresenter::selectVerse),
                        TitleItem("", false),
                        BookmarkItem(VerseIndex(0, 0, 1), MockContents.kjvBookNames[0], MockContents.kjvBookShortNames[0], MockContents.kjvVerses[1].text.text, Constants.SORT_BY_DATE, bookmarksPresenter::selectVerse),
                        BookmarkItem(VerseIndex(0, 0, 3), MockContents.kjvBookNames[0], MockContents.kjvBookShortNames[0], MockContents.kjvVerses[3].text.text, Constants.SORT_BY_DATE, bookmarksPresenter::selectVerse),
                        TitleItem("", false),
                        BookmarkItem(VerseIndex(0, 0, 2), MockContents.kjvBookNames[0], MockContents.kjvBookShortNames[0], MockContents.kjvVerses[2].text.text, Constants.SORT_BY_DATE, bookmarksPresenter::selectVerse)
                ))
                verify(bookmarksView, times(1)).onBookmarksLoadingCompleted()
                verify(bookmarksInteractor, times(1)).notifyLoadingFinished()
            }
            verify(bookmarksView, never()).onBookmarksLoadFailed(anyInt())

            bookmarksPresenter.detachView()
        }
    }

    @Test
    fun testLoadBookmarksSortByBook() {
        runBlocking {
            bookmarksSortOrder.send(Constants.SORT_BY_BOOK)
            `when`(bookmarksInteractor.readBookmarks(Constants.SORT_BY_BOOK)).thenReturn(listOf(
                    Bookmark(VerseIndex(0, 0, 3), 0L)
            ))
            `when`(bookmarksInteractor.readCurrentTranslation()).thenReturn(MockContents.kjvShortName)
            `when`(bookmarksInteractor.readVerse(MockContents.kjvShortName, VerseIndex(0, 0, 3)))
                    .thenReturn(MockContents.kjvVerses[3])
            `when`(bookmarksInteractor.readBookNames(MockContents.kjvShortName)).thenReturn(MockContents.kjvBookNames)
            `when`(bookmarksInteractor.readBookShortNames(MockContents.kjvShortName)).thenReturn(MockContents.kjvBookShortNames)

            // loadBookmarks() is called by onViewAttached(), so no need to call again
            bookmarksPresenter.attachView(bookmarksView)

            with(inOrder(bookmarksInteractor, bookmarksView)) {
                verify(bookmarksInteractor, times(1)).notifyLoadingStarted()
                verify(bookmarksView, times(1)).onBookmarksLoadingStarted()
                verify(bookmarksView, times(1)).onBookmarksLoaded(listOf(
                        TitleItem(MockContents.kjvBookNames[0], false),
                        BookmarkItem(VerseIndex(0, 0, 3), MockContents.kjvBookNames[0], MockContents.kjvBookShortNames[0], MockContents.kjvVerses[3].text.text, Constants.SORT_BY_BOOK, bookmarksPresenter::selectVerse)
                ))
                verify(bookmarksView, times(1)).onBookmarksLoadingCompleted()
                verify(bookmarksInteractor, times(1)).notifyLoadingFinished()
            }
            verify(bookmarksView, never()).onBookmarksLoadFailed(anyInt())

            bookmarksPresenter.detachView()
        }
    }

    @Test
    fun testLoadBookmarksWithException() {
        runBlocking {
            `when`(bookmarksInteractor.readBookmarks(Constants.SORT_BY_DATE)).thenThrow(RuntimeException("Random exception"))

            // loadBookmarks() is called by onViewAttached(), so no need to call again
            bookmarksPresenter.attachView(bookmarksView)

            with(inOrder(bookmarksInteractor, bookmarksView)) {
                verify(bookmarksInteractor, times(1)).notifyLoadingStarted()
                verify(bookmarksView, times(1)).onBookmarksLoadingStarted()
                verify(bookmarksView, times(1)).onBookmarksLoadFailed(Constants.SORT_BY_DATE)
                verify(bookmarksInteractor, times(1)).notifyLoadingFinished()
            }
            verify(bookmarksView, never()).onBookmarksLoaded(any())
            verify(bookmarksView, never()).onBookmarksLoadingCompleted()

            bookmarksPresenter.detachView()
        }
    }
}
