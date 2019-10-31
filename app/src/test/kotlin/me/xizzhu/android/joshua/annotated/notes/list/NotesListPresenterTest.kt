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

package me.xizzhu.android.joshua.annotated.notes.list

import android.content.res.Resources
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.annotated.notes.NotesActivity
import me.xizzhu.android.joshua.core.Note
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.infra.arch.viewData
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.`when`
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class NotesListPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var resources: Resources
    @Mock
    private lateinit var notesActivity: NotesActivity
    @Mock
    private lateinit var navigator: Navigator
    @Mock
    private lateinit var notesListInteractor: NotesListInteractor

    private lateinit var notesListPresenter: NotesListPresenter

    @BeforeTest
    override fun setup() {
        super.setup()

        `when`(resources.getString(anyInt(), anyString(), anyInt())).thenReturn("")
        `when`(resources.getString(anyInt(), anyString(), anyInt(), anyInt())).thenReturn("")
        `when`(resources.getStringArray(anyInt())).thenReturn(Array(12) { "" })
        `when`(notesActivity.resources).thenReturn(resources)

        notesListPresenter = NotesListPresenter(notesActivity, navigator, notesListInteractor, testDispatcher)
    }

    @Test
    fun testToBaseItemsByDate() = testDispatcher.runBlockingTest {
        val bookShortNames = MockContents.kjvBookShortNames
        `when`(notesListInteractor.bookShortNames()).thenReturn(viewData { bookShortNames })
        `when`(notesListInteractor.verse(any())).then { invocation ->
            return@then viewData { MockContents.kjvVerses[(invocation.arguments[0] as VerseIndex).verseIndex] }
        }

        val expected = listOf(
                TitleItem("", false),
                NoteItem(VerseIndex(0, 0, 4), MockContents.kjvBookShortNames[0], MockContents.kjvVerses[4].text.text, "Note1", notesListPresenter::openVerse),
                TitleItem("", false),
                NoteItem(VerseIndex(0, 0, 1), MockContents.kjvBookShortNames[0], MockContents.kjvVerses[1].text.text, "Note2", notesListPresenter::openVerse),
                NoteItem(VerseIndex(0, 0, 3), MockContents.kjvBookShortNames[0], MockContents.kjvVerses[3].text.text, "Note3", notesListPresenter::openVerse),
                TitleItem("", false),
                NoteItem(VerseIndex(0, 0, 2), MockContents.kjvBookShortNames[0], MockContents.kjvVerses[2].text.text, "Note4", notesListPresenter::openVerse)
        )
        val actual = with(notesListPresenter) {
            listOf(
                    Note(VerseIndex(0, 0, 4), "Note1", 2L * 365L * 24L * 3600L * 1000L),
                    Note(VerseIndex(0, 0, 1), "Note2", 36L * 3600L * 1000L),
                    Note(VerseIndex(0, 0, 3), "Note3", 36L * 3600L * 1000L - 1000L),
                    Note(VerseIndex(0, 0, 2), "Note4", 0L)
            ).toBaseItemsByDate()
        }
        assertEquals(expected, actual)
    }

    @Test
    fun testToBaseItemsByBook() = testDispatcher.runBlockingTest {
        val bookNames = MockContents.kjvBookNames
        val bookShortNames = MockContents.kjvBookShortNames
        `when`(notesListInteractor.bookNames()).thenReturn(viewData { bookNames })
        `when`(notesListInteractor.bookShortNames()).thenReturn(viewData { bookShortNames })
        `when`(notesListInteractor.verse(any())).then { invocation ->
            return@then viewData { MockContents.kjvVerses[(invocation.arguments[0] as VerseIndex).verseIndex] }
        }

        val expected = listOf(
                TitleItem(MockContents.kjvBookNames[0], false),
                NoteItem(VerseIndex(0, 0, 2), MockContents.kjvBookShortNames[0], MockContents.kjvVerses[2].text.text, "Note4", notesListPresenter::openVerse),
                NoteItem(VerseIndex(0, 0, 4), MockContents.kjvBookShortNames[0], MockContents.kjvVerses[4].text.text, "Note1", notesListPresenter::openVerse)
        )
        val actual = with(notesListPresenter) {
            listOf(
                    Note(VerseIndex(0, 0, 2), "Note4", 0L),
                    Note(VerseIndex(0, 0, 4), "Note1", 2L * 365L * 24L * 3600L * 1000L)
            ).toBaseItemsByBook()
        }
        assertEquals(expected, actual)
    }
}
