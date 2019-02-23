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

package me.xizzhu.android.joshua.repository

import android.database.sqlite.SQLiteDatabase
import androidx.annotation.WorkerThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.repository.internal.BackendService
import me.xizzhu.android.joshua.repository.internal.LocalStorage
import me.xizzhu.android.joshua.repository.internal.await
import okio.buffer
import okio.source
import java.io.IOException
import java.lang.Exception
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class TranslationRepository(private val localStorage: LocalStorage, private val backendService: BackendService) {
    private val availableTranslations: ConflatedBroadcastChannel<List<TranslationInfo>> = ConflatedBroadcastChannel(emptyList())
    private val downloadedTranslations: ConflatedBroadcastChannel<List<TranslationInfo>> = ConflatedBroadcastChannel(emptyList())

    init {
        GlobalScope.launch(Dispatchers.IO) {
            val available = ArrayList<TranslationInfo>()
            val downloaded = ArrayList<TranslationInfo>()
            for (t in readTranslationsFromLocal()) {
                if (t.downloaded) {
                    downloaded.add(t)
                } else {
                    available.add(t)
                }
            }
            availableTranslations.send(available)
            downloadedTranslations.send(downloaded)
        }
    }

    fun observeAvailableTranslations(): ReceiveChannel<List<TranslationInfo>> = availableTranslations.openSubscription()

    fun observeDownloadedTranslations(): ReceiveChannel<List<TranslationInfo>> = downloadedTranslations.openSubscription()

    @WorkerThread
    suspend fun reload(forceRefresh: Boolean) {
        val available = ArrayList<TranslationInfo>()
        val downloaded = ArrayList<TranslationInfo>()
        val translations = if (forceRefresh) {
            readTranslationsFromBackend()
        } else {
            val translations = readTranslationsFromLocal()
            if (translations.isNotEmpty()) {
                translations
            } else {
                readTranslationsFromBackend()
            }
        }
        for (t in translations) {
            if (t.downloaded) {
                downloaded.add(t)
            } else {
                available.add(t)
            }
        }
        if (available != availableTranslations.value) {
            availableTranslations.send(available)
        }
        if (downloaded != downloadedTranslations.value) {
            downloadedTranslations.send(downloaded)
        }
    }

    private suspend fun readTranslationsFromBackend(): List<TranslationInfo> {
        val backendTranslations = backendService.translationService.fetchTranslationList().await().translations
        val localTranslations = readTranslationsFromLocal()

        val translations = ArrayList<TranslationInfo>(backendTranslations.size)
        for (backend in backendTranslations) {
            var downloaded = false
            for (local in localTranslations) {
                if (backend.shortName == local.shortName) {
                    downloaded = local.downloaded
                    break
                }
            }
            translations.add(TranslationInfo(backend.shortName, backend.name, backend.language, backend.size, downloaded))
        }

        localStorage.translationInfoDao.replace(translations)

        return translations
    }

    fun readTranslationsFromLocal(): List<TranslationInfo> = localStorage.translationInfoDao.read()

    suspend fun downloadTranslation(channel: SendChannel<Int>, translationInfo: TranslationInfo) {
        var inputStream: ZipInputStream? = null
        var db: SQLiteDatabase? = null
        try {
            val response = backendService.translationService.fetchTranslation(translationInfo.shortName).execute()
            if (!response.isSuccessful) {
                throw IOException("Unsupported HTTP status code - ${response.code()}")
            }

            db = localStorage.writableDatabase
            db.beginTransaction()
            localStorage.translationDao.createTable(translationInfo.shortName)

            inputStream = ZipInputStream(response.body()!!.byteStream())
            var zipEntry: ZipEntry?
            var downloaded = 0
            var progress = -1
            while (true) {
                zipEntry = inputStream.nextEntry
                if (zipEntry == null) {
                    break
                }

                val bufferedSource = inputStream.source().buffer()
                val entryName = zipEntry.name
                if (entryName == "books.json") {
                    val backendBooks = backendService.booksAdapter.fromJson(bufferedSource)
                    localStorage.bookNamesDao.save(backendBooks!!.shortName, backendBooks.books)
                } else {
                    val split = entryName.substring(0, entryName.length - 5).split("-")
                    val bookIndex = split[0].toInt()
                    val chapterIndex = split[1].toInt()
                    localStorage.translationDao.save(translationInfo.shortName, bookIndex, chapterIndex,
                            backendService.chapterAdapter.fromJson(bufferedSource)!!.verses)
                }

                // only emits if the progress is actually changed
                val currentProgress = ++downloaded / 12
                if (currentProgress > progress) {
                    progress = currentProgress
                    channel.send(progress)
                }
            }

            localStorage.translationInfoDao.save(TranslationInfo(translationInfo.shortName,
                    translationInfo.name, translationInfo.language, translationInfo.size, true))

            db.setTransactionSuccessful()
            channel.close()

            val currentAvailable = ArrayList(availableTranslations.value)
            currentAvailable.remove(translationInfo)
            availableTranslations.send(currentAvailable)

            val currentDownloaded = ArrayList(downloadedTranslations.value)
            currentDownloaded.add(TranslationInfo(translationInfo.shortName, translationInfo.name,
                    translationInfo.language, translationInfo.size, true))
            downloadedTranslations.send(currentDownloaded)
        } catch (e: Exception) {
            channel.close(e)
        } finally {
            try {
                inputStream?.close()
            } catch (ignored: IOException) {
            }
            db?.endTransaction()
        }
    }
}
