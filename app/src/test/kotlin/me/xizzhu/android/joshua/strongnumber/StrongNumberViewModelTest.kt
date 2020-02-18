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

package me.xizzhu.android.joshua.strongnumber

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class StrongNumberViewModelTest : BaseUnitTest() {
    @Mock
    private lateinit var bibleReadingManager: BibleReadingManager
    @Mock
    private lateinit var strongNumberManager: StrongNumberManager
    @Mock
    private lateinit var settingsManager: SettingsManager

    private lateinit var strongNumberListViewModel: StrongNumberListViewModel

    @BeforeTest
    override fun setup() {
        super.setup()

        strongNumberListViewModel = StrongNumberListViewModel(bibleReadingManager, strongNumberManager, settingsManager)
    }

    @Test
    fun testStrongNumber() = testDispatcher.runBlockingTest {
        val currentTranslation = MockContents.kjvShortName
        val sn = "H7225"
        val strongNumber = StrongNumber(sn, MockContents.strongNumberWords.getValue(sn))
        `when`(bibleReadingManager.currentTranslation()).thenReturn(flowOf("", currentTranslation))
        `when`(bibleReadingManager.readBookNames(currentTranslation)).thenReturn(MockContents.kjvBookNames)
        `when`(bibleReadingManager.readBookShortNames(currentTranslation)).thenReturn(MockContents.kjvBookShortNames)
        `when`(bibleReadingManager.readVerses(currentTranslation, MockContents.strongNumberReverseIndex.getValue(sn)))
                .thenReturn(mapOf(VerseIndex(0, 0, 0) to MockContents.kjvVerses[0]))
        `when`(strongNumberManager.readStrongNumber(sn)).thenReturn(strongNumber)
        `when`(strongNumberManager.readVerseIndexes(sn)).thenReturn(MockContents.strongNumberReverseIndex.getValue(sn))

        assertEquals(
                listOf(
                        ViewData.loading(),
                        ViewData.success(
                                StrongNumberViewData(
                                        strongNumber, listOf(MockContents.kjvVerses[0]),
                                        MockContents.kjvBookNames, MockContents.kjvBookShortNames
                                )
                        )
                ),
                strongNumberListViewModel.strongNumber(sn).toList()
        )
    }
}
