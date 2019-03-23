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

package me.xizzhu.android.joshua.settings

import me.xizzhu.android.joshua.App
import me.xizzhu.android.joshua.core.logger.Log
import me.xizzhu.android.joshua.utils.MVPPresenter

class SettingsPresenter(private val app: App) : MVPPresenter<SettingsView>() {
    companion object {
        private val TAG: String = SettingsPresenter::class.java.simpleName
    }

    override fun onViewAttached() {
        super.onViewAttached()

        loadVersion()
    }

    private fun loadVersion() {
        try {
            val version = app.packageManager.getPackageInfo(app.packageName, 0).versionName
            view?.onVersionLoaded(version)
        } catch (e: Exception) {
            Log.e(TAG, e, "Failed to load app version")
        }
    }
}
