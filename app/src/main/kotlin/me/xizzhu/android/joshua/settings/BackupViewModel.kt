/*
 * Copyright (C) 2021 Xizhi Zhu
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

package me.xizzhu.android.joshua.settings

import android.net.Uri
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.infra.BaseViewModel
import me.xizzhu.android.logger.Log
import java.io.IOException

class BackupViewModel(
        backupInteractor: BackupInteractor, settingsActivity: SettingsActivity, coroutineScope: CoroutineScope = settingsActivity.lifecycleScope
) : BaseViewModel<BackupInteractor, SettingsActivity>(backupInteractor, settingsActivity, coroutineScope) {
    fun backup(uri: Uri?): Flow<ViewData<Int>> = flow {
        if (uri == null) {
            emit(ViewData.Failure(IllegalArgumentException("Null URI")))
            return@flow
        }

        emit(ViewData.Loading())
        try {
            activity.contentResolver.openOutputStream(uri)
                    ?.use { interactor.backup(it) }
                    ?: throw IOException("Failed to open Uri for backup - $uri")
            emit(ViewData.Success(R.string.toast_backed_up))
        } catch (e: Exception) {
            Log.e(tag, "Failed to backup data", e)
            emit(ViewData.Failure(e))
        }
    }.flowOn(Dispatchers.IO)

    fun restore(uri: Uri?): Flow<ViewData<Int>> = flow {
        if (uri == null) {
            emit(ViewData.Failure(IllegalArgumentException("Null URI")))
            return@flow
        }

        emit(ViewData.Loading())
        try {
            activity.contentResolver.openInputStream(uri)
                    ?.use { interactor.restore(it) }
                    ?: throw IOException("Failed to open Uri for restore - $uri")
            emit(ViewData.Success(R.string.toast_restored))
        } catch (t: Throwable) {
            when (t) {
                is Exception, is OutOfMemoryError -> {
                    // Catching OutOfMemoryError here, because there're cases when users try to
                    // open a huge file.
                    // See https://console.firebase.google.com/u/0/project/joshua-production/crashlytics/app/android:me.xizzhu.android.joshua/issues/e9339c69d6e1856856db88413614d3d3
                    Log.e(tag, "Failed to restore data", t)
                    emit(ViewData.Failure(t))
                }
                else -> throw t
            }
        }
    }.flowOn(Dispatchers.IO)
}
