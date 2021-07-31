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

package me.xizzhu.android.joshua.tests

import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import me.xizzhu.android.joshua.ui.ProgressDialog

fun ProgressDialog?.waitUntilDismissed() {
    this?.let { ProgressDialogIdlingResource(it) }
}

class ProgressDialogIdlingResource(private val dialog: ProgressDialog) : IdlingResource {
    init {
        IdlingRegistry.getInstance().register(this)
    }

    private var resourceCallback: IdlingResource.ResourceCallback? = null

    override fun getName(): String = ProgressDialogIdlingResource::class.java.name

    override fun isIdleNow(): Boolean {
        val idle = !dialog.isShowing()
        if (idle) {
            resourceCallback?.onTransitionToIdle()
            IdlingRegistry.getInstance().unregister(this)
        }
        return idle
    }

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
        this.resourceCallback = callback
    }
}
