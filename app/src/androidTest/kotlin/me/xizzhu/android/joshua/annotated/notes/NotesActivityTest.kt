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

package me.xizzhu.android.joshua.annotated.notes

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import me.xizzhu.android.joshua.annotated.notes.list.NoteItem
import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.core.repository.NotesRepository
import me.xizzhu.android.joshua.tests.EspressoTestRule
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.tests.robots.NotesActivityRobot
import org.junit.Rule
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
@LargeTest
class NotesActivityTest {
    @get:Rule
    val activityRule = EspressoTestRule(NotesActivity::class.java)

    @Test
    fun testEmptyNotes() {
        BibleReadingManager.currentTranslation.value = MockContents.kjvShortName
        val robot = NotesActivityRobot(activityRule.activity)
                .isNoNotesDisplayed()

        NotesRepository._sortOrder.value = Constants.SORT_BY_BOOK
        robot.isNoNotesDisplayed()
    }

    @Test
    fun testNotes() {
        val now = System.currentTimeMillis()
        val notes = listOf(
                Note(VerseIndex(0, 0, 4), "some random note", now),
                Note(VerseIndex(0, 0, 0), "more sample", now)
        )
        BibleReadingManager.currentTranslation.value = MockContents.kjvShortName
        NotesRepository.annotations = notes
        NotesActivityRobot(activityRule.activity)
                .areNotesDisplayed(notes.map { note ->
                    NoteItem(
                            note.verseIndex,
                            MockContents.kjvBookShortNames[note.verseIndex.bookIndex],
                            MockContents.kjvVerses[note.verseIndex.verseIndex].text.text,
                            note.note,
                            {}
                    )
                })
                .clickNote(0)

        assertEquals(VerseIndex(0, 0, 4), BibleReadingManager.currentVerseIndex.value)
    }
}
