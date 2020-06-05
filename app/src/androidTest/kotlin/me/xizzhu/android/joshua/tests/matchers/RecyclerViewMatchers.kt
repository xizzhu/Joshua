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

package me.xizzhu.android.joshua.tests.matchers

import android.content.res.Resources
import android.view.View
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

fun atPositionOnRecyclerView(@IdRes recyclerViewId: Int, position: Int, @IdRes viewId: Int) = object : TypeSafeMatcher<View>() {
    private var resources: Resources? = null

    override fun describeTo(description: Description) {
        val desc = resources?.let {
            try {
                it.getResourceName(recyclerViewId)
            } catch (e: Resources.NotFoundException) {
                "$recyclerViewId (resource name not found)"
            }
        } ?: recyclerViewId.toString()
        description.appendText("with ID: $desc")
    }

    override fun matchesSafely(view: View): Boolean {
        resources = view.resources

        val itemView = view.rootView
                .findViewById<RecyclerView>(recyclerViewId)
                .findViewHolderForAdapterPosition(position)
                ?.itemView
                ?: return false
        return view == if (viewId == 0) itemView else itemView.findViewById(viewId)
    }
}
