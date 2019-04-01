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

package me.xizzhu.android.joshua.reading.detail

import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.reading.ReadingInteractor
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*

class VerseDetailPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var readingInteractor: ReadingInteractor
    @Mock
    private lateinit var verseDetailView: VerseDetailView
    private lateinit var verseDetailOpenState: ConflatedBroadcastChannel<VerseIndex>
    private lateinit var currentTranslationShortName: BroadcastChannel<String>
    private lateinit var verseDetailPresenter: VerseDetailPresenter

    @Before
    override fun setup() {
        super.setup()

        verseDetailOpenState = ConflatedBroadcastChannel()
        `when`(readingInteractor.observeVerseDetailOpenState()).thenReturn(verseDetailOpenState.openSubscription())

        currentTranslationShortName = ConflatedBroadcastChannel(MockContents.kjvShortName)
        `when`(readingInteractor.observeCurrentTranslation()).thenReturn(currentTranslationShortName.openSubscription())

        verseDetailPresenter = VerseDetailPresenter(readingInteractor)

        verseDetailPresenter.attachView(verseDetailView)
    }

    @After
    override fun tearDown() {
        verseDetailPresenter.detachView()
        super.tearDown()
    }

    @Test
    fun testShow() {
        runBlocking {
            val verseIndex = VerseIndex(0, 0, 0)
            `when`(readingInteractor.readVerse(MockContents.kjvShortName, verseIndex)).thenReturn(MockContents.kjvVerses[0])

            verseDetailOpenState.send(verseIndex)

            verify(verseDetailView, times(1)).show()
            verify(verseDetailView, times(1)).showVerse(VerseDetail(MockContents.kjvVerses[0], false))
            verify(verseDetailView, never()).hide()
        }
    }

    @Test
    fun testHide() {
        runBlocking {
            verseDetailOpenState.send(VerseIndex.INVALID)

            verify(verseDetailView, never()).show()
            verify(verseDetailView, never()).showVerse(any())
            verify(verseDetailView, times(1)).hide()
        }
    }
}
