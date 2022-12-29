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
class TranslationsItemTest : BaseUnitTest() {
    @Test
    fun `test DiffCallback`() {
        val diffCallback = TranslationsItem.DiffCallback()

        assertTrue(diffCallback.areItemsTheSame(
            TranslationsItem.Header(Settings.DEFAULT, "", false),
            TranslationsItem.Header(Settings.DEFAULT, "", false)
        ))
        assertTrue(diffCallback.areItemsTheSame(
            TranslationsItem.Header(Settings.DEFAULT, "", false),
            TranslationsItem.Header(Settings.DEFAULT, "other", false)
        ))
        assertTrue(diffCallback.areContentsTheSame(
            TranslationsItem.Header(Settings.DEFAULT, "", false),
            TranslationsItem.Header(Settings.DEFAULT, "", false)
        ))
        assertFalse(diffCallback.areContentsTheSame(
            TranslationsItem.Header(Settings.DEFAULT, "", false),
            TranslationsItem.Header(Settings.DEFAULT, "other", false)
        ))

        assertTrue(diffCallback.areItemsTheSame(
            TranslationsItem.Translation(Settings.DEFAULT, MockContents.kjvTranslationInfo, true),
            TranslationsItem.Translation(Settings.DEFAULT, MockContents.kjvTranslationInfo, true)
        ))
        assertTrue(diffCallback.areItemsTheSame(
            TranslationsItem.Translation(Settings.DEFAULT, MockContents.kjvTranslationInfo, true),
            TranslationsItem.Translation(Settings.DEFAULT, MockContents.kjvTranslationInfo, false)
        ))
        assertTrue(diffCallback.areContentsTheSame(
            TranslationsItem.Translation(Settings.DEFAULT, MockContents.kjvTranslationInfo, true),
            TranslationsItem.Translation(Settings.DEFAULT, MockContents.kjvTranslationInfo, true)
        ))
        assertFalse(diffCallback.areContentsTheSame(
            TranslationsItem.Translation(Settings.DEFAULT, MockContents.kjvTranslationInfo, true),
            TranslationsItem.Translation(Settings.DEFAULT, MockContents.kjvTranslationInfo, false)
        ))

        assertFalse(diffCallback.areItemsTheSame(
            TranslationsItem.Header(Settings.DEFAULT, "", false),
            TranslationsItem.Translation(Settings.DEFAULT, MockContents.kjvTranslationInfo, true)
        ))
        assertFalse(diffCallback.areItemsTheSame(
            TranslationsItem.Translation(Settings.DEFAULT, MockContents.kjvTranslationInfo, true),
            TranslationsItem.Header(Settings.DEFAULT, "", false)
        ))
    }

    @Test
    fun `test viewType()`() {
        assertEquals(R.layout.item_title, TranslationsItem.Header(Settings.DEFAULT, "", false).viewType)
        assertEquals(R.layout.item_translation, TranslationsItem.Translation(Settings.DEFAULT, MockContents.kjvTranslationInfo, true).viewType)
    }
}
