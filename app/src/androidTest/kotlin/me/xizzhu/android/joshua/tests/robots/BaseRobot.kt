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

package me.xizzhu.android.joshua.tests.robots

import android.app.Activity
import android.view.View
import androidx.annotation.StringRes
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyBelow
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.Matcher

open class BaseRobot<ACTIVITY : Activity, PREV, SELF : BaseRobot<ACTIVITY, PREV, SELF>>(
        protected val activity: ACTIVITY, private val previous: PREV
) {
    fun goBack(): PREV {
        pressBack()
        return previous
    }

    fun pressBack(): SELF {
        Espresso.pressBack()
        return self()
    }

    @Suppress("UNCHECKED_CAST")
    protected fun self(): SELF = this as SELF

    fun clickDialogPositiveButton(): SELF {
        onView(withText(android.R.string.yes)).inRoot(isDialog()).perform(click())
        return self()
    }

    fun clickDialogNegativeButton(): SELF {
        onView(withText(android.R.string.no)).inRoot(isDialog()).perform(click())
        return self()
    }

    fun dialogNotExist(@StringRes title: Int): SELF {
        onView(withText(title)).check(doesNotExist())
        return self()
    }

    fun isDialogDisplayed(@StringRes title: Int): SELF {
        onView(withText(title)).inRoot(isDialog())
        return self()
    }

    fun clickText(text: String): SELF {
        onView(withText(text)).perform(click())
        return self()
    }

    fun longClickText(text: String): SELF {
        onView(withText(text)).perform(longClick())
        return self()
    }

    fun isTextDisplayed(@StringRes text: Int): SELF {
        onView(withText(text)).check(matches(isDisplayed()))
        return self()
    }

    fun isTextDisplayed(text: String): SELF {
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
