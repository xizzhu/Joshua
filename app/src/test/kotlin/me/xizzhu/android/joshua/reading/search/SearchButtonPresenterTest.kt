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

package me.xizzhu.android.joshua.reading.search

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.reading.ReadingActivity
import me.xizzhu.android.joshua.tests.BaseUnitTest
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.BeforeTest
import kotlin.test.Test

class SearchButtonPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var readingActivity: ReadingActivity
    @Mock
    private lateinit var navigator: Navigator
    @Mock
    private lateinit var searchButtonInteractor: SearchButtonInteractor
    @Mock
    private lateinit var searchButton: SearchFloatingActionButton

    private lateinit var searchButtonPresenter: SearchButtonPresenter
    private lateinit var searchButtonViewHolder: SearchButtonViewHolder

    @BeforeTest
    override fun setup() {
        super.setup()

        searchButtonViewHolder = SearchButtonViewHolder(searchButton)
        searchButtonPresenter = SearchButtonPresenter(readingActivity, navigator, searchButtonInteractor, testDispatcher)
    }

    @Test
    fun testObserveSettings() = testDispatcher.runBlockingTest {
        `when`(searchButtonInteractor.settings()).thenReturn(flowOf(
                ViewData.loading(),
                ViewData.success(Settings.DEFAULT),
                ViewData.error(),
                ViewData.success(Settings.DEFAULT.copy(hideSearchButton = true)),
                ViewData.loading(),
                ViewData.success(Settings.DEFAULT)
        ))

        searchButtonPresenter.create(searchButtonViewHolder)
        with(inOrder(searchButton)) {
            verify(searchButton, times(1)).show()
            verify(searchButton, times(1)).hide()
            verify(searchButton, times(1)).show()
        }

        searchButtonPresenter.destroy()
    }
}
