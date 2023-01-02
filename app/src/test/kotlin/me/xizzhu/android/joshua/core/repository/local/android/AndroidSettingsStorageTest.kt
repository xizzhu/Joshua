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

package me.xizzhu.android.joshua.core.repository.local.android

import kotlinx.coroutines.test.runTest
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.Settings
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class AndroidSettingsStorageTest : BaseSqliteTest() {
    private lateinit var androidSettingsStorage: AndroidSettingsStorage

    @BeforeTest
    override fun setup() {
        super.setup()
        androidSettingsStorage = AndroidSettingsStorage(androidDatabase)
    }

    @Test
    fun testReadDefault() = runTest {
        val expected = Settings.DEFAULT
        val actual = androidSettingsStorage.readSettings()
        assertEquals(expected, actual)
    }

    @Test
    fun testSaveThenRead() = runTest {
        androidSettingsStorage.saveSettings(Settings(false, Settings.NIGHT_MODE_ON, 1.25F, false, false, false, Highlight.COLOR_PINK))

        val expected = Settings(false, Settings.NIGHT_MODE_ON, 1.25F, false, false, false, Highlight.COLOR_PINK)
        val actual = androidSettingsStorage.readSettings()
        assertEquals(expected, actual)
    }

    @Test
    fun testSaveOverrideThenRead() = runTest {
        androidSettingsStorage.saveSettings(Settings(true, Settings.NIGHT_MODE_OFF, 3.0F, true, true, true, Highlight.COLOR_PINK))
        androidSettingsStorage.saveSettings(Settings(false, Settings.NIGHT_MODE_ON, 2.0F, false, false, false, Highlight.COLOR_PURPLE))

        val expected = Settings(false, Settings.NIGHT_MODE_ON, 2.0F, false, false, false, Highlight.COLOR_PURPLE)
        val actual = androidSettingsStorage.readSettings()
        assertEquals(expected, actual)
    }
}
