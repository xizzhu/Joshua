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

package me.xizzhu.android.joshua.core.analytics

interface AnalyticsProvider {
    fun track(event: String, params: Map<String, Any>?)
}

object Analytics {
    const val EVENT_DOWNLOAD_TRANSLATION: String = "download_translation"
    const val EVENT_DOWNLOAD_STRONG_NUMBER: String = "download_strong_number"

    const val PARAM_ITEM_ID: String = "item_id"
    const val PARAM_DOWNLOAD_TIME: String = "download_time"
    const val PARAM_INSTALL_TIME: String = "install_time"

    private val providers: ArrayList<AnalyticsProvider> = ArrayList()

    fun addProvider(provider: AnalyticsProvider) {
        if (!providers.contains(provider)) {
            providers.add(provider)
        }
    }

    fun removeProvider(provider: AnalyticsProvider) {
        providers.remove(provider)
    }

    fun track(event: String, params: Map<String, Any>? = null) {
        for (provider in providers) {
            provider.track(event, params)
        }
    }
}
