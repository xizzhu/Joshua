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

package me.xizzhu.android.joshua.core.analytics.android

import android.content.Context
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import com.google.firebase.analytics.FirebaseAnalytics
import me.xizzhu.android.joshua.core.analytics.AnalyticsProvider

class FirebaseAnalyticsProvider(context: Context) : AnalyticsProvider {
    private val firebaseAnalytics: FirebaseAnalytics by lazy { FirebaseAnalytics.getInstance(context) }

    override fun track(event: String, params: Map<String, Any>?) {
        firebaseAnalytics.logEvent(event, params?.toBundle())
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun Map<String, Any>.toBundle(): Bundle = Bundle().also { bundle ->
        forEach { (key, value) ->
            when (value) {
                is Long -> bundle.putLong(key, value)
                is String -> bundle.putString(key, value)
                else -> throw IllegalArgumentException("Unsupported param type, key - $key, value - $value")
            }
        }
    }
}
