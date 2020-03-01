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

package me.xizzhu.android.joshua.translations

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.TranslationManager
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TranslationsViewModelTest : BaseUnitTest() {
    @Mock
    private lateinit var bibleReadingManager: BibleReadingManager
    @Mock
    private lateinit var translationManager: TranslationManager
    @Mock
    private lateinit var settingsManager: SettingsManager

    private lateinit var translationsViewModel: TranslationsViewModel

    @BeforeTest
    override fun setup() {
        super.setup()

        `when`(bibleReadingManager.currentTranslation()).thenReturn(emptyFlow())
        `when`(translationManager.availableTranslations()).thenReturn(emptyFlow())
        `when`(translationManager.downloadedTranslations()).thenReturn(emptyFlow())
        translationsViewModel = TranslationsViewModel(bibleReadingManager, translationManager, settingsManager)
    }

    @Test
    fun testTranslationListWithNoCurrentTranslation() = testDispatcher.runBlockingTest {
        `when`(bibleReadingManager.currentTranslation()).thenReturn(flowOf(""))
        `when`(translationManager.availableTranslations()).thenReturn(flowOf(listOf(MockContents.cuvTranslationInfo)))
        `when`(translationManager.downloadedTranslations()).thenReturn(flowOf(emptyList()))
        translationsViewModel = TranslationsViewModel(bibleReadingManager, translationManager, settingsManager)

        assertEquals(
                listOf(
                        TranslationList(
                                "",
                                listOf(MockContents.cuvTranslationInfo),
                                emptyList()
                        )
                ),
                translationsViewModel.translationList(false).toList()
        )
        verify(translationManager, times(1)).reload(false)
    }

    @Test
    fun testTranslationList() = testDispatcher.runBlockingTest {
        `when`(bibleReadingManager.currentTranslation()).thenReturn(flowOf(MockContents.kjvShortName))
        `when`(translationManager.availableTranslations()).thenReturn(flowOf(listOf(MockContents.cuvTranslationInfo)))
        `when`(translationManager.downloadedTranslations()).thenReturn(flowOf(listOf(MockContents.kjvDownloadedTranslationInfo)))
        translationsViewModel = TranslationsViewModel(bibleReadingManager, translationManager, settingsManager)

        assertEquals(
                listOf(
                        TranslationList(
                                MockContents.kjvShortName,
                                listOf(MockContents.cuvTranslationInfo),
                                listOf(MockContents.kjvDownloadedTranslationInfo)
                        )
                ),
                translationsViewModel.translationList(false).toList()
        )
        verify(translationManager, times(1)).reload(false)
    }

    @Test
    fun testLoadTranslationListWithException() = testDispatcher.runBlockingTest {
        val exception = RuntimeException("random exception")
        `when`(translationManager.reload(true)).thenThrow(exception)

        translationsViewModel.translationList(true)
                .onCompletion { assertEquals(exception, it) }
                .catch { }
                .collect()
    }

    @Test
    fun testDownloadTranslation() = testDispatcher.runBlockingTest {
        `when`(bibleReadingManager.currentTranslation()).thenReturn(flowOf(""))
        val translationToDownload = MockContents.kjvTranslationInfo
        val progress = 89
        `when`(translationManager.downloadTranslation(translationToDownload)).thenReturn(flowOf(progress, 101))

        assertEquals(listOf(progress, -1),
                translationsViewModel.downloadTranslation(translationToDownload).toList())
        verify(translationManager, times(1)).downloadTranslation(translationToDownload)
    }

    @Test
    fun testDownloadTranslationWithException() = testDispatcher.runBlockingTest {
        val translationToDownload = MockContents.kjvTranslationInfo
        val exception = RuntimeException("random exception")
        `when`(translationManager.downloadTranslation(translationToDownload)).thenReturn(flow { throw exception })

        translationsViewModel.downloadTranslation(translationToDownload)
                .onCompletion { assertEquals(exception, it) }
                .catch { }
                .collect()
        verify(translationManager, times(1)).downloadTranslation(translationToDownload)
    }
}
