/*
 * Copyright (C) 2022 Xizhi Zhu
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
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.tests.BaseUnitTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
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
    fun `test initialize()`() {
        val titleClicked: () -> Unit = mockk()
        every { titleClicked() } returns Unit
        val navigate: (Int) -> Unit = mockk()
        every { navigate(any()) } returns Unit

        readingToolbar.initialize(mockk(), mockk(), mockk(), titleClicked, navigate)

        readingToolbar.performClick()
        verify(exactly = 1) { titleClicked() }

        val onMenuItemClickListener: Toolbar.OnMenuItemClickListener = (readingToolbar as Toolbar).getProperty("mOnMenuItemClickListener")
        assertFalse(onMenuItemClickListener.onMenuItemClick(readingToolbar.menu.findItem(R.id.action_translations)))

        assertTrue(onMenuItemClickListener.onMenuItemClick(readingToolbar.menu.findItem(R.id.action_reading_progress)))
        verify(exactly = 1) { navigate(Navigator.SCREEN_READING_PROGRESS) }

        assertTrue(onMenuItemClickListener.onMenuItemClick(readingToolbar.menu.findItem(R.id.action_bookmarks)))
        verify(exactly = 1) { navigate(Navigator.SCREEN_BOOKMARKS) }

        assertTrue(onMenuItemClickListener.onMenuItemClick(readingToolbar.menu.findItem(R.id.action_highlights)))
        verify(exactly = 1) { navigate(Navigator.SCREEN_HIGHLIGHTS) }

        assertTrue(onMenuItemClickListener.onMenuItemClick(readingToolbar.menu.findItem(R.id.action_notes)))
        verify(exactly = 1) { navigate(Navigator.SCREEN_NOTES) }

        assertTrue(onMenuItemClickListener.onMenuItemClick(readingToolbar.menu.findItem(R.id.action_search)))
        verify(exactly = 1) { navigate(Navigator.SCREEN_SEARCH) }

        assertTrue(onMenuItemClickListener.onMenuItemClick(readingToolbar.menu.findItem(R.id.action_settings)))
        verify(exactly = 1) { navigate(Navigator.SCREEN_SETTINGS) }
    }

    @Test
    fun `test setTranslationItems()`() {
        val requestParallelTranslation: (String) -> Unit = mockk()
        val removeParallelTranslation: (String) -> Unit = mockk()
        val selectCurrentTranslation: (String) -> Unit = mockk()
        every { selectCurrentTranslation(any()) } returns Unit
        val navigate: (Int) -> Unit = mockk()
        every { navigate(any()) } returns Unit

        readingToolbar.initialize(requestParallelTranslation, removeParallelTranslation, selectCurrentTranslation, mockk(), navigate)

        readingToolbar.setTranslationItems(listOf(
            TranslationItem.Translation(MockContents.bbeShortName, isCurrentTranslation = false, isParallelTranslation = true),
            TranslationItem.Translation(MockContents.kjvShortName, isCurrentTranslation = true, isParallelTranslation = false),
            TranslationItem.Translation(MockContents.cuvShortName, isCurrentTranslation = false, isParallelTranslation = false),
            TranslationItem.More,
        ))
        verify(exactly = 0) { selectCurrentTranslation(any()) }

        val spinner = readingToolbar.menu.findItem(R.id.action_translations).actionView as Spinner
        val onItemSelectedListener: AdapterView.OnItemSelectedListener = (spinner as AdapterView<*>).getProperty("mOnItemSelectedListener")
        onItemSelectedListener.onItemSelected(mockk(), null, 2, 0L)
        verify(exactly = 1) { selectCurrentTranslation(MockContents.cuvShortName) }

        onItemSelectedListener.onItemSelected(mockk(), null, 3, 0L)
        verify(exactly = 1) { navigate(Navigator.SCREEN_TRANSLATIONS) }
        assertEquals(
            TranslationItem.Translation(MockContents.cuvShortName, false, false),
            spinner.selectedItem
        )
    }
}
