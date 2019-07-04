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

package me.xizzhu.android.joshua.translations

import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import java.util.*
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

    @Test
    fun testToTranslationItems() {
        val onClicked: (TranslationInfo) -> Unit = {}
        val onLongClicked: (TranslationInfo, Boolean) -> Unit = { _, _ -> }
        val expected = listOf(
                TranslationItem(MockContents.kjvDownloadedTranslationInfo, true, onClicked, onLongClicked),
                TranslationItem(MockContents.bbeTranslationInfo, false, onClicked, onLongClicked),
                TitleItem(Locale("zh").displayName, true),
                TranslationItem(MockContents.cuvTranslationInfo, false, onClicked, onLongClicked)
        )
        val actual = listOf(MockContents.kjvDownloadedTranslationInfo, MockContents.bbeTranslationInfo, MockContents.cuvTranslationInfo)
                .toTranslationItems(MockContents.kjvShortName, false, onClicked, onLongClicked)
        assertEquals(expected, actual)
    }

    @Test
    fun testToTranslationItemsShowFirstLanguage() {

        val onClicked: (TranslationInfo) -> Unit = {}
        val onLongClicked: (TranslationInfo, Boolean) -> Unit = { _, _ -> }
        val expected = listOf(
                TitleItem(Locale("en").displayName, true),
                TranslationItem(MockContents.kjvDownloadedTranslationInfo, true, onClicked, onLongClicked),
                TranslationItem(MockContents.bbeTranslationInfo, false, onClicked, onLongClicked),
                TitleItem(Locale("zh").displayName, true),
                TranslationItem(MockContents.cuvTranslationInfo, false, onClicked, onLongClicked)
        )
        val actual = listOf(MockContents.kjvDownloadedTranslationInfo, MockContents.bbeTranslationInfo, MockContents.cuvTranslationInfo)
                .toTranslationItems(MockContents.kjvShortName, true, onClicked, onLongClicked)
        assertEquals(expected, actual)
    }
}
