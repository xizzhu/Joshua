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

package me.xizzhu.android.joshua.settings

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.tests.EspressoTestRule
import me.xizzhu.android.joshua.tests.robots.SettingsActivityRobot
import org.junit.Rule
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
@LargeTest
class SettingsActivityTest {
    @get:Rule
    val activityRule = EspressoTestRule(SettingsActivity::class.java)

    @Test
    fun testSettings() {
        val robot = SettingsActivityRobot(activityRule.activity)
                .selectFontSize(0) // make font small to make tests less likely to fail on small devices
        var expectedSettings = Settings.DEFAULT.copy(fontSizeScale = 1)
        assertEquals(expectedSettings, SettingsManager.settings.value)

        robot.toggleKeepScreenOn()
        expectedSettings = expectedSettings.copy(keepScreenOn = false)
        assertEquals(expectedSettings, SettingsManager.settings.value)

        robot.toggleNightMode()
        expectedSettings = expectedSettings.copy(nightModeOn = true)
        assertEquals(expectedSettings, SettingsManager.settings.value)

        robot.toggleSimpleReading()
        expectedSettings = expectedSettings.copy(simpleReadingModeOn = true)
        assertEquals(expectedSettings, SettingsManager.settings.value)

        robot.toggleHideSearchButton()
        expectedSettings = expectedSettings.copy(hideSearchButton = true)
        assertEquals(expectedSettings, SettingsManager.settings.value)

        robot.toggleConsolidateVersesForSharing()
        expectedSettings = expectedSettings.copy(consolidateVersesForSharing = true)
        assertEquals(expectedSettings, SettingsManager.settings.value)

        robot.selectDefaultHighlightColor(Highlight.COLOR_PURPLE)
        expectedSettings = expectedSettings.copy(defaultHighlightColor = Highlight.COLOR_PURPLE)
        assertEquals(expectedSettings, SettingsManager.settings.value)
    }
}
