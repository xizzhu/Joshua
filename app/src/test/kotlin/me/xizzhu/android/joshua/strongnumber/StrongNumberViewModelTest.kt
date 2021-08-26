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

package me.xizzhu.android.joshua.strongnumber

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.core.StrongNumber
import me.xizzhu.android.joshua.infra.BaseViewModel
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.ui.recyclerview.TextItem
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class StrongNumberViewModelTest : BaseUnitTest() {
    private lateinit var navigator: Navigator
    private lateinit var strongNumberInteractor: StrongNumberInteractor
    private lateinit var strongNumberActivity: StrongNumberActivity

    private lateinit var strongNumberViewModel: StrongNumberViewModel

    @BeforeTest
    override fun setup() {
        super.setup()

        navigator = mockk()
        strongNumberInteractor = mockk()
        strongNumberActivity = mockk()

        strongNumberViewModel = StrongNumberViewModel(navigator, strongNumberInteractor, strongNumberActivity, testCoroutineScope)
    }

    @Test
    fun `test strongNumber`() = runBlocking {
        val sn = "H7225"
        coEvery { strongNumberInteractor.strongNumber(sn) } returns StrongNumber(sn, MockContents.strongNumberWords.getValue(sn))
        coEvery { strongNumberInteractor.verses(sn) } returns listOf(MockContents.kjvVerses[0], MockContents.kjvExtraVerses[0], MockContents.kjvExtraVerses[1])
        coEvery { strongNumberInteractor.bookNames() } returns MockContents.kjvBookNames
        coEvery { strongNumberInteractor.bookShortNames() } returns MockContents.kjvBookShortNames

        strongNumberViewModel.loadStrongNumber(sn)

        val actual = strongNumberViewModel.strongNumber().first()
        assertTrue(actual is BaseViewModel.ViewData.Success)
        assertEquals(6, actual.data.items.size)
        assertEquals(
                "H7225 beginning, chief(-est), first(-fruits, part, time), principal thing.",
                (actual.data.items[0] as TextItem).title.toString()
        )
        assertEquals("Genesis", (actual.data.items[1] as TitleItem).title.toString())
        assertEquals(
                "Gen. 1:1 In the beginning God created the heaven and the earth.",
                (actual.data.items[2] as VerseStrongNumberItem).textForDisplay.toString()
        )
        assertEquals(
                "Gen. 10:10 And the beginning of his kingdom was Babel, and Erech, and Accad, and Calneh, in the land of Shinar.",
                (actual.data.items[3] as VerseStrongNumberItem).textForDisplay.toString()
        )
        assertEquals("Exodus", (actual.data.items[4] as TitleItem).title.toString())
        assertEquals(
                "Ex. 23:19 The first of the firstfruits of thy land thou shalt bring into the house of the LORD thy God. Thou shalt not seethe a kid in his mother’s milk.",
                (actual.data.items[5] as VerseStrongNumberItem).textForDisplay.toString()
        )
    }
}
