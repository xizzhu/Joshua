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

package me.xizzhu.android.joshua.reading

import android.content.Context
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReadingInteractorTest : BaseUnitTest() {
    @Mock
    private lateinit var readingActivity: ReadingActivity
    @Mock
    private lateinit var navigator: Navigator
    @Mock
    private lateinit var bibleReadingManager: BibleReadingManager
    @Mock
    private lateinit var bookmarkManager: BookmarkManager
    @Mock
    private lateinit var highlightManager: HighlightManager
    @Mock
    private lateinit var noteManager: NoteManager
    @Mock
    private lateinit var readingProgressManager: ReadingProgressManager
    @Mock
    private lateinit var translationManager: TranslationManager
    @Mock
    private lateinit var settingsManager: SettingsManager

    private lateinit var readingInteractor: ReadingInteractor

    @Before
    override fun setup() {
        super.setup()
        readingInteractor = ReadingInteractor(readingActivity, navigator, bibleReadingManager,
                bookmarkManager, highlightManager, noteManager, readingProgressManager,
                translationManager, settingsManager)
    }

    @Test
    fun testCloseVerseDetailWithDefaultState() {
        runBlocking { assertFalse(readingInteractor.closeVerseDetail()) }
    }

    @Test
    fun testOpenThenCloseVerseDetail() {
        runBlocking {
            val verseIndex = VerseIndex(1, 2, 3)
            readingInteractor.openVerseDetail(verseIndex, 0)

            assertEquals(verseIndex, readingInteractor.observeVerseDetailOpenState().first().first)
            assertTrue(readingInteractor.closeVerseDetail())
        }
    }

    @Test
    fun testCopyToClipBoardWithEmptyVerses() {
        runBlocking {
            assertFalse(readingInteractor.copyToClipBoard(emptyList()))
        }
    }

    @Test
    fun testCopyToClipBoardWithException() {
        runBlocking {
            `when`(readingActivity.getSystemService(Context.CLIPBOARD_SERVICE)).thenThrow(RuntimeException("Random exception"))

            assertFalse(readingInteractor.copyToClipBoard(MockContents.kjvVerses))
        }
    }

    @Test
    fun testShareWithEmptyVerses() {
        runBlocking {
            assertFalse(readingInteractor.share(emptyList()))
        }
    }

    @Test
    fun testShareWithException() {
        runBlocking {
            assertFalse(readingInteractor.share(MockContents.kjvVerses))
        }
    }

    @Test
    fun testEmptyVerseToStringForSharing() {
        assertTrue(readingInteractor.toStringForSharing(emptyList(), "").isEmpty())
    }

    @Test
    fun testSingleVerseToStringForSharing() {
        assertEquals(
                "Genesis 1:1 In the beginning God created the heaven and the earth.",
                readingInteractor.toStringForSharing(listOf(MockContents.kjvVerses[0]), MockContents.kjvBookNames[0])
        )
    }

    @Test
    fun testSingleVerseWithMultipleTranslationsToStringForSharing() {
        assertEquals(
                "Genesis 1:1\nKJV: In the beginning God created the heaven and the earth.\n中文和合本: 起初神创造天地。",
                readingInteractor.toStringForSharing(listOf(MockContents.kjvVersesWithCuvParallel[0]), MockContents.kjvBookNames[0])
        )
    }

    @Test
    fun testMultipleVersesToStringForSharing() {
        assertEquals(
                "Genesis 1:1 In the beginning God created the heaven and the earth.\nGenesis 1:2 And the earth was without form, and void; and darkness was upon the face of the deep. And the Spirit of God moved upon the face of the waters.",
                readingInteractor.toStringForSharing(listOf(MockContents.kjvVerses[0], MockContents.kjvVerses[1]), MockContents.kjvBookNames[0])
        )
    }

    @Test
    fun testMultipleVerseWithMultipleTranslationsToStringForSharing() {
        assertEquals(
                "Genesis 1:1\nKJV: In the beginning God created the heaven and the earth.\n中文和合本: 起初神创造天地。\nGenesis 1:2\nKJV: And the earth was without form, and void; and darkness was upon the face of the deep. And the Spirit of God moved upon the face of the waters.\n中文和合本: 地是空虚混沌。渊面黑暗。神的灵运行在水面上。",
                readingInteractor.toStringForSharing(listOf(MockContents.kjvVersesWithCuvParallel[0], MockContents.kjvVersesWithCuvParallel[1]), MockContents.kjvBookNames[0])
        )
    }

    @Test
    fun testMultipleVersesRandomOrderToStringForSharing() {
        assertEquals(
                "Genesis 1:1 In the beginning God created the heaven and the earth.\nGenesis 1:2 And the earth was without form, and void; and darkness was upon the face of the deep. And the Spirit of God moved upon the face of the waters.\nGenesis 1:10 And God called the dry land Earth; and the gathering together of the waters called he Seas: and God saw that it was good.",
                readingInteractor.toStringForSharing(listOf(MockContents.kjvVerses[0], MockContents.kjvVerses[9], MockContents.kjvVerses[1]), MockContents.kjvBookNames[0])
        )
    }
}
