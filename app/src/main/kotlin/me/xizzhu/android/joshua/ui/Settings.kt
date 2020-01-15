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

package me.xizzhu.android.joshua.ui

import android.content.res.Resources
import android.graphics.Color
import android.util.TypedValue
import android.widget.TextView
import androidx.annotation.*
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings

@ColorInt
fun Settings.getBackgroundColor(): Int = if (nightModeOn) Color.BLACK else Color.WHITE

@ColorInt
fun Settings.getPrimaryTextColor(resources: Resources): Int =
        getTextColor(resources, nightModeOn, R.color.text_light_primary, R.color.text_dark_primary)

private val textColors = mutableMapOf<Int, Int>()

@ColorInt
private fun getTextColor(resources: Resources, nightModeOn: Boolean,
                         @ColorRes nightModeColor: Int, @ColorRes dayModeColor: Int): Int {
    val key = if (nightModeOn) nightModeColor else dayModeColor
    return textColors.getOrPut(key, { resources.getColor(key) })
}

@ColorInt
fun Settings.getPrimarySelectedTextColor(resources: Resources): Int =
        getTextColor(resources, nightModeOn, R.color.text_dark_primary, R.color.text_dark_primary)

@ColorInt
fun Settings.getSecondaryTextColor(resources: Resources): Int =
        getTextColor(resources, nightModeOn, R.color.text_light_secondary, R.color.text_dark_secondary)

fun Settings.getBodyTextSize(resources: Resources): Float =
        getTextSize(resources, R.dimen.text_body) * fontSizeScale

private val textSizes = mutableMapOf<Int, Float>()

private fun getTextSize(resources: Resources, @DimenRes fontSize: Int): Float {
    return textSizes.getOrPut(fontSize, { resources.getDimension(fontSize) / 2.0F })
}

fun Settings.getCaptionTextSize(resources: Resources): Float =
        getTextSize(resources, R.dimen.text_caption) * fontSizeScale

fun TextView.updateSettingsWithPrimaryText(settings: Settings) {
    setTextColor(settings.getPrimaryTextColor(resources))
    setTextSize(TypedValue.COMPLEX_UNIT_PX, settings.getBodyTextSize(resources))
}

fun TextView.updateSettingsWithSecondaryText(settings: Settings) {
    setTextColor(settings.getSecondaryTextColor(resources))
    setTextSize(TypedValue.COMPLEX_UNIT_PX, settings.getCaptionTextSize(resources))
}
