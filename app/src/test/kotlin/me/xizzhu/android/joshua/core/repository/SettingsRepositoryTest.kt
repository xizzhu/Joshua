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

package me.xizzhu.android.joshua.core.repository

import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.repository.local.LocalSettingsStorage
import me.xizzhu.android.joshua.tests.BaseUnitTest
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsRepositoryTest : BaseUnitTest() {
    @Mock
    private lateinit var localSettingsStorage: LocalSettingsStorage

    private lateinit var settingsRepository: SettingsRepository

    @BeforeTest
    override fun setup() {
        super.setup()
        settingsRepository = SettingsRepository(localSettingsStorage)
    }

    @Test
    fun testReadSettings() = runBlockingTest {
        val settings = Settings(false, true, 1, true)
        `when`(localSettingsStorage.readSettings()).thenReturn(settings)

        // for the first time, should read from localSettingsStorage
        assertEquals(settings, settingsRepository.readSettings())
        verify(localSettingsStorage, times(1)).readSettings()

        // for the second time, should read from memory cache
        assertEquals(settings, settingsRepository.readSettings())
        verify(localSettingsStorage, times(1)).readSettings()
    }

    @Test
    fun testSaveThenReadSettings() = runBlockingTest {
        val settings = Settings(false, true, 1, true)
        `when`(localSettingsStorage.saveSettings(settings)).thenReturn(settings)

        assertEquals(settings, settingsRepository.saveSettings(settings))

        // saving settings should have updated memory cache
        assertEquals(settings, settingsRepository.readSettings())
        verify(localSettingsStorage, never()).readSettings()
    }
}
