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

package me.xizzhu.android.joshua.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.TypedArray
import android.os.SystemClock
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.CompoundButton
import android.widget.TextView
import androidx.annotation.StyleableRes
import androidx.viewpager2.widget.ViewPager2
import me.xizzhu.android.logger.Log
import androidx.recyclerview.widget.RecyclerView

private const val ANIMATION_DURATION = 300L

val View.activity: Activity
    get() {
        var context = context
        while (context !is Activity) {
            if (context is ContextWrapper) {
                context = context.baseContext
            }
        }
        return context
    }

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

fun View.setOnSingleClickListener(listener: () -> Unit) {
    setOnClickListener(object : View.OnClickListener {
        private var lastClicked = 0L
        override fun onClick(v: View) {
            val now = SystemClock.elapsedRealtime()
            if (now - lastClicked > 500L) {
                lastClicked = now
                listener()
            }
        }
    })
}

fun CompoundButton.setOnCheckedChangeByUserListener(listener: (isChecked: Boolean) -> Unit) {
    setOnCheckedChangeListener { button, isChecked ->
        if (button.isPressed) {
            listener(isChecked)
        }
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

fun ViewPager2.makeLessSensitive() {
    try {
        val recyclerViewField = ViewPager2::class.java.getDeclaredField("mRecyclerView")
            .apply { isAccessible = true }
        val touchSlopField = RecyclerView::class.java.getDeclaredField("mTouchSlop")
            .apply { isAccessible = true }

        val recyclerView = recyclerViewField.get(this)
        val touchSlop = touchSlopField.get(recyclerView) as Int
        touchSlopField.set(recyclerView, touchSlop * 2)
    } catch (t: Throwable) {
        Log.e("ViewPager2", "Error occurred while trying to make ViewPager2 less sensitive to swipe", t)
    }
}
