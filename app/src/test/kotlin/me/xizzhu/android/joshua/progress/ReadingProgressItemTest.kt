/*
 * Copyright (C) 2023 Xizhi Zhu
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

package me.xizzhu.android.joshua.progress

import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.tests.BaseUnitTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReadingProgressItemTest : BaseUnitTest() {
    @Test
    fun `test DiffCallback`() {
        val diffCallback = ReadingProgressItem.DiffCallback()

        assertTrue(diffCallback.areItemsTheSame(
            ReadingProgressItem.Summary(Settings.DEFAULT, 0, 0, 0, 0, 0),
            ReadingProgressItem.Summary(Settings.DEFAULT, 0, 0, 0, 0, 0)
        ))
        assertTrue(diffCallback.areContentsTheSame(
            ReadingProgressItem.Summary(Settings.DEFAULT, 0, 0, 0, 0, 0),
            ReadingProgressItem.Summary(Settings.DEFAULT, 0, 0, 0, 0, 0)
        ))
        assertTrue(diffCallback.areItemsTheSame(
            ReadingProgressItem.Summary(Settings.DEFAULT, 0, 0, 0, 0, 0),
            ReadingProgressItem.Summary(Settings.DEFAULT, 1, 0, 0, 0, 0)
        ))
        assertFalse(diffCallback.areContentsTheSame(
            ReadingProgressItem.Summary(Settings.DEFAULT, 0, 0, 0, 0, 0),
            ReadingProgressItem.Summary(Settings.DEFAULT, 1, 0, 0, 0, 0)
        ))

        assertTrue(diffCallback.areItemsTheSame(
            ReadingProgressItem.Book(Settings.DEFAULT, "", 0, emptyList(), 0, false),
            ReadingProgressItem.Book(Settings.DEFAULT, "", 0, emptyList(), 0, false)
        ))
        assertTrue(diffCallback.areContentsTheSame(
            ReadingProgressItem.Book(Settings.DEFAULT, "", 0, emptyList(), 0, false),
            ReadingProgressItem.Book(Settings.DEFAULT, "", 0, emptyList(), 0, false)
        ))
        assertTrue(diffCallback.areItemsTheSame(
            ReadingProgressItem.Book(Settings.DEFAULT, "", 0, emptyList(), 0, false),
            ReadingProgressItem.Book(Settings.DEFAULT, "bookName", 0, emptyList(), 0, false)
        ))
        assertFalse(diffCallback.areItemsTheSame(
            ReadingProgressItem.Book(Settings.DEFAULT, "", 0, emptyList(), 0, false),
            ReadingProgressItem.Book(Settings.DEFAULT, "", 1, emptyList(), 0, false)
        ))
        assertFalse(diffCallback.areContentsTheSame(
            ReadingProgressItem.Book(Settings.DEFAULT, "", 0, emptyList(), 0, false),
            ReadingProgressItem.Book(Settings.DEFAULT, "", 1, emptyList(), 0, false)
        ))

        assertFalse(diffCallback.areItemsTheSame(
            ReadingProgressItem.Summary(Settings.DEFAULT, 0, 0, 0, 0, 0),
            ReadingProgressItem.Book(Settings.DEFAULT, "", 0, emptyList(), 0, false)
        ))
        assertFalse(diffCallback.areItemsTheSame(
            ReadingProgressItem.Book(Settings.DEFAULT, "", 0, emptyList(), 0, false),
            ReadingProgressItem.Summary(Settings.DEFAULT, 0, 0, 0, 0, 0)
        ))
        assertFalse(diffCallback.areContentsTheSame(
            ReadingProgressItem.Summary(Settings.DEFAULT, 0, 0, 0, 0, 0),
            ReadingProgressItem.Book(Settings.DEFAULT, "", 0, emptyList(), 0, false)
        ))
    }

    @Test
    fun `test viewType`() {
        assertEquals(R.layout.item_reading_progress_summary, ReadingProgressItem.Summary(Settings.DEFAULT, 0, 0, 0, 0, 0).viewType)
        assertEquals(R.layout.item_reading_progress_book, ReadingProgressItem.Book(Settings.DEFAULT, "", 0, emptyList(), 0, false).viewType)
    }
}
