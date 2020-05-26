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

package me.xizzhu.android.joshua.end2end

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.annotation.StringRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyBelow
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.rule.ActivityTestRule
import org.hamcrest.Matcher

open class BaseRobot<T : Activity, SELF : BaseRobot<T, SELF>>(private val activityRule: ActivityTestRule<T>) {
    open fun launch(): SELF {
        activityRule.launchActivity(Intent())
        return self()
    }

    @Suppress("UNCHECKED_CAST")
    protected fun self(): SELF = this as SELF

    fun hasText(@StringRes text: Int): SELF {
        onView(withText(text)).check(matches(isDisplayed()))
        return self()
    }

    fun hasText(text: String): SELF {
        onView(withText(text)).check(matches(isDisplayed()))
        return self()
    }

    fun isTextDisplayedBelow(@StringRes text: Int, matcher: Matcher<View>): SELF {
        onView(withText(text)).check(matches(isDisplayed())).check(isCompletelyBelow(matcher))
        return self()
    }

    fun isTextDisplayedBelow(text: String, matcher: Matcher<View>): SELF {
        onView(withText(text)).check(matches(isDisplayed())).check(isCompletelyBelow(matcher))
        return self()
    }
}
