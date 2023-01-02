/*
 * Copyright (C) 2023 Xizhi Zhu
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

package me.xizzhu.android.joshua.annotated.highlights

import android.app.Application
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.emptyFlow
import me.xizzhu.android.joshua.annotated.AnnotatedVerseItem
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.VerseAnnotationManager
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.tests.TestTimeProvider
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class HighlightsViewModelTest : BaseUnitTest() {
    private lateinit var bibleReadingManager: BibleReadingManager
    private lateinit var highlightsManager: VerseAnnotationManager<Highlight>
    private lateinit var settingsManager: SettingsManager
    private lateinit var application: Application
    private lateinit var highlightsViewModel: HighlightsViewModel

    @BeforeTest
    override fun setup() {
        super.setup()

        bibleReadingManager = mockk<BibleReadingManager>().apply { every { currentTranslation() } returns emptyFlow() }
        highlightsManager = mockk<VerseAnnotationManager<Highlight>>().apply { every { sortOrder() } returns emptyFlow() }
        settingsManager = mockk<SettingsManager>().apply { every { settings() } returns emptyFlow() }
        application = mockk()
        highlightsViewModel = HighlightsViewModel(bibleReadingManager, highlightsManager, settingsManager, testCoroutineDispatcherProvider, TestTimeProvider(), application)
    }

    @Test
    fun `test buildAnnotatedVerseItem()`() {
        assertEquals(
            AnnotatedVerseItem.Highlight(
                Settings.DEFAULT,
                MockContents.kjvVerses[0].verseIndex,
                MockContents.kjvBookNames[0],
                MockContents.kjvBookShortNames[0],
                MockContents.kjvVerses[0].text.text,
                Highlight.COLOR_BLUE,
                Constants.SORT_BY_BOOK
            ),
            highlightsViewModel.buildAnnotatedVerseItem(
                settings = Settings.DEFAULT,
                verseAnnotation = Highlight(VerseIndex(0, 0, 0), Highlight.COLOR_BLUE, 1L),
                bookName = MockContents.kjvBookNames[0],
                bookShortName = MockContents.kjvBookShortNames[0],
                verseText = MockContents.kjvVerses[0].text.text,
                sortOrder = Constants.SORT_BY_BOOK
            )
        )
    }
}
