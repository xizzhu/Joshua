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

import kotlinx.coroutines.channels.first
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.TranslationManager
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.ui.BaseLoadingAwareInteractor
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock

class TranslationInteractorTest : BaseUnitTest() {
    @Mock
    private lateinit var translationManagementActivity: TranslationManagementActivity
    @Mock
    private lateinit var bibleReadingManager: BibleReadingManager
    @Mock
    private lateinit var translationManager: TranslationManager
    @Mock
    private lateinit var settingsManager: SettingsManager
    private lateinit var translationInteractor: TranslationInteractor

    @Before
    override fun setup() {
        super.setup()
        translationInteractor = TranslationInteractor(translationManagementActivity,
                bibleReadingManager, translationManager, settingsManager)
    }

    @Test
    fun testInitialTranslationsLoadingState() {
        runBlocking {
            assertEquals(BaseLoadingAwareInteractor.IS_LOADING, translationInteractor.observeLoadingState().first())
        }
    }
}
