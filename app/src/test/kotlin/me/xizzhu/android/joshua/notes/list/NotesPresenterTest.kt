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

package me.xizzhu.android.joshua.notes.list

import android.content.res.Resources
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.Note
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.notes.NotesInteractor
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.ui.recyclerview.TextItem
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
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
    private lateinit var notesSortOrder: BroadcastChannel<Int>
    private lateinit var notesPresenter: NotesPresenter

    @Before
    override fun setup() {
        super.setup()

        runBlocking {
            settingsChannel = ConflatedBroadcastChannel(Settings.DEFAULT)
            notesSortOrder = ConflatedBroadcastChannel(Constants.SORT_BY_DATE)
            `when`(notesInteractor.observeSettings()).thenReturn(settingsChannel.openSubscription())
            `when`(notesInteractor.observeNotesSortOrder()).thenReturn(notesSortOrder.openSubscription())
            `when`(notesInteractor.readCurrentTranslation()).thenReturn(MockContents.kjvShortName)
            `when`(resources.getString(anyInt())).thenReturn("")
            `when`(resources.getString(anyInt(), anyString(), anyInt(), anyInt())).thenReturn("")
            `when`(resources.getStringArray(anyInt())).thenReturn(Array(12) { "" })

            notesPresenter = NotesPresenter(notesInteractor, resources)
        }
    }

    @Test
    fun testLoadEmptyNotes() {
        runBlocking {
            `when`(notesInteractor.readNotes(Constants.SORT_BY_DATE)).thenReturn(emptyList())

            // loadNotes() is called by onViewAttached()
            notesPresenter.attachView(notesView)
            verify(notesView, times(1)).onNotesLoaded(listOf(TextItem("")))
            verify(notesView, never()).onNotesLoadFailed(Constants.SORT_BY_DATE)

            notesPresenter.detachView()
        }
    }

    @Test
    fun testLoadNotesSortByDate() {
        runBlocking {
            `when`(notesInteractor.readNotes(Constants.SORT_BY_DATE)).thenReturn(listOf(
                    Note(VerseIndex(0, 0, 4), "Note1", 2L * 365L * 24L * 3600L * 1000L),
                    Note(VerseIndex(0, 0, 1), "Note2", 36L * 3600L * 1000L),
                    Note(VerseIndex(0, 0, 3), "Note3", 36L * 3600L * 1000L - 1000L),
                    Note(VerseIndex(0, 0, 2), "Note4", 0L)
            ))
            `when`(notesInteractor.readCurrentTranslation()).thenReturn(MockContents.kjvShortName)
            `when`(notesInteractor.readVerse(MockContents.kjvShortName, VerseIndex(0, 0, 1)))
                    .thenReturn(MockContents.kjvVerses[1])
            `when`(notesInteractor.readVerse(MockContents.kjvShortName, VerseIndex(0, 0, 2)))
                    .thenReturn(MockContents.kjvVerses[2])
            `when`(notesInteractor.readVerse(MockContents.kjvShortName, VerseIndex(0, 0, 3)))
                    .thenReturn(MockContents.kjvVerses[3])
            `when`(notesInteractor.readVerse(MockContents.kjvShortName, VerseIndex(0, 0, 4)))
                    .thenReturn(MockContents.kjvVerses[4])
            `when`(notesInteractor.readBookShortName(MockContents.kjvShortName, 0)).thenReturn(MockContents.kjvBookShortNames[0])

            // loadBookmarks() is called by onViewAttached(), so no need to call again
            notesPresenter.attachView(notesView)

            verify(notesView, times(1)).onNotesLoaded(listOf(
                    TitleItem("", false),
                    NoteItem(VerseIndex(0, 0, 4), MockContents.kjvBookShortNames[0], MockContents.kjvVerses[4].text.text, "Note1", notesPresenter::selectVerse),
                    TitleItem("", false),
                    NoteItem(VerseIndex(0, 0, 1), MockContents.kjvBookShortNames[0], MockContents.kjvVerses[1].text.text, "Note2", notesPresenter::selectVerse),
                    NoteItem(VerseIndex(0, 0, 3), MockContents.kjvBookShortNames[0], MockContents.kjvVerses[3].text.text, "Note3", notesPresenter::selectVerse),
                    TitleItem("", false),
                    NoteItem(VerseIndex(0, 0, 2), MockContents.kjvBookShortNames[0], MockContents.kjvVerses[2].text.text, "Note4", notesPresenter::selectVerse)
            ))
            verify(notesView, never()).onNotesLoadFailed(anyInt())

            notesPresenter.detachView()
        }
    }

    @Test
    fun testLoadNotesSortByBook() {
        runBlocking {
            notesSortOrder.send(Constants.SORT_BY_BOOK)
            `when`(notesInteractor.readNotes(Constants.SORT_BY_BOOK)).thenReturn(listOf(
                    Note(VerseIndex(0, 0, 3), "Note", 0L)
            ))
            `when`(notesInteractor.readCurrentTranslation()).thenReturn(MockContents.kjvShortName)
            `when`(notesInteractor.readVerse(MockContents.kjvShortName, VerseIndex(0, 0, 3)))
                    .thenReturn(MockContents.kjvVerses[3])
            `when`(notesInteractor.readBookShortName(MockContents.kjvShortName, 0)).thenReturn(MockContents.kjvBookShortNames[0])

            // loadBookmarks() is called by onViewAttached(), so no need to call again
            notesPresenter.attachView(notesView)

            verify(notesView, times(1)).onNotesLoaded(listOf(
                    TitleItem(MockContents.kjvVerses[3].text.bookName, false),
                    NoteItem(VerseIndex(0, 0, 3), MockContents.kjvBookShortNames[0], MockContents.kjvVerses[3].text.text, "Note", notesPresenter::selectVerse)
            ))
            verify(notesView, never()).onNotesLoadFailed(anyInt())

            notesPresenter.detachView()
        }
    }

    @Test
    fun testLoadBookmarksWithException() {
        runBlocking {
            `when`(notesInteractor.readNotes(Constants.SORT_BY_DATE)).thenThrow(RuntimeException("Random exception"))

            // loadNotes() is called by onViewAttached()
            notesPresenter.attachView(notesView)
            verify(notesView, never()).onNotesLoaded(any())
            verify(notesView, times(1)).onNotesLoadFailed(Constants.SORT_BY_DATE)

            notesPresenter.detachView()
        }
    }
}
