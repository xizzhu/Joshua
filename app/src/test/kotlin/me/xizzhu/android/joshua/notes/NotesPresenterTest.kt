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

package me.xizzhu.android.joshua.notes

import android.content.res.Resources
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.ui.recyclerview.TextItem
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*

class NotesPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var notesInteractor: NotesInteractor
    @Mock
    private lateinit var notesView: NotesView
    @Mock
    private lateinit var resources: Resources

    private lateinit var settingsChannel: BroadcastChannel<Settings>
    private lateinit var notesPresenter: NotesPresenter

    @Before
    override fun setup() {
        super.setup()

        runBlocking {
            settingsChannel = ConflatedBroadcastChannel(Settings.DEFAULT)
            `when`(notesInteractor.observeSettings()).thenReturn(settingsChannel.openSubscription())
            `when`(notesInteractor.readNotes()).thenReturn(emptyList())
            `when`(notesInteractor.readCurrentTranslation()).thenReturn(MockContents.kjvShortName)
            `when`(resources.getString(anyInt())).thenReturn("")

            notesPresenter = NotesPresenter(notesInteractor, resources)
            notesPresenter.attachView(notesView)
        }
    }

    @After
    override fun tearDown() {
        notesPresenter.detachView()
        super.tearDown()
    }

    @Test
    fun testLoadEmptyNotes() {
        runBlocking {
            // loadNotes() is called by onViewAttached()
            verify(notesView, times(1)).onNotesLoaded(listOf(TextItem("")))
            verify(notesView, never()).onNotesLoadFailed()
        }
    }

    @Test
    fun testLoadBookmarksWithException() {
        runBlocking {
            `when`(notesInteractor.readNotes()).thenThrow(RuntimeException("Random exception"))
            notesPresenter.loadNotes()

            // loadBookmarks() is called by onViewAttached(), so onNoNotesAvailable() is called once
            verify(notesView, times(1)).onNotesLoaded(listOf(TextItem("")))
            verify(notesView, times(1)).onNotesLoadFailed()
        }
    }
}
