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

package me.xizzhu.android.joshua.annotated

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.Bookmark
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.infra.arch.viewData
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BaseAnnotatedVersesInteractorTest : BaseUnitTest() {
    @Mock
    private lateinit var bibleReadingManager: BibleReadingManager
    @Mock
    private lateinit var settingsManager: SettingsManager

    private lateinit var baseAnnotatedVersesInteractor: BaseAnnotatedVersesInteractor<Bookmark>

    @BeforeTest
    override fun setup() {
        super.setup()

        baseAnnotatedVersesInteractor = object : BaseAnnotatedVersesInteractor<Bookmark>(
                bibleReadingManager, settingsManager, testDispatcher) {
            override fun sortOrder(): Flow<ViewData<Int>> = emptyFlow()

            override suspend fun verseAnnotations(sortOrder: Int): ViewData<List<Bookmark>> = viewData { emptyList<Bookmark>() }
        }
    }

    @Test
    fun testCurrentTranslation() = testDispatcher.runBlockingTest {
        `when`(bibleReadingManager.observeCurrentTranslation()).thenReturn(flowOf(""))

        baseAnnotatedVersesInteractor.start()
        verify(bibleReadingManager, times(1)).observeCurrentTranslation()

        assertEquals("", baseAnnotatedVersesInteractor.currentTranslation())
        verify(bibleReadingManager, times(2)).observeCurrentTranslation()

        `when`(bibleReadingManager.observeCurrentTranslation()).thenReturn(flowOf(MockContents.kjvShortName))
        assertEquals(MockContents.kjvShortName, baseAnnotatedVersesInteractor.currentTranslation())
        verify(bibleReadingManager, times(3)).observeCurrentTranslation()

        assertEquals(MockContents.kjvShortName, baseAnnotatedVersesInteractor.currentTranslation())
        verify(bibleReadingManager, times(3)).observeCurrentTranslation()

        baseAnnotatedVersesInteractor.stop()
    }
}
