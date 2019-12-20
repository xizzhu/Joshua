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

package me.xizzhu.android.joshua.core

import kotlinx.coroutines.async
import kotlinx.coroutines.flow.asFlow

import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.core.repository.BibleReadingRepository
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.mockito.Mock
import org.mockito.Mockito.`when`
import kotlin.test.*

class BibleReadingManagerTest : BaseUnitTest() {
    @Mock
    private lateinit var bibleReadingRepository: BibleReadingRepository
    @Mock
    private lateinit var translationManager: TranslationManager

    private lateinit var downloadedTranslationsChannel: ConflatedBroadcastChannel<List<TranslationInfo>>
    private lateinit var bibleReadingManager: BibleReadingManager

    @BeforeTest
    override fun setup() {
        super.setup()

        testDispatcher.runBlockingTest {
            `when`(bibleReadingRepository.readCurrentTranslation()).thenReturn(MockContents.kjvShortName)
            `when`(bibleReadingRepository.readParallelTranslations()).thenReturn(emptyList())
            `when`(bibleReadingRepository.readCurrentVerseIndex()).thenReturn(VerseIndex(1, 2, 3))

            downloadedTranslationsChannel = ConflatedBroadcastChannel(listOf(MockContents.kjvDownloadedTranslationInfo))
            `when`(translationManager.observeDownloadedTranslations()).thenReturn(downloadedTranslationsChannel.asFlow())
            bibleReadingManager = BibleReadingManager(bibleReadingRepository, translationManager, testDispatcher)
        }
    }

    @Test
    fun testObserveInitialCurrentVerseIndex() = testDispatcher.runBlockingTest {
        assertEquals(VerseIndex(1, 2, 3), bibleReadingManager.observeCurrentVerseIndex().first())
    }

    @Test
    fun testObserveInitialCurrentVerseIndexWithException() = testDispatcher.runBlockingTest {
        `when`(bibleReadingRepository.readCurrentVerseIndex()).thenThrow(RuntimeException("Random exception"))
        bibleReadingManager = BibleReadingManager(bibleReadingRepository, translationManager)

        assertFalse(bibleReadingManager.observeCurrentVerseIndex().first().isValid())
    }

    @Test
    fun testSaveCurrentVerseIndex() = testDispatcher.runBlockingTest {
        bibleReadingManager.saveCurrentVerseIndex(VerseIndex(4, 5, 6))
        assertEquals(VerseIndex(4, 5, 6), bibleReadingManager.observeCurrentVerseIndex().first())
    }

    @Test
    fun testSaveCurrentTranslation() = testDispatcher.runBlockingTest {
        bibleReadingManager.saveCurrentTranslation(MockContents.cuvShortName)
        assertEquals(MockContents.cuvShortName, bibleReadingManager.observeCurrentTranslation().first())
    }

    @Test
    fun testObserveInitialCurrentTranslation() = testDispatcher.runBlockingTest {
        assertEquals(MockContents.kjvShortName, bibleReadingManager.observeCurrentTranslation().first())
    }

    @Test
    fun testObserveInitialCurrentTranslationWithException() = testDispatcher.runBlockingTest {
        `when`(bibleReadingRepository.readCurrentTranslation()).thenThrow(RuntimeException("Random exception"))
        bibleReadingManager = BibleReadingManager(bibleReadingRepository, translationManager)

        assertTrue(bibleReadingManager.observeCurrentTranslation().first().isEmpty())
    }

    @Test
    fun testObserveCurrentTranslation() = testDispatcher.runBlockingTest {
        // removing all downloaded translations
        downloadedTranslationsChannel.offer(emptyList())
        assertEquals("", bibleReadingManager.observeCurrentTranslation().first())

        // download a translation
        downloadedTranslationsChannel.offer(listOf(MockContents.cuvTranslationInfo))
        assertEquals(MockContents.cuvShortName, bibleReadingManager.observeCurrentTranslation().first())

        // download another translation
        downloadedTranslationsChannel.offer(listOf(MockContents.cuvTranslationInfo, MockContents.bbeTranslationInfo))
        assertEquals(MockContents.cuvShortName, bibleReadingManager.observeCurrentTranslation().first())

        // download another translation
        downloadedTranslationsChannel.offer(listOf(MockContents.cuvTranslationInfo, MockContents.bbeTranslationInfo, MockContents.kjvTranslationInfo))
        assertEquals(MockContents.cuvShortName, bibleReadingManager.observeCurrentTranslation().first())

        // remove a translation
        downloadedTranslationsChannel.offer(listOf(MockContents.cuvTranslationInfo, MockContents.bbeTranslationInfo))
        assertEquals(MockContents.cuvShortName, bibleReadingManager.observeCurrentTranslation().first())

        // remove another translation
        downloadedTranslationsChannel.offer(listOf(MockContents.bbeTranslationInfo))
        assertEquals(MockContents.bbeShortName, bibleReadingManager.observeCurrentTranslation().first())

        // removing all downloaded translations
        downloadedTranslationsChannel.offer(emptyList())
        assertEquals("", bibleReadingManager.observeCurrentTranslation().first())
    }

    @Test
    fun testDefaultParallelTranslations() = testDispatcher.runBlockingTest {
        assertTrue(bibleReadingManager.observeParallelTranslations().first().isEmpty())
    }

    @Test
    fun testParallelTranslations() = testDispatcher.runBlockingTest {
        bibleReadingManager.requestParallelTranslation(MockContents.kjvShortName)
        assertEquals(listOf(MockContents.kjvShortName),
                bibleReadingManager.observeParallelTranslations().first())

        bibleReadingManager.requestParallelTranslation(MockContents.kjvShortName)
        bibleReadingManager.requestParallelTranslation(MockContents.cuvShortName)
        assertEquals(setOf(MockContents.kjvShortName, MockContents.cuvShortName),
                bibleReadingManager.observeParallelTranslations().first().toSet())

        bibleReadingManager.removeParallelTranslation(MockContents.kjvShortName)
        assertEquals(listOf(MockContents.cuvShortName),
                bibleReadingManager.observeParallelTranslations().first())

        bibleReadingManager.removeParallelTranslation(MockContents.cuvShortName)
        assertTrue(bibleReadingManager.observeParallelTranslations().first().isEmpty())
    }

    @Test
    fun testRemoveNonExistParallelTranslations() = testDispatcher.runBlockingTest {
        bibleReadingManager.requestParallelTranslation(MockContents.kjvShortName)
        bibleReadingManager.requestParallelTranslation(MockContents.cuvShortName)
        assertEquals(setOf(MockContents.kjvShortName, MockContents.cuvShortName),
                bibleReadingManager.observeParallelTranslations().first().toSet())

        bibleReadingManager.removeParallelTranslation("not_exist")
        assertEquals(setOf(MockContents.kjvShortName, MockContents.cuvShortName),
                bibleReadingManager.observeParallelTranslations().first().toSet())
    }

    @Test
    fun testClearParallelTranslations() = testDispatcher.runBlockingTest {
        bibleReadingManager.requestParallelTranslation(MockContents.kjvShortName)
        bibleReadingManager.requestParallelTranslation(MockContents.cuvShortName)
        assertEquals(setOf(MockContents.kjvShortName, MockContents.cuvShortName),
                bibleReadingManager.observeParallelTranslations().first().toSet())

        bibleReadingManager.clearParallelTranslation()
        assertTrue(bibleReadingManager.observeParallelTranslations().first().isEmpty())
    }
}
