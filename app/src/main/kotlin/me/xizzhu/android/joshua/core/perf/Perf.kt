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

package me.xizzhu.android.joshua.core.perf

object Perf {
    private lateinit var traceFactory: (String) -> Trace

    fun initialize(traceFactory: (String) -> Trace) {
        this.traceFactory = traceFactory
    }

    fun newTrace(name: String): Trace = traceFactory(name)

    inline fun trace(name: String, block: Trace.() -> Unit) {
        val trace = newTrace(name).apply { start() }
        try {
            block(trace)
        } finally {
            trace.stop()
        }
    }
}
