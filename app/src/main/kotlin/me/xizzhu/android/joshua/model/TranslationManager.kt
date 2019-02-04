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

package me.xizzhu.android.joshua.model

import android.database.sqlite.SQLiteDatabase
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import okio.buffer
import okio.source
import java.io.IOException
import java.lang.Exception
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton

data class TranslationInfo(val shortName: String, val name: String, val language: String,
                           val size: Long, val downloaded: Boolean)

@Singleton
class TranslationManager @Inject constructor(
        private val backendService: BackendService, private val localStorage: LocalStorage) {
    fun hasTranslationsInstalled(): Single<Boolean> =
            loadCurrentTranslation().map { it.isNotEmpty() }

    fun loadCurrentTranslation(): Single<String> =
            localStorage.metadataDao.load(MetadataDao.KEY_LAST_TRANSLATION, "")

    fun loadTranslations(forceRefresh: Boolean): Single<List<TranslationInfo>> {
        return if (forceRefresh) {
            loadTranslationsFromBackend(true)
        } else {
            loadTranslationsFromLocal().filter { it.isNotEmpty() }
                    .switchIfEmpty(loadTranslationsFromBackend(false))
        }
    }

    private fun loadTranslationsFromBackend(needToLoadLocal: Boolean): Single<List<TranslationInfo>> {
        val local = if (needToLoadLocal) {
            loadTranslationsFromLocal()
        } else {
            Single.just(emptyList())
        }
        val backend = backendService.translationService.fetchTranslationList()
        return Single.zip(local, backend,
                BiFunction<List<TranslationInfo>, BackendTranslationList, List<TranslationInfo>> { existing, fetched ->
                    val new = ArrayList<TranslationInfo>(fetched.translations.size)
                    for (f in fetched.translations) {
                        var downloaded = false
                        for (e in existing) {
                            if (e.shortName == f.shortName) {
                                downloaded = e.downloaded
                                break
                            }
                        }
                        new.add(TranslationInfo(f.shortName, f.name, f.language, f.size, downloaded))
                    }
                    new
                }).doOnSuccess {
            localStorage.translationInfoDao.save(it)
        }
    }

    private fun loadTranslationsFromLocal(): Single<List<TranslationInfo>> =
            localStorage.translationInfoDao.load()

    fun downloadTranslation(translationInfo: TranslationInfo): Observable<Int> =
            Observable.create { emitter ->
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
                            progress = currentProgress;
                            emitter.onNext(progress)
                        }
                    }

                    db.setTransactionSuccessful()

                    emitter.onComplete()
                } catch (e: Exception) {
                    emitter.onError(e)
                } finally {
                    try {
                        inputStream?.close()
                    } catch (ignored: IOException) {
                    }
                    db?.endTransaction()
                }
            }
}
