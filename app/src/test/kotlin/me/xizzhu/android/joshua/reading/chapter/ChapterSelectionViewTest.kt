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

package me.xizzhu.android.joshua.reading.chapter

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.databinding.InnerChapterSelectionViewBinding
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.tests.getProperty
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ChapterSelectionViewTest : BaseUnitTest() {
    @BeforeTest
    override fun setup() {
        super.setup()
        ApplicationProvider.getApplicationContext<Context>().setTheme(R.style.AppTheme)
    }

    @Test
    fun `test OT NT selection`() {
        val chapterSelectionView = ChapterSelectionView(ApplicationProvider.getApplicationContext())
        chapterSelectionView.initialize { fail() }
        chapterSelectionView.setViewState(ChapterSelectionView.ViewState(
            currentBookIndex = 0,
            currentChapterIndex = 0,
            chapterSelectionItems = MockContents.kjvBookNames.mapIndexed { index, bookName ->
                ChapterSelectionView.ViewState.ChapterSelectionItem(bookIndex = index, bookName = bookName, chapterCount = Bible.getChapterCount(index))
            }
        ))

        val viewBinding: InnerChapterSelectionViewBinding = chapterSelectionView.getProperty("viewBinding")
        assertTrue(viewBinding.chapterSelectionOldTestament.isSelected)
        assertFalse(viewBinding.chapterSelectionNewTestament.isSelected)

        viewBinding.chapterSelectionNewTestament.performClick()
        assertFalse(viewBinding.chapterSelectionOldTestament.isSelected)
        assertTrue(viewBinding.chapterSelectionNewTestament.isSelected)
    }
}
