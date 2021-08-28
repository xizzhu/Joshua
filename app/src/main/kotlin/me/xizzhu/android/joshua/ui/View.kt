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

package me.xizzhu.android.joshua.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.res.TypedArray
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.StyleableRes
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope

private const val ANIMATION_DURATION = 300L

fun View.activity(): Activity = context as Activity

fun View.lifecycleScope(): LifecycleCoroutineScope = (context as LifecycleOwner).lifecycleScope

fun View.fadeIn() {
    if (visibility == View.VISIBLE) {
        return
    }

    alpha = 0.0F
    visibility = View.VISIBLE
    animate().alpha(1.0F).setDuration(ANIMATION_DURATION).start()
}

fun View.fadeOut() {
    if (visibility == View.GONE) {
        return
    }

    alpha = 1.0F
    animate().alpha(0.0F).setDuration(ANIMATION_DURATION)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    visibility = View.GONE
                    alpha = 1.0F
                }
            })
}

fun View.setBackground(resId: Int) {
    val out = TypedValue()
    context.theme.resolveAttribute(resId, out, true)
    setBackgroundResource(out.resourceId)
}

fun View.hideKeyboard() {
    if (hasFocus()) {
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }
}

fun TextView.animateTextColor(@ColorInt to: Int) {
    val from = currentTextColor
    val argbEvaluator = ArgbEvaluator()
    with(ValueAnimator.ofFloat(0.0F, 1.0F)) {
        addUpdateListener { animator ->
            val fraction = animator.animatedValue as Float
            val color = argbEvaluator.evaluate(fraction, from, to) as Int
            setTextColor(color)
        }
        setDuration(ANIMATION_DURATION).start()
    }
}

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
