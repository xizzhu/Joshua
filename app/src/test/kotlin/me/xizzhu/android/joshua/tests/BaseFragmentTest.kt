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

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.HiltAndroidRule
import me.xizzhu.android.joshua.R
import org.junit.Rule

abstract class BaseFragmentTest : BaseUnitTest() {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    protected inline fun <reified F : Fragment, reified A: AppCompatActivity> withFragment(fragmentArgs: Bundle? = null, crossinline block: (fragment: F) -> Unit) {
        val startActivityIntent = Intent.makeMainActivity(ComponentName(ApplicationProvider.getApplicationContext(), A::class.java))
            .putExtra("androidx.fragment.app.testing.FragmentScenario.EmptyFragmentActivity.THEME_EXTRAS_BUNDLE_KEY", R.style.AppTheme)

        ActivityScenario.launch<A>(startActivityIntent).onActivity { activity ->
            val fragment: F = activity.supportFragmentManager.fragmentFactory.instantiate(F::class.java.classLoader!!, F::class.java.name) as F
            fragment.arguments = fragmentArgs

            activity.supportFragmentManager
                .beginTransaction()
                .add(android.R.id.content, fragment, "")
                .commitNow()

            block(fragment)
        }
    }
}
