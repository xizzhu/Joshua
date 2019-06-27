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

package me.xizzhu.android.joshua.utils

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/*
 * async with a SupervisorJob
 * See https://github.com/Kotlin/kotlinx.coroutines/issues/763 for more details.
 */
fun <T> CoroutineScope.supervisedAsync(
        context: CoroutineContext = SupervisorJob(coroutineContext[Job]),
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> T
): Deferred<T> = async(context, start, block)
