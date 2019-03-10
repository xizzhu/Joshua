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

import me.xizzhu.android.joshua.core.logger.Log
import me.xizzhu.android.joshua.core.logger.Logger

class LogcatLogger : Logger {
    override fun log(@Log.Level level: Int, tag: String, msg: String) {
        when (level) {
            Log.VERBOSE -> android.util.Log.v(tag, msg)
            Log.DEBUG -> android.util.Log.d(tag, msg)
            Log.INFO -> android.util.Log.i(tag, msg)
            Log.WARN -> android.util.Log.w(tag, msg)
            Log.ERROR -> android.util.Log.e(tag, msg)
            Log.FATAL -> android.util.Log.wtf(tag, msg)
        }
    }

    override fun log(level: Int, tag: String, e: Throwable, msg: String) {
        when (level) {
            Log.VERBOSE -> android.util.Log.v(tag, msg, e)
            Log.DEBUG -> android.util.Log.d(tag, msg, e)
            Log.INFO -> android.util.Log.i(tag, msg, e)
            Log.WARN -> android.util.Log.w(tag, msg, e)
            Log.ERROR -> android.util.Log.e(tag, msg, e)
            Log.FATAL -> android.util.Log.wtf(tag, msg, e)
        }
    }
}
