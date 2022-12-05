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

package me.xizzhu.android.joshua.preview

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.tests.TestExecutor
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PreviewAdapterTest : BaseUnitTest() {
    private lateinit var context: Context
    private lateinit var adapter: PreviewAdapter

    @BeforeTest
    override fun setup() {
        super.setup()

        context = ApplicationProvider.getApplicationContext<Context>().apply { setTheme(R.style.AppTheme) }
        adapter = PreviewAdapter(
            inflater = LayoutInflater.from(context),
            executor = TestExecutor()
        ) {}
    }

    @Test
    fun `test getItemViewType()`() {
        adapter.submitList(listOf(
            PreviewItem.Verse(
                settings = Settings.DEFAULT,
                verseIndex = VerseIndex(0, 0, 0),
                verseText = MockContents.kjvVerses[0].text.text,
                followingEmptyVerseCount = 0
            ),
            PreviewItem.VerseWithQuery(
                settings = Settings.DEFAULT,
                verseIndex = VerseIndex(0, 0, 0),
                verseText = MockContents.kjvVerses[0].text.text,
                query = "query",
                followingEmptyVerseCount = 0
            )
        )) {
            assertEquals(R.layout.item_preview_verse, adapter.getItemViewType(0))
            assertEquals(R.layout.item_preview_verse_with_query, adapter.getItemViewType(1))
        }
    }

    @Test(expected = IllegalStateException::class)
    fun `test onCreateViewHolder(), with unsupported viewType`() {
        adapter.onCreateViewHolder(FrameLayout(context), 0)
    }

    @Test
    fun `test onCreateViewHolder()`() {
        adapter.onCreateViewHolder(FrameLayout(context), PreviewItem.Verse.VIEW_TYPE) as PreviewViewHolder.Verse
        adapter.onCreateViewHolder(FrameLayout(context), PreviewItem.VerseWithQuery.VIEW_TYPE) as PreviewViewHolder.VerseWithQuery
    }
}
