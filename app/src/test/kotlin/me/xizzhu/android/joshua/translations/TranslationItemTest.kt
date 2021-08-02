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

package me.xizzhu.android.joshua.translations

import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import kotlin.test.Test
import kotlin.test.assertEquals

class TranslationItemTest : BaseUnitTest() {
    @Test
    fun testItemViewType() {
        assertEquals(R.layout.item_translation, TranslationItem(MockContents.kjvTranslationInfo, true, {}, { _, _ -> }).viewType)
    }

    @Test
    fun testRightDrawable() {
        assertEquals(0, TranslationItem(MockContents.kjvTranslationInfo, false, {}, { _, _ -> }).rightDrawable)
        assertEquals(R.drawable.ic_check, TranslationItem(MockContents.kjvTranslationInfo, true, {}, { _, _ -> }).rightDrawable)
    }
}
