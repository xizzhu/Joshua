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

package me.xizzhu.android.joshua.reading

import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import dagger.android.AndroidInjector
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.reading.chapter.ChapterListPresenter
import me.xizzhu.android.joshua.reading.toolbar.ToolbarPresenter
import me.xizzhu.android.joshua.reading.verse.VersePresenter

@Module
class ReadingModule {
    @Provides
    fun provideReadingPresenter(bibleReadingManager: BibleReadingManager) = ReadingPresenter(bibleReadingManager)

    @Provides
    fun provideToolbarPresenter(bibleReadingManager: BibleReadingManager) = ToolbarPresenter(bibleReadingManager)

    @Provides
    fun provideChapterListPresenter(bibleReadingManager: BibleReadingManager) = ChapterListPresenter(bibleReadingManager)

    @Provides
    fun provideVersePresenter(bibleReadingManager: BibleReadingManager) = VersePresenter(bibleReadingManager)
}

@Subcomponent(modules = [(ReadingModule::class)])
interface ReadingComponent : AndroidInjector<ReadingActivity> {
    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<ReadingActivity>()
}
