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

package me.xizzhu.android.joshua.tests

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.HiltAndroidRule
import org.junit.Rule

abstract class BaseActivityTest : BaseUnitTest() {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    protected inline fun <reified A : Activity> withActivity(crossinline block: (activity: A) -> Unit) {
        val startActivityIntent = Intent.makeMainActivity(ComponentName(ApplicationProvider.getApplicationContext(), A::class.java))
        ActivityScenario.launch<A>(startActivityIntent).onActivity { activity ->
            block(activity)
        }
    }
}
