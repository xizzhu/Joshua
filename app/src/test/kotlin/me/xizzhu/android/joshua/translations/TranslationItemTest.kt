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

package me.xizzhu.android.joshua.translations

import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import me.xizzhu.android.joshua.core.Settings
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TranslationItemTest : BaseUnitTest() {
    @Test
    fun `test DiffCallback`() {
        val diffCallback = TranslationItem.DiffCallback()

        assertTrue(diffCallback.areItemsTheSame(
            TranslationItem.Header(Settings.DEFAULT, "", false),
            TranslationItem.Header(Settings.DEFAULT, "", false)
        ))
        assertTrue(diffCallback.areItemsTheSame(
            TranslationItem.Header(Settings.DEFAULT, "", false),
            TranslationItem.Header(Settings.DEFAULT, "other", false)
        ))
        assertTrue(diffCallback.areContentsTheSame(
            TranslationItem.Header(Settings.DEFAULT, "", false),
            TranslationItem.Header(Settings.DEFAULT, "", false)
        ))
        assertFalse(diffCallback.areContentsTheSame(
            TranslationItem.Header(Settings.DEFAULT, "", false),
            TranslationItem.Header(Settings.DEFAULT, "other", false)
        ))

        assertTrue(diffCallback.areItemsTheSame(
            TranslationItem.Translation(Settings.DEFAULT, MockContents.kjvTranslationInfo, true),
            TranslationItem.Translation(Settings.DEFAULT, MockContents.kjvTranslationInfo, true)
        ))
        assertTrue(diffCallback.areItemsTheSame(
            TranslationItem.Translation(Settings.DEFAULT, MockContents.kjvTranslationInfo, true),
            TranslationItem.Translation(Settings.DEFAULT, MockContents.kjvTranslationInfo, false)
        ))
        assertTrue(diffCallback.areContentsTheSame(
            TranslationItem.Translation(Settings.DEFAULT, MockContents.kjvTranslationInfo, true),
            TranslationItem.Translation(Settings.DEFAULT, MockContents.kjvTranslationInfo, true)
        ))
        assertFalse(diffCallback.areContentsTheSame(
            TranslationItem.Translation(Settings.DEFAULT, MockContents.kjvTranslationInfo, true),
            TranslationItem.Translation(Settings.DEFAULT, MockContents.kjvTranslationInfo, false)
        ))

        assertFalse(diffCallback.areItemsTheSame(
            TranslationItem.Header(Settings.DEFAULT, "", false),
            TranslationItem.Translation(Settings.DEFAULT, MockContents.kjvTranslationInfo, true)
        ))
        assertFalse(diffCallback.areItemsTheSame(
            TranslationItem.Translation(Settings.DEFAULT, MockContents.kjvTranslationInfo, true),
            TranslationItem.Header(Settings.DEFAULT, "", false)
        ))
    }

    @Test
    fun `test viewType()`() {
        assertEquals(R.layout.item_title, TranslationItem.Header(Settings.DEFAULT, "", false).viewType)
        assertEquals(R.layout.item_translation, TranslationItem.Translation(Settings.DEFAULT, MockContents.kjvTranslationInfo, true).viewType)
    }
}
