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

import android.content.res.Resources
import android.view.View
import android.widget.ProgressBar
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.ui.fadeIn
import me.xizzhu.android.joshua.ui.recyclerview.CommonRecyclerView
import me.xizzhu.android.joshua.ui.recyclerview.TextItem
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import me.xizzhu.android.joshua.utils.currentTimeMillis
import org.mockito.Mock
import org.mockito.Mockito.*
import java.util.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BaseAnnotatedVersesPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var resources: Resources
    @Mock
    private lateinit var lifecycle: Lifecycle
    @Mock
    private lateinit var activity: TestVerseAnnotationsActivity
    @Mock
    private lateinit var navigator: Navigator
    private val noItemText = R.string.text_no_bookmark
    @Mock
    private lateinit var verseAnnotationViewModel: TestVerseAnnotationViewModel
    @Mock
    private lateinit var loadingSpinner: ProgressBar
    @Mock
    private lateinit var annotatedVerseListView: CommonRecyclerView

    private lateinit var annotatedVersesViewHolder: AnnotatedVersesViewHolder
    private lateinit var baseAnnotatedVersesPresenter: BaseAnnotatedVersesPresenter<TestVerseAnnotation, TestVerseAnnotationsActivity>

    @BeforeTest
    override fun setup() {
        super.setup()

        `when`(activity.resources).thenReturn(resources)
        `when`(activity.lifecycle).thenReturn(lifecycle)

        `when`(verseAnnotationViewModel.settings()).thenReturn(emptyFlow())
        `when`(verseAnnotationViewModel.sortOrder()).thenReturn(emptyFlow())
        `when`(verseAnnotationViewModel.loadingRequest()).thenReturn(emptyFlow())

        annotatedVersesViewHolder = AnnotatedVersesViewHolder(loadingSpinner, annotatedVerseListView)
        baseAnnotatedVersesPresenter = TestVerseAnnotationPresenter(navigator, noItemText, verseAnnotationViewModel, activity, testCoroutineScope)
        baseAnnotatedVersesPresenter.bind(annotatedVersesViewHolder)
    }

    @Test
    fun testObserveSettings() = testDispatcher.runBlockingTest {
        val settings = Settings.DEFAULT.copy(keepScreenOn = false)
        `when`(verseAnnotationViewModel.settings()).thenReturn(flowOf(settings))

        baseAnnotatedVersesPresenter.onCreate()
        verify(annotatedVerseListView, times(1)).setSettings(settings)
    }

    @Test
    fun testLoadAnnotatedVerses() = testDispatcher.runBlockingTest {
        val sortOrder = Constants.SORT_BY_DATE
        val currentTranslation = MockContents.kjvShortName
        val title = "random title"
        `when`(activity.getString(noItemText)).thenReturn(title)
        `when`(verseAnnotationViewModel.loadingRequest()).thenReturn(flowOf(LoadingRequest(currentTranslation, sortOrder)))
        `when`(verseAnnotationViewModel.annotatedVerses(LoadingRequest(currentTranslation, sortOrder))).thenReturn(flowOf(
                AnnotatedVerses(sortOrder, emptyList(), MockContents.kjvBookNames, MockContents.kjvBookShortNames)
        ))

        baseAnnotatedVersesPresenter.onCreate()

        with(inOrder(loadingSpinner, annotatedVerseListView)) {
            // loading
            verify(loadingSpinner, times(1)).fadeIn()
            verify(annotatedVerseListView, times(1)).visibility = View.GONE

            // success
            verify(annotatedVerseListView, times(1)).setItems(listOf(TextItem(title)))
            verify(annotatedVerseListView, times(1)).fadeIn()
            verify(loadingSpinner, times(1)).visibility = View.GONE
        }
    }

    @Test
    fun testLoadAnnotatedVersesWithException() = testDispatcher.runBlockingTest {
        val currentTranslation = MockContents.kjvShortName
        val sortOrder = Constants.SORT_BY_DATE
        `when`(verseAnnotationViewModel.loadingRequest()).thenReturn(flowOf(LoadingRequest(currentTranslation, sortOrder)))
        `when`(verseAnnotationViewModel.annotatedVerses(LoadingRequest(currentTranslation, sortOrder))).thenReturn(flow { throw RuntimeException() })

        baseAnnotatedVersesPresenter.onCreate()
        verify(loadingSpinner, times(1)).visibility = View.GONE
    }

    @Test
    fun testToEmptyItems() {
        val title = "random title"
        `when`(activity.getString(noItemText)).thenReturn(title)
        val annotatedVerses = AnnotatedVerses<TestVerseAnnotation>(
                Constants.DEFAULT_SORT_ORDER, emptyList(), MockContents.kjvBookNames, MockContents.kjvBookShortNames
        )
        assertEquals(
                listOf(TextItem(title)),
                with(baseAnnotatedVersesPresenter) { annotatedVerses.toItems() }
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testToItemsWithIllegalSortOrder() {
        val annotatedVerses = AnnotatedVerses(
                -1,
                listOf(TestVerseAnnotation(VerseIndex(0, 0, 0), 0L) to MockContents.kjvVerses[0]),
                MockContents.kjvBookNames,
                MockContents.kjvBookShortNames
        )
        with(baseAnnotatedVersesPresenter) { annotatedVerses.toItems() }
    }

    @Test
    fun testToItemsByDate() {
        val expected = listOf(
                TitleItem("", false),
                TestVerseItem(VerseIndex(0, 0, 4), MockContents.kjvBookNames[0], MockContents.kjvBookShortNames[0], Constants.SORT_BY_DATE),
                TitleItem("", false),
                TestVerseItem(VerseIndex(0, 0, 1), MockContents.kjvBookNames[0], MockContents.kjvBookShortNames[0], Constants.SORT_BY_DATE),
                TestVerseItem(VerseIndex(0, 0, 3), MockContents.kjvBookNames[0], MockContents.kjvBookShortNames[0], Constants.SORT_BY_DATE),
                TitleItem("", false),
                TestVerseItem(VerseIndex(0, 0, 2), MockContents.kjvBookNames[0], MockContents.kjvBookShortNames[0], Constants.SORT_BY_DATE)
        )

        val annotatedVerses = AnnotatedVerses(
                Constants.SORT_BY_DATE,
                listOf(
                        TestVerseAnnotation(VerseIndex(0, 0, 4), 2L * 365L * 24L * 3600L * 1000L) to MockContents.kjvVerses[4],
                        TestVerseAnnotation(VerseIndex(0, 0, 1), 36L * 3600L * 1000L) to MockContents.kjvVerses[1],
                        TestVerseAnnotation(VerseIndex(0, 0, 3), 36L * 3600L * 1000L - 1000L) to MockContents.kjvVerses[3],
                        TestVerseAnnotation(VerseIndex(0, 0, 2), 0L) to MockContents.kjvVerses[2]
                ),
                MockContents.kjvBookNames,
                MockContents.kjvBookShortNames
        )
        `when`(resources.getString(anyInt(), anyString(), anyInt())).thenReturn("")
        `when`(resources.getString(anyInt(), anyString(), anyInt(), anyInt())).thenReturn("")
        `when`(resources.getStringArray(anyInt())).thenReturn(Array(12) { "" })
        val actual = with(baseAnnotatedVersesPresenter) { annotatedVerses.toItems() }

        assertEquals(expected, actual)
    }

    @Test
    fun testFormatDateSameYear() {
        val expected = "January 1"
        `when`(resources.getString(R.string.text_date_without_year, "0", 1)).thenReturn(expected)
        `when`(resources.getStringArray(R.array.text_months)).thenReturn(Array(12) { it.toString() })

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_YEAR, 1)
        currentTimeMillis = System.currentTimeMillis()

        val actual = with(baseAnnotatedVersesPresenter) { calendar.timeInMillis.formatDate(calendar) }
        assertEquals(expected, actual)
        verify(resources, times(1)).getString(R.string.text_date_without_year, "0", 1)
        verify(resources, times(1)).getStringArray(R.array.text_months)
    }

    @Test
    fun testFormatDateDifferentYear() {
        val expected = "January 1, 2019"
        `when`(resources.getString(R.string.text_date, "0", 1, 2019)).thenReturn(expected)
        `when`(resources.getStringArray(R.array.text_months)).thenReturn(Array(12) { it.toString() })

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, 2020)
        currentTimeMillis = System.currentTimeMillis()
        calendar.set(Calendar.DAY_OF_YEAR, 1)
        calendar.set(Calendar.YEAR, 2019)

        val actual = with(baseAnnotatedVersesPresenter) { calendar.timeInMillis.formatDate(calendar) }
        assertEquals(expected, actual)
        verify(resources, times(1)).getString(R.string.text_date, "0", 1, 2019)
        verify(resources, times(1)).getStringArray(R.array.text_months)
    }

    @Test
    fun testToItemsByBook() {
        val expected = listOf(
                TitleItem(MockContents.kjvBookNames[0], false),
                TestVerseItem(VerseIndex(0, 0, 2), MockContents.kjvBookNames[0], MockContents.kjvBookShortNames[0], Constants.SORT_BY_BOOK),
                TestVerseItem(VerseIndex(0, 0, 4), MockContents.kjvBookNames[0], MockContents.kjvBookShortNames[0], Constants.SORT_BY_BOOK)
        )

        val annotatedVerses = AnnotatedVerses(
                Constants.SORT_BY_BOOK,
                listOf(
                        TestVerseAnnotation(VerseIndex(0, 0, 2), 3L) to MockContents.kjvVerses[2],
                        TestVerseAnnotation(VerseIndex(0, 0, 4), 5L) to MockContents.kjvVerses[4]
                ),
                MockContents.kjvBookNames,
                MockContents.kjvBookShortNames
        )
        val actual = with(baseAnnotatedVersesPresenter) { annotatedVerses.toItems() }

        assertEquals(expected, actual)
    }
}
