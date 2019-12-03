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

package me.xizzhu.android.joshua.core.logger.android

import androidx.annotation.VisibleForTesting
import com.crashlytics.android.Crashlytics
import kotlinx.coroutines.CancellationException
import me.xizzhu.android.logger.Log
import me.xizzhu.android.logger.Logger

class CrashlyticsLogger : Logger {
    private val crashlyticsCore by lazy { Crashlytics.getInstance().core }

    override fun log(@Log.Level level: Int, tag: String, msg: String) {
        if (level >= Log.INFO && msg.isNotBlank()) {
            crashlyticsCore.log(level, tag, msg)
        }
    }

    override fun log(@Log.Level level: Int, tag: String, msg: String, e: Throwable) {
        if (e.isCoroutineCancellationException()) return

        if (msg.isNotBlank()) {
            crashlyticsCore.log(level, tag, msg)
        }
        crashlyticsCore.logException(e)
    }

    @VisibleForTesting
    fun Throwable.isCoroutineCancellationException(): Boolean {
        var rootCause = this
        while (rootCause.cause?.let { it != rootCause } == true) rootCause = rootCause.cause!!
        if (rootCause is CancellationException && rootCause.message == "Job was cancelled") return true

        return false
    }
}
