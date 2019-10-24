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

import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.CommonRecyclerView
import me.xizzhu.android.joshua.ui.recyclerview.TextItem
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BaseAnnotatedVersesPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var activity: BaseAnnotatedVersesActivity<Unit>
    @Mock
    private lateinit var navigator: Navigator
    @Mock
    private lateinit var interactor: BaseAnnotatedVersesInteractor<Unit>
    @Mock
    private lateinit var annotatedVerseListView: CommonRecyclerView

    private lateinit var annotatedVersesViewHolder: AnnotatedVersesViewHolder
    private lateinit var baseAnnotatedVersesPresenter: BaseAnnotatedVersesPresenter<Unit, BaseAnnotatedVersesInteractor<Unit>>

    @BeforeTest
    override fun setup() {
        super.setup()

        `when`(interactor.settings()).thenReturn(emptyFlow())
        `when`(interactor.sortOrder()).thenReturn(emptyFlow())

        annotatedVersesViewHolder = AnnotatedVersesViewHolder(annotatedVerseListView)
        baseAnnotatedVersesPresenter = object : BaseAnnotatedVersesPresenter<Unit, BaseAnnotatedVersesInteractor<Unit>>(activity, navigator, 0, interactor, testDispatcher) {
            override suspend fun List<Unit>.toBaseItemsByDate(): List<BaseItem> = emptyList()

            override suspend fun List<Unit>.toBaseItemsByBook(): List<BaseItem> = emptyList()
        }
        baseAnnotatedVersesPresenter.bind(annotatedVersesViewHolder)
    }

    @AfterTest
    override fun tearDown() {
        baseAnnotatedVersesPresenter.unbind()
        super.tearDown()
    }

    @Test
    fun testPrepareItemsWithEmptyBookmarks() = testDispatcher.runBlockingTest {
        val title = "random title"
        `when`(interactor.readVerseAnnotations(ArgumentMatchers.anyInt())).thenReturn(emptyList())
        `when`(activity.getString(anyInt())).thenReturn(title)

        assertEquals(listOf(TextItem(title)), baseAnnotatedVersesPresenter.prepareItems(Constants.SORT_BY_DATE))
        assertEquals(listOf(TextItem(title)), baseAnnotatedVersesPresenter.prepareItems(Constants.SORT_BY_BOOK))
    }

    @Test
    fun testObserveSettings() = testDispatcher.runBlockingTest {
        val settings = Settings(false, true, 1, true)
        `when`(interactor.settings()).thenReturn(flowOf(ViewData.loading(), ViewData.success(settings), ViewData.error()))

        baseAnnotatedVersesPresenter.start()
        verify(annotatedVerseListView, times(1)).setSettings(settings)
        verify(annotatedVerseListView, never()).setSettings(Settings.DEFAULT)

        baseAnnotatedVersesPresenter.stop()
    }
}
