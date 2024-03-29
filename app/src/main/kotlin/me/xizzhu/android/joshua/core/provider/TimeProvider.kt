/*
 * Copyright (C) 2023 Xizhi Zhu
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

package me.xizzhu.android.joshua.core.provider

import android.os.SystemClock
import java.util.Calendar

interface TimeProvider {
    val calendar: Calendar
    val currentTimeMillis: Long
    val elapsedRealtime: Long
}

class DefaultTimeProvider : TimeProvider {
    override val calendar: Calendar
        get() = Calendar.getInstance()

    override val currentTimeMillis: Long
        get() = System.currentTimeMillis()

    override val elapsedRealtime: Long
        get() = SystemClock.elapsedRealtime()
}
