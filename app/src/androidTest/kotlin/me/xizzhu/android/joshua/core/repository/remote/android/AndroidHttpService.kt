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

package me.xizzhu.android.joshua.core.repository.remote.android

import android.util.JsonReader
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.StringReader
import java.util.zip.ZipInputStream

var inputStream: InputStream? = null

fun getInputStream(relativeUrl: String): InputStream = inputStream!!

fun ZipInputStream.forEachIndexed(action: (index: Int, entryName: String, contentReader: JsonReader) -> Unit) = use {
    var index = 0
    val buffer = ByteArray(4096)
    val os = ByteArrayOutputStream()
    while (true) {
        val entryName = nextEntry?.name ?: break

        os.reset()
        while (true) {
            val byteCount = read(buffer)
            if (byteCount < 0) break
            os.write(buffer, 0, byteCount)
        }

        action(index++, entryName, JsonReader(StringReader(os.toString("UTF-8"))))
    }
}
