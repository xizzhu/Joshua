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

package me.xizzhu.android.joshua.bookmarks

import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*

class BookmarksPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var bookmarksInteractor: BookmarksInteractor
    @Mock
    private lateinit var bookmarksView: BookmarksView

    private lateinit var settingsChannel: BroadcastChannel<Settings>
    private lateinit var bookmarksPresenter: BookmarksPresenter

    @Before
    override fun setup() {
        super.setup()

        runBlocking {
            settingsChannel = ConflatedBroadcastChannel(Settings.DEFAULT)
            `when`(bookmarksInteractor.observeSettings()).thenReturn(settingsChannel.openSubscription())
            `when`(bookmarksInteractor.readBookmarks()).thenReturn(emptyList())
            `when`(bookmarksInteractor.readCurrentTranslation()).thenReturn(MockContents.kjvShortName)

            bookmarksPresenter = BookmarksPresenter(bookmarksInteractor)
            bookmarksPresenter.attachView(bookmarksView)
        }
    }

    @After
    override fun tearDown() {
        bookmarksPresenter.detachView()
        super.tearDown()
    }

    @Test
    fun testLoadBookmarks() {
        runBlocking {
            // loadBookmarks() is called by onViewAttached(), so no need to call from here
            verify(bookmarksView, times(1)).onBookmarksLoaded(emptyList())
            verify(bookmarksView, never()).onBookmarksLoadFailed()
        }
    }

    @Test
    fun testLoadBookmarksWithException() {
        runBlocking {
            `when`(bookmarksInteractor.readBookmarks()).thenThrow(RuntimeException("Random exception"))
            bookmarksPresenter.loadBookmarks()

            // loadBookmarks() is called by onViewAttached(), so onBookmarksLoaded() is called once
            verify(bookmarksView, times(1)).onBookmarksLoaded(emptyList())
            verify(bookmarksView, times(1)).onBookmarksLoadFailed()
        }
    }
}
