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

package me.xizzhu.android.joshua.tests.action

import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import me.xizzhu.android.joshua.tests.assertions.isDialogDisplayed
import me.xizzhu.android.joshua.tests.matchers.onView
import me.xizzhu.android.joshua.tests.matchers.viewWithText

fun click(@IdRes viewId: Int): ViewInteraction = onView(viewId).perform(click())

fun clickDialogPositiveButton(): ViewInteraction = isDialogDisplayed(android.R.string.yes).perform(click())

fun clickDialogNegativeButton(): ViewInteraction = isDialogDisplayed(android.R.string.no).perform(click())

fun clickText(text: String): ViewInteraction = viewWithText(text).perform(click())

fun clickText(@StringRes textId: Int): ViewInteraction = viewWithText(textId).perform(click())

fun longClickText(text: String): ViewInteraction = viewWithText(text).perform(longClick())
