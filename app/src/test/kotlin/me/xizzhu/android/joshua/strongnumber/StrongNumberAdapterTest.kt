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

package me.xizzhu.android.joshua.strongnumber

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.TestExecutor
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class StrongNumberAdapterTest : BaseUnitTest() {
    private lateinit var context: Context
    private lateinit var adapter: StrongNumberAdapter

    @BeforeTest
    override fun setup() {
        super.setup()

        context = ApplicationProvider.getApplicationContext<Context>().apply { setTheme(R.style.AppTheme) }
        adapter = StrongNumberAdapter(
            inflater = LayoutInflater.from(context),
            executor = TestExecutor()
        ) {}
    }

    @Test
    fun `test getItemViewType()`() {
        adapter.submitList(
            listOf(
                StrongNumberItem.StrongNumber(Settings.DEFAULT, ""),
                StrongNumberItem.BookName(Settings.DEFAULT, ""),
                StrongNumberItem.Verse(Settings.DEFAULT, VerseIndex.INVALID, "", "")
            )
        ) {
            assertEquals(R.layout.item_text, adapter.getItemViewType(0))
            assertEquals(R.layout.item_title, adapter.getItemViewType(1))
            assertEquals(R.layout.item_strong_number_verse, adapter.getItemViewType(2))
        }
    }

    @Test(expected = IllegalStateException::class)
    fun `test onCreateViewHolder(), with unsupported viewType`() {
        adapter.onCreateViewHolder(FrameLayout(context), 0)
    }

    @Test
    fun `test onCreateViewHolder()`() {
        adapter.onCreateViewHolder(FrameLayout(context), StrongNumberItem.StrongNumber.VIEW_TYPE) as StrongNumberViewHolder.StrongNumber
        adapter.onCreateViewHolder(FrameLayout(context), StrongNumberItem.BookName.VIEW_TYPE) as StrongNumberViewHolder.BookName
        adapter.onCreateViewHolder(FrameLayout(context), StrongNumberItem.Verse.VIEW_TYPE) as StrongNumberViewHolder.Verse
    }
}
