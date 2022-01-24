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
import androidx.appcompat.app.AppCompatActivity
import io.mockk.Ordering
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import kotlin.test.BeforeTest
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
class ReadingToolbarTest : BaseUnitTest() {
    private lateinit var context: Context
    private lateinit var spinner: Spinner
    private lateinit var spinnerAdapter: TranslationSpinnerAdapter
    private lateinit var readingToolbar: ReadingToolbar

    @BeforeTest
    override fun setup() {
        super.setup()

        context = Robolectric.buildActivity(AppCompatActivity::class.java).get()
        spinner = mockk()
        every { spinner.context } returns context
        spinnerAdapter = mockk()

        readingToolbar = spyk(ReadingToolbar(context))
        every { readingToolbar.context } returns context
        every { readingToolbar.spinner() } returns spinner
        every { readingToolbar.spinnerAdapter() } returns spinnerAdapter
    }

    @Test
    fun `test initialize`() {
        val requestParallelTranslation: (String) -> Unit = mockk()
        val removeParallelTranslation: (String) -> Unit = mockk()
        val selectCurrentTranslation: (String) -> Unit = mockk()
        every { selectCurrentTranslation(any()) } returns Unit
        val titleClicked: () -> Unit = mockk()
        every { titleClicked() } returns Unit
        val navigate: (Int) -> Unit = mockk()
        every { navigate(any()) } returns Unit

        every { spinner.adapter = any() } returns Unit

        var onItemSelected: AdapterView.OnItemSelectedListener? = null
        every { spinner.onItemSelectedListener = any() } answers {
            onItemSelected = this.args[0] as AdapterView.OnItemSelectedListener
        }

        readingToolbar.initialize(requestParallelTranslation, removeParallelTranslation, selectCurrentTranslation, titleClicked, navigate)

        readingToolbar.performClick()
        verify(exactly = 1) { titleClicked() }

        every { spinnerAdapter.count } returns 3
        onItemSelected!!.onItemSelected(spinner, spinner, 2, 0) // clicks on "More"
        verify(exactly = 1) { navigate(Navigator.SCREEN_TRANSLATIONS) }

        every { spinnerAdapter.getItem(1) } returns MockContents.kjvShortName
        onItemSelected!!.onItemSelected(spinner, spinner, 1, 0)
        verify(exactly = 1) { selectCurrentTranslation(MockContents.kjvShortName) }

        // clicks on "More" again, should make sure it still selects the previously selected translation
        every { spinner.setSelection(1) } returns Unit
        onItemSelected!!.onItemSelected(spinner, spinner, 2, 0)
        verify(ordering = Ordering.ORDERED) {
            spinner.setSelection(1)
            navigate(Navigator.SCREEN_TRANSLATIONS)
        }
    }

    @Test
    fun `test setData`() {
        every { spinner.setSelection(2) } returns Unit
        every {
            spinnerAdapter.setData(
                    currentTranslation = MockContents.cuvShortName,
                    parallelTranslations = listOf(MockContents.bbeShortName),
                    downloadedTranslations = listOf(MockContents.bbeShortName, MockContents.kjvShortName, MockContents.cuvShortName, "More")
            )
        } returns Unit

        readingToolbar.setData(
                currentTranslation = MockContents.cuvShortName,
                parallelTranslations = listOf(MockContents.bbeShortName),
                downloadedTranslations = listOf(MockContents.bbeShortName, MockContents.kjvShortName, MockContents.cuvShortName)
        )

        verify(exactly = 1) { spinnerAdapter.setData(any(), any(), any()) }
        verify(exactly = 1) { spinner.setSelection(any()) }
    }
}
