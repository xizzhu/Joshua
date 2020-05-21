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

package me.xizzhu.android.joshua.core.logger.android

import androidx.annotation.VisibleForTesting
import com.google.firebase.crashlytics.FirebaseCrashlytics
import me.xizzhu.android.logger.Log
import me.xizzhu.android.logger.Logger

class CrashlyticsLogger : Logger {
    private val crashlytics by lazy { FirebaseCrashlytics.getInstance() }

    override fun log(@Log.Level level: Int, tag: String, msg: String) {
        if (level >= Log.INFO && msg.isNotBlank()) {
            crashlytics.log("$tag: $msg")
        }
    }

    override fun log(@Log.Level level: Int, tag: String, msg: String, e: Throwable) {
        if (e.isCoroutineCancellationException()) return

        if (msg.isNotBlank()) {
            crashlytics.log("$tag: $msg")
        }
        crashlytics.recordException(e)
    }

    @VisibleForTesting
    fun Throwable.isCoroutineCancellationException(): Boolean {
        var rootCause = this
        while (rootCause.cause?.let { it != rootCause } == true) rootCause = rootCause.cause!!
        val name = rootCause.javaClass.name
        return name == "kotlinx.coroutines.JobCancellationException"
                || name == "kotlinx.coroutines.flow.internal.ChildCancelledException"
    }
}
