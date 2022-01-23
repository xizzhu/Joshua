/*
 * Copyright (C) 2022 Xizhi Zhu
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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import me.xizzhu.android.joshua.core.VerseIndex

suspend inline fun Flow<String>.firstNotEmpty(): String = first { it.isNotEmpty() }

fun Flow<String>.filterNotEmpty(): Flow<String> = filter { it.isNotEmpty() }

fun Flow<VerseIndex>.filterIsValid(): Flow<VerseIndex> = filter { it.isValid() }
