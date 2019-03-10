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

package me.xizzhu.android.joshua.reading

import me.xizzhu.android.joshua.tests.BaseUnitTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*

class SearchButtonPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var readingInteractor: ReadingInteractor
    @Mock
    private lateinit var searchButtonView: SearchButtonView
    private lateinit var searchButtonPresenter: SearchButtonPresenter

    @Before
    override fun setup() {
        super.setup()

        searchButtonPresenter = SearchButtonPresenter(readingInteractor)
        searchButtonPresenter.attachView(searchButtonView)
    }

    @After
    override fun tearDown() {
        searchButtonPresenter.detachView()
        super.tearDown()
    }

    @Test
    fun testOpenSearch() {
        searchButtonPresenter.openSearch()
        verify(searchButtonView, never()).onFailedToNavigateToSearch()
    }

    @Test
    fun testOpenSearchWithException() {
        `when`(readingInteractor.openSearch()).thenThrow(RuntimeException("Random exception"))

        searchButtonPresenter.openSearch()
        verify(searchButtonView, times(1)).onFailedToNavigateToSearch()
    }
}
