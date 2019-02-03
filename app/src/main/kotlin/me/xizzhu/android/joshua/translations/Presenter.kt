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

package me.xizzhu.android.joshua.translations

import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.observers.DisposableCompletableObserver
import io.reactivex.observers.DisposableObserver
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import me.xizzhu.android.joshua.model.TranslationInfo
import me.xizzhu.android.joshua.model.TranslationManager
import me.xizzhu.android.joshua.utils.MVPPresenter
import me.xizzhu.android.joshua.utils.applySchedulers

class TranslationManagementPresenter(private val translationManager: TranslationManager) : MVPPresenter<TranslationManagementView>() {
    private var loadTranslationsDisposable: Disposable? = null
    private var downloadTranslationDisposable: Disposable? = null

    override fun onViewDropped() {
        disposeLoadTranslations()
        disposeDownloadTranslation()

        super.onViewDropped()
    }

    private fun disposeLoadTranslations() {
        loadTranslationsDisposable?.dispose()
        loadTranslationsDisposable = null
    }

    private fun disposeDownloadTranslation() {
        downloadTranslationDisposable?.dispose()
        downloadTranslationDisposable = null
    }

    fun loadTranslations(forceRefresh: Boolean) {
        disposeLoadTranslations()
        loadTranslationsDisposable = Single.zip(translationManager.loadTranslations(forceRefresh).subscribeOn(Schedulers.io()),
                translationManager.loadCurrentTranslation().subscribeOn(Schedulers.io()),
                BiFunction<List<TranslationInfo>, String, Pair<List<TranslationInfo>, String>> { translations, currentTranslation ->
                    Pair(translations, currentTranslation)
                }).applySchedulers()
                .subscribeWith(object : DisposableSingleObserver<Pair<List<TranslationInfo>, String>>() {
                    override fun onSuccess(t: Pair<List<TranslationInfo>, String>) {
                        loadTranslationsDisposable = null
                        view?.onTranslationsLoaded(t.first, t.second)
                    }

                    override fun onError(e: Throwable) {
                        loadTranslationsDisposable = null
                        view?.onTranslationsLoadFailed()
                    }
                })
    }

    fun downloadTranslation(translationInfo: TranslationInfo) {
        disposeDownloadTranslation()
        downloadTranslationDisposable = translationManager.downloadTranslation(translationInfo)
                .applySchedulers()
                .subscribeWith(object : DisposableObserver<Int>() {
                    override fun onComplete() {
                        downloadTranslationDisposable = null
                        view?.onTranslationDownloaded()
                    }

                    override fun onNext(progress: Int) {
                        view?.onTranslationDownloadProgressed(progress)
                    }

                    override fun onError(e: Throwable) {
                        downloadTranslationDisposable = null
                        view?.onTranslationDownloadFailed()
                    }
                })
    }
}
