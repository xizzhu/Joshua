/*
 * Copyright (C) 2021 Xizhi Zhu
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

import androidx.lifecycle.Lifecycle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.reading.ReadingActivity
import me.xizzhu.android.joshua.reading.ReadingViewModel
import me.xizzhu.android.joshua.tests.BaseUnitTest
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.*

class SearchButtonPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var lifecycle: Lifecycle

    @Mock
    private lateinit var navigator: Navigator

    @Mock
    private lateinit var readingViewModel: ReadingViewModel

    @Mock
    private lateinit var readingActivity: ReadingActivity

    @Mock
    private lateinit var searchButton: FloatingActionButton

    private lateinit var searchButtonViewHolder: SearchButtonViewHolder
    private lateinit var searchButtonPresenter: SearchButtonPresenter

    @BeforeTest
    override fun setup() {
        super.setup()

        `when`(readingActivity.lifecycle).thenReturn(lifecycle)

        searchButtonViewHolder = SearchButtonViewHolder(searchButton)
        searchButtonPresenter = SearchButtonPresenter(navigator, readingViewModel, readingActivity, testCoroutineScope)
        searchButtonPresenter.bind(searchButtonViewHolder)
    }

    @Test
    fun testObserveSettings() = runBlocking {
        `when`(readingViewModel.settings()).thenReturn(flowOf(
                Settings.DEFAULT,
                Settings.DEFAULT.copy(hideSearchButton = true),
                Settings.DEFAULT
        ))

        searchButtonPresenter.observeSettings()

        with(inOrder(searchButton)) {
            verify(searchButton, times(1)).show()
            verify(searchButton, times(1)).hide()
            verify(searchButton, times(1)).show()
        }
    }
}
