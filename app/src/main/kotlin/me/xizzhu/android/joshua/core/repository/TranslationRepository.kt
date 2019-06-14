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

package me.xizzhu.android.joshua.core.repository

import android.os.Bundle
import android.os.SystemClock
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.channels.SendChannel
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.core.analytics.Analytics
import me.xizzhu.android.joshua.core.repository.local.LocalTranslationStorage
import me.xizzhu.android.joshua.core.repository.remote.RemoteTranslationInfo
import me.xizzhu.android.joshua.core.repository.remote.RemoteTranslationService
import me.xizzhu.android.logger.Log

class TranslationRepository(private val localTranslationStorage: LocalTranslationStorage,
                            private val remoteTranslationService: RemoteTranslationService) {
    companion object {
        private val TAG: String = TranslationRepository::class.java.simpleName
        private const val TRANSLATION_LIST_REFRESH_INTERVAL_IN_MILLIS = 7L * 24L * 3600L * 1000L // 7 day
    }

    suspend fun reload(forceRefresh: Boolean): List<TranslationInfo> {
        val translations = if (forceRefresh || translationListTooOld()) {
            try {
                readTranslationsFromBackend()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to read translation list from backend", e)
                readTranslationsFromLocal()
            }
        } else {
            readTranslationsFromLocal().ifEmpty {
                Log.w(TAG, "Have fresh but empty local translation list")
                readTranslationsFromBackend()
            }
        }
        if (translations.isEmpty()) {
            throw IllegalStateException("Empty translation list")
        }
        return translations
    }

    @VisibleForTesting
    suspend fun translationListTooOld(): Boolean =
            System.currentTimeMillis() - localTranslationStorage.readTranslationListRefreshTimestamp() >=
                    TRANSLATION_LIST_REFRESH_INTERVAL_IN_MILLIS

    @VisibleForTesting
    suspend fun readTranslationsFromBackend(): List<TranslationInfo> {
        val fetchedTranslations = remoteTranslationService.fetchTranslations()
        val localTranslations = readTranslationsFromLocal()

        val translations = ArrayList<TranslationInfo>(fetchedTranslations.size)
        for (fetched in fetchedTranslations) {
            var downloaded = false
            for (local in localTranslations) {
                if (fetched.shortName == local.shortName) {
                    downloaded = local.downloaded
                    break
                }
            }
            translations.add(fetched.toTranslationInfo(downloaded))
        }

        localTranslationStorage.replaceTranslations(translations)

        try {
            if (translations.isNotEmpty()) {
                localTranslationStorage.saveTranslationListRefreshTimestamp(
                        System.currentTimeMillis())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save translation list refresh timestamp", e)
        }

        return translations
    }

    suspend fun readTranslationsFromLocal(): List<TranslationInfo> = localTranslationStorage.readTranslations()

    suspend fun downloadTranslation(channel: SendChannel<Int>, translationInfo: TranslationInfo) {
        val start = elapsedRealtime()
        val translation = remoteTranslationService.fetchTranslation(
                channel, RemoteTranslationInfo.fromTranslationInfo(translationInfo))
        channel.send(100)
        val downloadFinished = elapsedRealtime()

        localTranslationStorage.saveTranslation(translation.translationInfo.toTranslationInfo(true),
                translation.bookNames, translation.bookShortNames, translation.verses)
        val installFinished = elapsedRealtime()

        Analytics.track(Analytics.EVENT_DOWNLOAD_TRANSLATION,
                buildParams(translationInfo, start, downloadFinished, installFinished))
    }

    @VisibleForTesting
    fun elapsedRealtime(): Long = SystemClock.elapsedRealtime()

    @VisibleForTesting
    fun buildParams(translationInfo: TranslationInfo, start: Long,
                    downloadFinished: Long, installFinished: Long): Bundle = Bundle().apply {
        putString(Analytics.PARAM_ITEM_ID, translationInfo.shortName)
        putLong(Analytics.PARAM_DOWNLOAD_TIME, downloadFinished - start)
        putLong(Analytics.PARAM_INSTALL_TIME, installFinished - downloadFinished)
    }

    suspend fun removeTranslation(translationInfo: TranslationInfo) {
        localTranslationStorage.removeTranslation(translationInfo)
    }
}
