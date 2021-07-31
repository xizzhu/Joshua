/*
 * Copyright (C) 2021 Xizhi Zhu
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

package me.xizzhu.android.joshua.tests.assertions

import android.view.View
import androidx.annotation.StringRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyBelow
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import me.xizzhu.android.joshua.tests.matchers.viewWithText
import org.hamcrest.Matcher

fun isDisplayed(matcher: Matcher<View>, text: String): ViewInteraction =
        onView(matcher).check(matches(withText(text))).check(matches(isDisplayed()))

fun isDisplayed(matcher: Matcher<View>, @StringRes textId: Int): ViewInteraction =
        onView(matcher).check(matches(withText(textId))).check(matches(isDisplayed()))

fun isDisplayed(text: String): ViewInteraction =
        viewWithText(text).check(matches(isDisplayed()))

fun isDisplayed(@StringRes textId: Int): ViewInteraction =
        viewWithText(textId).check(matches(isDisplayed()))

fun doesNotExist(@StringRes textId: Int): ViewInteraction =
        viewWithText(textId).check(doesNotExist())

fun isDialogDisplayed(@StringRes textId: Int): ViewInteraction =
        isDisplayed(textId).inRoot(isDialog())

fun isTextDisplayedBelow(text: String, matcher: Matcher<View>): ViewInteraction =
        isDisplayed(text).check(isCompletelyBelow(matcher))

fun isTextDisplayedBelow(@StringRes textId: Int, matcher: Matcher<View>): ViewInteraction =
        isDisplayed(textId).check(isCompletelyBelow(matcher))
