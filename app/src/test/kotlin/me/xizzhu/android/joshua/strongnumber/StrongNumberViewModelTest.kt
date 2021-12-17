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

package me.xizzhu.android.joshua.strongnumber

import android.app.Application
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.StrongNumber
import me.xizzhu.android.joshua.core.StrongNumberManager
import me.xizzhu.android.joshua.infra.BaseViewModel
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.ui.recyclerview.TextItem
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class StrongNumberViewModelTest : BaseUnitTest() {
    private lateinit var bibleReadingManager: BibleReadingManager
    private lateinit var strongNumberManager: StrongNumberManager
    private lateinit var settingsManager: SettingsManager
    private lateinit var application: Application

    private lateinit var strongNumberViewModel: StrongNumberViewModel

    @BeforeTest
    override fun setup() {
        super.setup()

        bibleReadingManager = mockk()
        strongNumberManager = mockk()
        settingsManager = mockk()
        application = mockk()

        strongNumberViewModel = StrongNumberViewModel(bibleReadingManager, strongNumberManager, settingsManager, application)
    }

    @Test
    fun `test strongNumber`() = runTest {
        val sn = "H7225"
        coEvery { bibleReadingManager.currentTranslation() } returns flowOf(MockContents.kjvShortName)
        coEvery { strongNumberManager.readStrongNumber(sn) } returns StrongNumber(sn, MockContents.strongNumberWords.getValue(sn))
        coEvery { strongNumberManager.readVerseIndexes(sn) } returns MockContents.strongNumberReverseIndex.getValue(sn)
        coEvery {
            bibleReadingManager.readVerses(MockContents.kjvShortName, MockContents.strongNumberReverseIndex.getValue(sn))
        } returns mapOf(
                Pair(MockContents.kjvExtraVerses[0].verseIndex, MockContents.kjvExtraVerses[0]),
                Pair(MockContents.kjvExtraVerses[1].verseIndex, MockContents.kjvExtraVerses[1]),
                Pair(MockContents.kjvVerses[0].verseIndex, MockContents.kjvVerses[0])
        )
        coEvery { bibleReadingManager.readBookNames(MockContents.kjvShortName) } returns MockContents.kjvBookNames
        coEvery { bibleReadingManager.readBookShortNames(MockContents.kjvShortName) } returns MockContents.kjvBookShortNames

        strongNumberViewModel.loadStrongNumber(sn)

        val actual = strongNumberViewModel.strongNumber().first()
        assertTrue(actual is BaseViewModel.ViewData.Success)
        assertEquals(6, actual.data.items.size)
        assertEquals(
                "H7225 beginning, chief(-est), first(-fruits, part, time), principal thing.",
                (actual.data.items[0] as TextItem).title.toString()
        )
        assertEquals("Genesis", (actual.data.items[1] as TitleItem).title.toString())
        assertEquals(
                "Gen. 1:1 In the beginning God created the heaven and the earth.",
                (actual.data.items[2] as StrongNumberItem).textForDisplay.toString()
        )
        assertEquals(
                "Gen. 10:10 And the beginning of his kingdom was Babel, and Erech, and Accad, and Calneh, in the land of Shinar.",
                (actual.data.items[3] as StrongNumberItem).textForDisplay.toString()
        )
        assertEquals("Exodus", (actual.data.items[4] as TitleItem).title.toString())
        assertEquals(
                "Ex. 23:19 The first of the firstfruits of thy land thou shalt bring into the house of the LORD thy God. Thou shalt not seethe a kid in his mother’s milk.",
                (actual.data.items[5] as StrongNumberItem).textForDisplay.toString()
        )
    }
}
