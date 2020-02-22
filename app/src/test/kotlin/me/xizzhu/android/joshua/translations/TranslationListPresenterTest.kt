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

package me.xizzhu.android.joshua.translations

import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import org.mockito.Mock
import org.mockito.Mockito.`when`
import java.util.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TranslationListPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var translationsViewModel: TranslationsViewModel
    @Mock
    private lateinit var translationsActivity: TranslationsActivity

    private lateinit var translationListPresenter: TranslationListPresenter

    @BeforeTest
    override fun setup() {
        super.setup()

        translationListPresenter = TranslationListPresenter(translationsViewModel, translationsActivity, testCoroutineScope)
    }

    @Test
    fun testToItems() {
        val expected = listOf(
                TitleItem(Locale("en").displayName, true),
                TranslationItem(MockContents.kjvDownloadedTranslationInfo, true, translationListPresenter::onTranslationClicked, translationListPresenter::onTranslationLongClicked),
                TitleItem(Locale("zh").displayName, true),
                TranslationItem(MockContents.cuvDownloadedTranslationInfo, false, translationListPresenter::onTranslationClicked, translationListPresenter::onTranslationLongClicked),
                TitleItem("AVAILABLE", false),
                TitleItem(Locale("en").displayName, true),
                TranslationItem(MockContents.bbeTranslationInfo, false, translationListPresenter::onTranslationClicked, translationListPresenter::onTranslationLongClicked),
                TranslationItem(MockContents.msgTranslationInfo, false, translationListPresenter::onTranslationClicked, translationListPresenter::onTranslationLongClicked)
        )

        `when`(translationsActivity.getString(R.string.header_available_translations)).thenReturn("AVAILABLE")
        val translationList = TranslationList(
                MockContents.kjvShortName,
                listOf(MockContents.bbeTranslationInfo, MockContents.msgTranslationInfo),
                listOf(MockContents.kjvDownloadedTranslationInfo, MockContents.cuvDownloadedTranslationInfo)
        )
        val actual = with(translationListPresenter) { translationList.toItems() }

        assertEquals(expected, actual)
    }
}
