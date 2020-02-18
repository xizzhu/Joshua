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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.ui.recyclerview.TextItem
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BaseAnnotatedVersesPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var resources: Resources
    @Mock
    private lateinit var activity: BaseAnnotatedVersesActivity<TestVerseAnnotation>
    @Mock
    private lateinit var navigator: Navigator
    private val noItemText = R.string.text_no_bookmark
    @Mock
    private lateinit var verseAnnotationViewModel: TestVerseAnnotationViewModel
    @Mock
    private lateinit var lifecycle: Lifecycle
    @Mock
    private lateinit var lifecycleCoroutineScope: LifecycleCoroutineScope

    private lateinit var baseAnnotatedVersesPresenter: BaseAnnotatedVersesPresenter<TestVerseAnnotation>

    @BeforeTest
    override fun setup() {
        super.setup()

        `when`(activity.resources).thenReturn(resources)

        baseAnnotatedVersesPresenter = TestVerseAnnotationPresenter(activity, navigator, noItemText, verseAnnotationViewModel, lifecycle, lifecycleCoroutineScope)
    }

    @Test
    fun testToEmptyItems() {
        val title = "random title"
        `when`(activity.getString(noItemText)).thenReturn(title)
        val annotatedVerses = AnnotatedVersesViewData<TestVerseAnnotation>(emptyList(), MockContents.kjvBookNames, MockContents.kjvBookShortNames)
        assertEquals(
                listOf(TextItem(title)),
                with(baseAnnotatedVersesPresenter) { annotatedVerses.toItems(Constants.DEFAULT_SORT_ORDER) }
        )
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

        val sortOrder = Constants.SORT_BY_DATE
        val annotatedVerses = AnnotatedVersesViewData(
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
        val actual = with(baseAnnotatedVersesPresenter) { annotatedVerses.toItems(sortOrder) }

        assertEquals(expected, actual)
    }

    @Test
    fun testToItemsByBook() {
        val expected = listOf(
                TitleItem(MockContents.kjvBookNames[0], false),
                TestVerseItem(VerseIndex(0, 0, 2), MockContents.kjvBookNames[0], MockContents.kjvBookShortNames[0], Constants.SORT_BY_BOOK),
                TestVerseItem(VerseIndex(0, 0, 4), MockContents.kjvBookNames[0], MockContents.kjvBookShortNames[0], Constants.SORT_BY_BOOK)
        )

        val sortOrder = Constants.SORT_BY_BOOK
        val annotatedVerses = AnnotatedVersesViewData(
                listOf(
                        TestVerseAnnotation(VerseIndex(0, 0, 2), 3L) to MockContents.kjvVerses[2],
                        TestVerseAnnotation(VerseIndex(0, 0, 4), 5L) to MockContents.kjvVerses[4]
                ),
                MockContents.kjvBookNames,
                MockContents.kjvBookShortNames
        )
        val actual = with(baseAnnotatedVersesPresenter) { annotatedVerses.toItems(sortOrder) }

        assertEquals(expected, actual)
    }
}
