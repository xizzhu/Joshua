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

package me.xizzhu.android.joshua.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Color
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.annotation.StyleableRes
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import kotlin.math.roundToInt

fun TextView.setText(a: TypedArray, @StyleableRes index: Int) {
    val resourceId = a.getResourceId(index, -1)
    if (resourceId != -1) {
        setText(resourceId)
    } else {
        val text = a.getString(index)
        if (!TextUtils.isEmpty(text)) {
            setText(text)
        }
    }
}

fun View.setBackground(resId: Int) {
    val out = TypedValue()
    context.theme.resolveAttribute(resId, out, true)
    setBackgroundResource(out.resourceId)
}

fun Context.getActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) {
            return context
        }
        context = context.baseContext
    }
    return null
}

@ColorInt
fun Settings.getBackgroundColor(): Int = if (nightModeOn) Color.BLACK else Color.WHITE

@ColorInt
fun Settings.getPrimaryTextColor(resources: Resources): Int =
        resources.getColor(if (nightModeOn) R.color.text_light_primary else R.color.text_dark_primary)

@ColorInt
fun Settings.getSecondaryTextColor(resources: Resources): Int =
        resources.getColor(if (nightModeOn) R.color.text_light_secondary else R.color.text_dark_secondary)

@Px
fun Settings.getBodyTextSize(resources: Resources): Int =
        (resources.getDimension(R.dimen.text_body) * fontSizeScale / 2.0F).roundToInt()

@Px
fun Settings.getCaptionTextSize(resources: Resources): Int =
        (resources.getDimension(R.dimen.text_caption) * fontSizeScale / 2.0F).roundToInt()
