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

package me.xizzhu.android.joshua.preview

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.infra.BaseViewModel
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class PreviewViewDataTest : BaseUnitTest() {
    @Test
    fun `test loadPreview with invalid verseIndex`() = runTest {
        val actual = loadPreview(mockk(), mockk(), VerseIndex.INVALID, mockk()).toList()
        assertEquals(2, actual.size)
        assertTrue(actual[0] is BaseViewModel.ViewData.Loading)
        assertTrue(actual[1] is BaseViewModel.ViewData.Failure)
    }

    @Test
    fun `test loadPreview`() = runTest {
        val bibleReadingManager: BibleReadingManager = mockk()
        every { bibleReadingManager.currentTranslation() } returns flowOf("", MockContents.kjvShortName)
        coEvery { bibleReadingManager.readVerses("KJV", 0, 0) } returns MockContents.kjvVerses
        coEvery { bibleReadingManager.readBookShortNames("KJV") } returns MockContents.kjvBookShortNames
        val settingsManager: SettingsManager = mockk()
        every { settingsManager.settings() } returns flowOf(Settings.DEFAULT)
        val converter: List<Verse>.() -> List<BaseItem> = mockk()
        every { converter(any()) } returns listOf()

        val actual = loadPreview(bibleReadingManager, settingsManager, VerseIndex(0, 0, 0), converter).toList()
        assertEquals(2, actual.size)
        assertTrue(actual[0] is BaseViewModel.ViewData.Loading)
        assertEquals(Settings.DEFAULT, (actual[1] as BaseViewModel.ViewData.Success).data.settings)
        assertEquals("Gen., 1", (actual[1] as BaseViewModel.ViewData.Success).data.title)
        assertTrue((actual[1] as BaseViewModel.ViewData.Success).data.items.isEmpty())
        assertEquals(0, (actual[1] as BaseViewModel.ViewData.Success).data.currentPosition)
    }

    @Test
    fun `test toVersePreviewItems() with single verse`() {
        val actual = toVersePreviewItems(listOf(MockContents.kjvVerses[0]))
        assertEquals(1, actual.size)
        assertEquals("1:1 In the beginning God created the heaven and the earth.", actual[0].textForDisplay.toString())
    }

    @Test
    fun `test toVersePreviewItems() with multiple verses`() {
        val actual = toVersePreviewItems(listOf(MockContents.kjvVerses[0], MockContents.kjvVerses[1]))
        assertEquals(2, actual.size)
        assertEquals("1:1 In the beginning God created the heaven and the earth.", actual[0].textForDisplay.toString())
        assertEquals(
                "1:2 And the earth was without form, and void; and darkness was upon the face of the deep. And the Spirit of God moved upon the face of the waters.",
                actual[1].textForDisplay.toString()
        )
    }

    @Test
    fun `test toVersePreviewItems() with multiple verses but not consecutive`() {
        val actual = toVersePreviewItems(listOf(MockContents.msgVerses[0], MockContents.msgVerses[1], MockContents.msgVerses[2]))
        assertEquals(2, actual.size)
        assertEquals(
                "1:1-2 First this: God created the Heavens and Earth—all you see, all you don't see. Earth was a soup of nothingness, a bottomless emptiness, an inky blackness. God's Spirit brooded like a bird above the watery abyss.",
                actual[0].textForDisplay.toString()
        )
        assertEquals(
                "1:3 God spoke: \"Light!\"\nAnd light appeared.\nGod saw that light was good\nand separated light from dark.\nGod named the light Day,\nhe named the dark Night.\nIt was evening, it was morning—\nDay One.",
                actual[1].textForDisplay.toString()
        )
    }
}
