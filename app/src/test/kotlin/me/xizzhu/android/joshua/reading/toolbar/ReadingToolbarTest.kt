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

package me.xizzhu.android.joshua.reading.toolbar

import android.content.Context
import android.widget.AdapterView
import android.widget.Spinner
import androidx.appcompat.widget.Toolbar
import androidx.test.core.app.ApplicationProvider
import io.mockk.mockk
import me.xizzhu.android.joshua.tests.BaseUnitTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.tests.getProperty

@RunWith(RobolectricTestRunner::class)
class ReadingToolbarTest : BaseUnitTest() {
    private lateinit var readingToolbar: ReadingToolbar

    @BeforeTest
    override fun setup() {
        super.setup()

        ApplicationProvider.getApplicationContext<Context>().setTheme(R.style.AppTheme)

        readingToolbar = ReadingToolbar(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun `test without data`() {
        var openBookmarksCalled = 0
        var openHighlightsCalled = 0
        var openNotesCalled = 0
        var openReadingProgressCalled = 0
        var openSearchCalled = 0
        var openSettingsCalled = 0
        var titleClickedCalled = 0
        readingToolbar.initialize { viewEvent ->
            when (viewEvent) {
                is ReadingToolbar.ViewEvent.OpenBookmarks -> openBookmarksCalled++
                is ReadingToolbar.ViewEvent.OpenHighlights -> openHighlightsCalled++
                is ReadingToolbar.ViewEvent.OpenNotes -> openNotesCalled++
                is ReadingToolbar.ViewEvent.OpenReadingProgress -> openReadingProgressCalled++
                is ReadingToolbar.ViewEvent.OpenSearch -> openSearchCalled++
                is ReadingToolbar.ViewEvent.OpenSettings -> openSettingsCalled++
                is ReadingToolbar.ViewEvent.OpenTranslations -> fail()
                is ReadingToolbar.ViewEvent.RemoveParallelTranslation -> fail()
                is ReadingToolbar.ViewEvent.RequestParallelTranslation -> fail()
                is ReadingToolbar.ViewEvent.SelectCurrentTranslation -> fail()
                is ReadingToolbar.ViewEvent.TitleClicked -> titleClickedCalled++
            }
        }
        assertEquals(0, openBookmarksCalled)
        assertEquals(0, openHighlightsCalled)
        assertEquals(0, openNotesCalled)
        assertEquals(0, openReadingProgressCalled)
        assertEquals(0, openSearchCalled)
        assertEquals(0, openSettingsCalled)
        assertEquals(0, titleClickedCalled)

        readingToolbar.performClick()
        assertEquals(0, openBookmarksCalled)
        assertEquals(0, openHighlightsCalled)
        assertEquals(0, openNotesCalled)
        assertEquals(0, openReadingProgressCalled)
        assertEquals(0, openSearchCalled)
        assertEquals(0, openSettingsCalled)
        assertEquals(1, titleClickedCalled)

        val onMenuItemClickListener: Toolbar.OnMenuItemClickListener = (readingToolbar as Toolbar).getProperty("mOnMenuItemClickListener")
        assertFalse(onMenuItemClickListener.onMenuItemClick(readingToolbar.menu.findItem(R.id.action_translations)))

        assertTrue(onMenuItemClickListener.onMenuItemClick(readingToolbar.menu.findItem(R.id.action_reading_progress)))
        assertEquals(0, openBookmarksCalled)
        assertEquals(0, openHighlightsCalled)
        assertEquals(0, openNotesCalled)
        assertEquals(1, openReadingProgressCalled)
        assertEquals(0, openSearchCalled)
        assertEquals(0, openSettingsCalled)
        assertEquals(1, titleClickedCalled)

        assertTrue(onMenuItemClickListener.onMenuItemClick(readingToolbar.menu.findItem(R.id.action_bookmarks)))
        assertEquals(1, openBookmarksCalled)
        assertEquals(0, openHighlightsCalled)
        assertEquals(0, openNotesCalled)
        assertEquals(1, openReadingProgressCalled)
        assertEquals(0, openSearchCalled)
        assertEquals(0, openSettingsCalled)
        assertEquals(1, titleClickedCalled)

        assertTrue(onMenuItemClickListener.onMenuItemClick(readingToolbar.menu.findItem(R.id.action_highlights)))
        assertEquals(1, openBookmarksCalled)
        assertEquals(1, openHighlightsCalled)
        assertEquals(0, openNotesCalled)
        assertEquals(1, openReadingProgressCalled)
        assertEquals(0, openSearchCalled)
        assertEquals(0, openSettingsCalled)
        assertEquals(1, titleClickedCalled)

        assertTrue(onMenuItemClickListener.onMenuItemClick(readingToolbar.menu.findItem(R.id.action_notes)))
        assertEquals(1, openBookmarksCalled)
        assertEquals(1, openHighlightsCalled)
        assertEquals(1, openNotesCalled)
        assertEquals(1, openReadingProgressCalled)
        assertEquals(0, openSearchCalled)
        assertEquals(0, openSettingsCalled)
        assertEquals(1, titleClickedCalled)

        assertTrue(onMenuItemClickListener.onMenuItemClick(readingToolbar.menu.findItem(R.id.action_search)))
        assertEquals(1, openBookmarksCalled)
        assertEquals(1, openHighlightsCalled)
        assertEquals(1, openNotesCalled)
        assertEquals(1, openReadingProgressCalled)
        assertEquals(1, openSearchCalled)
        assertEquals(0, openSettingsCalled)
        assertEquals(1, titleClickedCalled)

        assertTrue(onMenuItemClickListener.onMenuItemClick(readingToolbar.menu.findItem(R.id.action_settings)))
        assertEquals(1, openBookmarksCalled)
        assertEquals(1, openHighlightsCalled)
        assertEquals(1, openNotesCalled)
        assertEquals(1, openReadingProgressCalled)
        assertEquals(1, openSearchCalled)
        assertEquals(1, openSettingsCalled)
        assertEquals(1, titleClickedCalled)
    }

    @Test
    fun `test with data`() {
        var openTranslationsCalled = 0
        var removeParallelTranslationCalled = 0
        var requestParallelTranslationCalled = 0
        var selectCurrentTranslationCalled = 0
        readingToolbar.initialize { viewEvent ->
            when (viewEvent) {
                is ReadingToolbar.ViewEvent.OpenBookmarks -> fail()
                is ReadingToolbar.ViewEvent.OpenHighlights -> fail()
                is ReadingToolbar.ViewEvent.OpenNotes -> fail()
                is ReadingToolbar.ViewEvent.OpenReadingProgress -> fail()
                is ReadingToolbar.ViewEvent.OpenSearch -> fail()
                is ReadingToolbar.ViewEvent.OpenSettings -> fail()
                is ReadingToolbar.ViewEvent.OpenTranslations -> openTranslationsCalled++
                is ReadingToolbar.ViewEvent.RemoveParallelTranslation -> {
                    removeParallelTranslationCalled++
                }
                is ReadingToolbar.ViewEvent.RequestParallelTranslation -> {
                    requestParallelTranslationCalled++
                }
                is ReadingToolbar.ViewEvent.SelectCurrentTranslation -> {
                    assertEquals(MockContents.cuvShortName, viewEvent.translationToSelect)
                    selectCurrentTranslationCalled++
                }
                is ReadingToolbar.ViewEvent.TitleClicked -> fail()
            }
        }
        assertEquals(0, openTranslationsCalled)
        assertEquals(0, removeParallelTranslationCalled)
        assertEquals(0, requestParallelTranslationCalled)
        assertEquals(0, selectCurrentTranslationCalled)

        readingToolbar.setViewState(ReadingToolbar.ViewState(
            translationItems = listOf(
                ReadingToolbar.ViewState.TranslationItem.Translation(MockContents.bbeShortName, isCurrentTranslation = false, isParallelTranslation = true),
                ReadingToolbar.ViewState.TranslationItem.Translation(MockContents.kjvShortName, isCurrentTranslation = true, isParallelTranslation = false),
                ReadingToolbar.ViewState.TranslationItem.Translation(MockContents.cuvShortName, isCurrentTranslation = false, isParallelTranslation = false),
                ReadingToolbar.ViewState.TranslationItem.More,
            )
        ))
        assertEquals(0, openTranslationsCalled)
        assertEquals(0, removeParallelTranslationCalled)
        assertEquals(0, requestParallelTranslationCalled)
        assertEquals(0, selectCurrentTranslationCalled)

        val spinner = readingToolbar.menu.findItem(R.id.action_translations).actionView as Spinner
        val onItemSelectedListener: AdapterView.OnItemSelectedListener = (spinner as AdapterView<*>).getProperty("mOnItemSelectedListener")
        onItemSelectedListener.onItemSelected(mockk(), null, 2, 0L)
        assertEquals(0, openTranslationsCalled)
        assertEquals(0, removeParallelTranslationCalled)
        assertEquals(0, requestParallelTranslationCalled)
        assertEquals(1, selectCurrentTranslationCalled)

        onItemSelectedListener.onItemSelected(mockk(), null, 3, 0L)
        assertEquals(1, openTranslationsCalled)
        assertEquals(0, removeParallelTranslationCalled)
        assertEquals(0, requestParallelTranslationCalled)
        assertEquals(1, selectCurrentTranslationCalled)
        assertEquals(
            ReadingToolbar.ViewState.TranslationItem.Translation(MockContents.cuvShortName, false, false),
            spinner.selectedItem
        )
    }
}
