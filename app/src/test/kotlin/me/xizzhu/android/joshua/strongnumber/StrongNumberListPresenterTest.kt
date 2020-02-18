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

package me.xizzhu.android.joshua.strongnumber

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import me.xizzhu.android.joshua.Navigator
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

class StrongNumberListPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var strongNumberListActivity: StrongNumberListActivity
    @Mock
    private lateinit var navigator: Navigator
    @Mock
    private lateinit var strongNumberListViewModel: StrongNumberListViewModel
    @Mock
    private lateinit var lifecycle: Lifecycle
    @Mock
    private lateinit var lifecycleCoroutineScope: LifecycleCoroutineScope

    private lateinit var strongNumberListPresenter: StrongNumberListPresenter

    @BeforeTest
    override fun setup() {
        super.setup()

        strongNumberListPresenter = StrongNumberListPresenter(strongNumberListActivity, navigator, strongNumberListViewModel, lifecycle, lifecycleCoroutineScope)
    }

    @Test
    fun testToItems() {
        strongNumberListPresenter = spy(strongNumberListPresenter)
        doReturn("formatted strong number").`when`(strongNumberListPresenter).formatStrongNumber(any())

        val expected = listOf(
                TextItem("formatted strong number"),
                TitleItem(MockContents.kjvBookNames[0], false),
                VerseStrongNumberItem(VerseIndex(0, 0, 0), MockContents.kjvBookShortNames[0], MockContents.kjvVerses[0].text.text, strongNumberListPresenter::openVerse)
        )

        val strongNumber = StrongNumber("H7225", MockContents.strongNumberWords.getValue("H7225"))
        val verses = listOf(MockContents.kjvVerses[0])
        val bookNames = MockContents.kjvBookNames
        val bookShortNames = MockContents.kjvBookShortNames
        val actual = with(strongNumberListPresenter) { StrongNumberViewData(strongNumber, verses, bookNames, bookShortNames).toItems() }

        assertEquals(expected, actual)
    }
}
