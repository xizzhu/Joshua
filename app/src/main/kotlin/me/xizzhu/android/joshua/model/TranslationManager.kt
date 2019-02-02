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

import android.content.SharedPreferences
import android.text.TextUtils
import com.squareup.moshi.Json
import io.reactivex.Observable
import io.reactivex.Single
import me.xizzhu.android.joshua.SHARED_PREFERENCES_KEY_LAST_TRANSLATION
import retrofit2.Retrofit
import retrofit2.http.GET
import javax.inject.Inject
import javax.inject.Singleton

data class TranslationInfo(val name: String, val shortName: String, val language: String, val size: Long)

@Singleton
class TranslationManager @Inject constructor(private val sharedPreferences: SharedPreferences, retrofit: Retrofit) {
    private val translationService = retrofit.create(TranslationService::class.java)

    fun hasTranslationsInstalled() = !TextUtils.isEmpty(sharedPreferences.getString(SHARED_PREFERENCES_KEY_LAST_TRANSLATION, null))

    fun loadTranslations(): Observable<List<TranslationInfo>> {
        return translationService.fetchTranslationList().map { it.translations }.toObservable()
    }
}

private data class BackendTranslationList(@Json(name = "translations") val translations: List<TranslationInfo>)

private interface TranslationService {
    @GET("list.json")
    fun fetchTranslationList(): Single<BackendTranslationList>
}
