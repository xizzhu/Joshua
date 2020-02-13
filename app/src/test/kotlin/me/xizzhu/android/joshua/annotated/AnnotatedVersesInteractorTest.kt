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

package me.xizzhu.android.joshua.annotated

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.Bookmark
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.VerseAnnotationManager
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AnnotatedVersesInteractorTest : BaseUnitTest() {
    @Mock
    private lateinit var bookmarkManager: VerseAnnotationManager<Bookmark>
    @Mock
    private lateinit var bibleReadingManager: BibleReadingManager
    @Mock
    private lateinit var settingsManager: SettingsManager

    private lateinit var annotatedVersesInteractor: AnnotatedVersesInteractor<Bookmark>

    @BeforeTest
    override fun setup() {
        super.setup()

        annotatedVersesInteractor = AnnotatedVersesInteractor(
                bookmarkManager, bibleReadingManager, settingsManager, testDispatcher)
    }

    @Test
    fun testCurrentTranslation() = testDispatcher.runBlockingTest {
        `when`(bibleReadingManager.currentTranslation())
                .thenReturn(flowOf("", MockContents.kjvShortName, "", "", MockContents.cuvShortName, "", MockContents.bbeShortName))

        assertEquals(
                listOf(ViewData.success(MockContents.kjvShortName), ViewData.success(MockContents.cuvShortName), ViewData.success(MockContents.bbeShortName)),
                annotatedVersesInteractor.currentTranslation().toList()
        )
    }
}
