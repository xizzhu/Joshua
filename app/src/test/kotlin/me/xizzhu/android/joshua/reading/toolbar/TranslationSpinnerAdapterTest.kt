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
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.test.core.app.ApplicationProvider
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.tests.performClickPressed
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TranslationSpinnerAdapterTest : BaseUnitTest() {
    @BeforeTest
    override fun setup() {
        super.setup()
        ApplicationProvider.getApplicationContext<Context>().setTheme(R.style.AppTheme)
    }

    @Test
    fun `test without data`() {
        val adapter = TranslationSpinnerAdapter(
            context = ApplicationProvider.getApplicationContext(),
            onViewEvent = { fail() },
        )
        assertEquals(0, adapter.count)
    }

    @Test
    fun `test without parallel translations`() {
        var requestParallelTranslationCalled = 0
        val adapter = TranslationSpinnerAdapter(
            context = ApplicationProvider.getApplicationContext(),
        ) { viewEvent ->
            when (viewEvent) {
                is ReadingToolbar.ViewEvent.OpenBookmarks -> fail()
                is ReadingToolbar.ViewEvent.OpenHighlights -> fail()
                is ReadingToolbar.ViewEvent.OpenNotes -> fail()
                is ReadingToolbar.ViewEvent.OpenReadingProgress -> fail()
                is ReadingToolbar.ViewEvent.OpenSearch -> fail()
                is ReadingToolbar.ViewEvent.OpenSettings -> fail()
                is ReadingToolbar.ViewEvent.OpenTranslations -> fail()
                is ReadingToolbar.ViewEvent.RemoveParallelTranslation -> fail()
                is ReadingToolbar.ViewEvent.RequestParallelTranslation -> {
                    assertEquals("MSG", viewEvent.translationToRequest)
                    requestParallelTranslationCalled++
                }
                is ReadingToolbar.ViewEvent.SelectCurrentTranslation -> fail()
                is ReadingToolbar.ViewEvent.TitleClicked -> fail()
            }
        }
        adapter.setItems(listOf(
            TranslationItem.Translation(
                translationShortName = MockContents.kjvDownloadedTranslationInfo.shortName,
                isCurrentTranslation = true,
                isParallelTranslation = false,
            ),
            TranslationItem.Translation(
                translationShortName = MockContents.msgDownloadedTranslationInfo.shortName,
                isCurrentTranslation = false,
                isParallelTranslation = false,
            ),
            TranslationItem.More,
        ))

        assertEquals(0, requestParallelTranslationCalled)

        assertEquals(3, adapter.count)

        val view = adapter.getView(0, null, FrameLayout(ApplicationProvider.getApplicationContext()))
        assertEquals("KJV", (view as TextView).text.toString())
        assertEquals("MSG", (adapter.getView(1, view, FrameLayout(ApplicationProvider.getApplicationContext())) as TextView).text.toString())

        val firstDropDownView = adapter.getDropDownView(0, null, FrameLayout(ApplicationProvider.getApplicationContext()))
        assertEquals("KJV", firstDropDownView.findViewById<TextView>(R.id.title).text.toString())
        assertTrue(firstDropDownView.findViewById<CheckBox>(R.id.checkbox).isVisible)
        assertFalse(firstDropDownView.findViewById<CheckBox>(R.id.checkbox).isEnabled)
        assertTrue(firstDropDownView.findViewById<CheckBox>(R.id.checkbox).isChecked)

        with(adapter.getDropDownView(1, firstDropDownView, FrameLayout(ApplicationProvider.getApplicationContext()))) {
            assertEquals("MSG", findViewById<TextView>(R.id.title).text.toString())
            assertTrue(findViewById<CheckBox>(R.id.checkbox).isVisible)
            assertTrue(findViewById<CheckBox>(R.id.checkbox).isEnabled)
            assertFalse(findViewById<CheckBox>(R.id.checkbox).isChecked)
        }

        with(adapter.getDropDownView(2, firstDropDownView, FrameLayout(ApplicationProvider.getApplicationContext()))) {
            assertEquals("More", findViewById<TextView>(R.id.title).text.toString())
            assertFalse(findViewById<CheckBox>(R.id.checkbox).isVisible)
            assertFalse(findViewById<CheckBox>(R.id.checkbox).isEnabled)
            assertFalse(findViewById<CheckBox>(R.id.checkbox).isChecked)
        }

        adapter.getDropDownView(1, null, FrameLayout(ApplicationProvider.getApplicationContext()))
            .findViewById<CheckBox>(R.id.checkbox).performClickPressed()
        assertEquals(1, requestParallelTranslationCalled)
    }

    @Test
    fun `test with parallel translations`() {
        var removeParallelTranslationCalled = 0
        val adapter = TranslationSpinnerAdapter(
            context = ApplicationProvider.getApplicationContext(),
        ) { viewEvent ->
            when (viewEvent) {
                is ReadingToolbar.ViewEvent.OpenBookmarks -> fail()
                is ReadingToolbar.ViewEvent.OpenHighlights -> fail()
                is ReadingToolbar.ViewEvent.OpenNotes -> fail()
                is ReadingToolbar.ViewEvent.OpenReadingProgress -> fail()
                is ReadingToolbar.ViewEvent.OpenSearch -> fail()
                is ReadingToolbar.ViewEvent.OpenSettings -> fail()
                is ReadingToolbar.ViewEvent.OpenTranslations -> fail()
                is ReadingToolbar.ViewEvent.RemoveParallelTranslation -> {
                    assertEquals("MSG", viewEvent.translationToRemove)
                    removeParallelTranslationCalled++
                }
                is ReadingToolbar.ViewEvent.RequestParallelTranslation -> fail()
                is ReadingToolbar.ViewEvent.SelectCurrentTranslation -> fail()
                is ReadingToolbar.ViewEvent.TitleClicked -> fail()
            }
        }
        adapter.setItems(listOf(
            TranslationItem.Translation(
                translationShortName = MockContents.kjvDownloadedTranslationInfo.shortName,
                isCurrentTranslation = true,
                isParallelTranslation = false,
            ),
            TranslationItem.Translation(
                translationShortName = MockContents.msgDownloadedTranslationInfo.shortName,
                isCurrentTranslation = false,
                isParallelTranslation = true,
            ),
            TranslationItem.More,
        ))

        assertEquals(0, removeParallelTranslationCalled)

        assertEquals(3, adapter.count)

        val view = adapter.getView(0, null, FrameLayout(ApplicationProvider.getApplicationContext()))
        assertEquals("KJV", (view as TextView).text.toString())
        assertEquals("MSG", (adapter.getView(1, view, FrameLayout(ApplicationProvider.getApplicationContext())) as TextView).text.toString())

        val firstDropDownView = adapter.getDropDownView(0, null, FrameLayout(ApplicationProvider.getApplicationContext()))
        assertEquals("KJV", firstDropDownView.findViewById<TextView>(R.id.title).text.toString())
        assertTrue(firstDropDownView.findViewById<CheckBox>(R.id.checkbox).isVisible)
        assertFalse(firstDropDownView.findViewById<CheckBox>(R.id.checkbox).isEnabled)
        assertTrue(firstDropDownView.findViewById<CheckBox>(R.id.checkbox).isChecked)

        with(adapter.getDropDownView(1, firstDropDownView, FrameLayout(ApplicationProvider.getApplicationContext()))) {
            assertEquals("MSG", findViewById<TextView>(R.id.title).text.toString())
            assertTrue(findViewById<CheckBox>(R.id.checkbox).isVisible)
            assertTrue(findViewById<CheckBox>(R.id.checkbox).isEnabled)
            assertTrue(findViewById<CheckBox>(R.id.checkbox).isChecked)
        }

        with(adapter.getDropDownView(2, firstDropDownView, FrameLayout(ApplicationProvider.getApplicationContext()))) {
            assertEquals("More", findViewById<TextView>(R.id.title).text.toString())
            assertFalse(findViewById<CheckBox>(R.id.checkbox).isVisible)
            assertFalse(findViewById<CheckBox>(R.id.checkbox).isEnabled)
            assertFalse(findViewById<CheckBox>(R.id.checkbox).isChecked)
        }

        adapter.getDropDownView(1, null, FrameLayout(ApplicationProvider.getApplicationContext()))
            .findViewById<CheckBox>(R.id.checkbox).performClickPressed()
        assertEquals(1, removeParallelTranslationCalled)
    }
}
