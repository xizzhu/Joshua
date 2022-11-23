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

package me.xizzhu.android.joshua.utils

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import me.xizzhu.android.joshua.tests.BaseUnitTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(RobolectricTestRunner::class)
class ContextTest : BaseUnitTest() {
    private lateinit var packageManager: PackageManager
    private lateinit var resolveInfoList: List<ResolveInfo>

    @BeforeTest
    override fun setup() {
        super.setup()

        packageManager = mockk()

        resolveInfoList = ArrayList<ResolveInfo>().apply {
            repeat(10) { i ->
                val activityInfo = ActivityInfo().apply {
                    name = "name$i"
                    packageName = "packageName$i"
                }

                val resolveInfo = spyk(ResolveInfo()).apply {
                    this.activityInfo = activityInfo
                    every { iconResource } returns 0
                    every { loadLabel(packageManager) } returns ""
                }
                add(resolveInfo)
            }
        }
        every { packageManager.queryIntentActivities(any(), any<Int>()) } returns resolveInfoList
    }

    @Test
    fun testCreateChooserForSharing() {
        val title = "random Title"
        val textToShare = "Random text to share"
        val packageToExclude = "packageName5"
        val chooseIntent = packageManager.chooserForSharing(packageToExclude, title, textToShare)
        assertNotNull(chooseIntent)
        assertEquals(Intent.ACTION_CHOOSER, chooseIntent.action)
        assertEquals(title, chooseIntent.getStringExtra(Intent.EXTRA_TITLE))

        val primaryIntent = chooseIntent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
        assertNotNull(primaryIntent)
        verifyShareIntent(primaryIntent, "name0", "packageName0")

        val filteredIntents = chooseIntent.getParcelableArrayExtra(Intent.EXTRA_INITIAL_INTENTS)
        assertNotNull(filteredIntents)
        assertEquals(resolveInfoList.size - 2, filteredIntents.size)
        for ((i, intent) in filteredIntents.withIndex()) {
            val index = if (i < 4) (i + 1) else (i + 2)
            verifyShareIntent(intent as Intent, "name$index", "packageName$index")
        }
    }

    private fun verifyShareIntent(intent: Intent, expectedName: String, expectedPackageName: String) {
        assertEquals(Intent.ACTION_SEND, intent.action)
        assertEquals("text/plain", intent.type)
        assertEquals(expectedName, intent.component!!.className)
        assertEquals(expectedPackageName, intent.`package`)
    }
}
