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

package me.xizzhu.android.joshua.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.core.repository.BibleReadingRepository
import me.xizzhu.android.joshua.core.repository.TranslationRepository
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.*

class BibleReadingManagerTest : BaseUnitTest() {
    @Mock
    private lateinit var bibleReadingRepository: BibleReadingRepository
    @Mock
    private lateinit var translationRepository: TranslationRepository

    @Test
    fun testObserveDownloadedTranslations() = runBlocking {
        `when`(bibleReadingRepository.currentTranslation).thenReturn(flowOf(""))
        `when`(bibleReadingRepository.parallelTranslations).thenReturn(flowOf(emptyList()))
        val downloadedTranslations = MutableStateFlow<List<TranslationInfo>>(emptyList())
        `when`(translationRepository.downloadedTranslations).thenReturn(downloadedTranslations)

        // we need to create a BibleReadingManager instance to let TranslationRepository load downloaded translations
        val bibleReadingManager = BibleReadingManager(bibleReadingRepository, translationRepository, testDispatcher)

        // downloads a translation
        downloadedTranslations.value = listOf(MockContents.cuvTranslationInfo)

        // downloads another translation
        `when`(bibleReadingRepository.currentTranslation).thenReturn(flowOf(MockContents.cuvShortName))
        downloadedTranslations.value = listOf(MockContents.cuvTranslationInfo, MockContents.bbeTranslationInfo)

        // downloads yet another translation
        downloadedTranslations.value = listOf(MockContents.cuvTranslationInfo, MockContents.bbeTranslationInfo, MockContents.kjvTranslationInfo)

        // requests a parallel
        `when`(bibleReadingRepository.parallelTranslations).thenReturn(flowOf(listOf(MockContents.bbeShortName)))

        // requests another parallel
        `when`(bibleReadingRepository.parallelTranslations).thenReturn(flowOf(listOf(MockContents.bbeShortName, MockContents.kjvShortName)))

        // removes a translation that is selected as parallel
        downloadedTranslations.value = listOf(MockContents.cuvTranslationInfo, MockContents.kjvTranslationInfo)

        // removes a translation that is selected as current
        downloadedTranslations.value = listOf(MockContents.kjvTranslationInfo)

        // removes all translations
        downloadedTranslations.value = emptyList()

        with(inOrder(bibleReadingRepository)) {
            // initial state
            verify(bibleReadingRepository, times(1)).saveCurrentTranslation("")
            verify(bibleReadingRepository, times(1)).clearParallelTranslation()

            // downloads a translation
            verify(bibleReadingRepository, times(1)).saveCurrentTranslation(MockContents.cuvShortName)

            // downloads another translation (nothing here)

            // downloads yet another translation (nothing here)

            // requests a parallel (nothing here)

            // requests another parallel (nothing here)

            // removes a translation that is selected as parallel
            verify(bibleReadingRepository, times(1)).saveParallelTranslations(listOf(MockContents.kjvShortName))

            // // removes a translation that is selected as current
            verify(bibleReadingRepository, times(1)).saveCurrentTranslation(MockContents.kjvShortName)
            verify(bibleReadingRepository, times(1)).saveParallelTranslations(emptyList())

            // removes all translations
            verify(bibleReadingRepository, times(1)).saveCurrentTranslation("")
            verify(bibleReadingRepository, times(1)).clearParallelTranslation()
        }
    }
}
