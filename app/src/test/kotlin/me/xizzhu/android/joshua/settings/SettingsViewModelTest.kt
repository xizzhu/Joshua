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

import android.app.Application
import android.content.ContentResolver
import android.net.Uri
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.BackupManager
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.infra.BaseViewModel
import me.xizzhu.android.joshua.tests.BaseUnitTest
import java.io.InputStream
import java.io.OutputStream
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SettingsViewModelTest : BaseUnitTest() {
    private lateinit var uri: Uri
    private lateinit var inputStream: InputStream
    private lateinit var outputStream: OutputStream
    private lateinit var resolver: ContentResolver
    private lateinit var backupManager: BackupManager
    private lateinit var settingsManager: SettingsManager
    private lateinit var application: Application
    private lateinit var settingsViewModel: SettingsViewModel

    @BeforeTest
    override fun setup() {
        super.setup()

        uri = mockk()
        inputStream = mockk<InputStream>().apply { every { close() } returns Unit }
        outputStream = mockk<OutputStream>().apply { every { close() } returns Unit }
        resolver = mockk<ContentResolver>().apply {
            every { openInputStream(uri) } returns inputStream
            every { openOutputStream(uri) } returns outputStream
        }
        backupManager = mockk<BackupManager>().apply {
            coEvery { backup(outputStream) } returns Unit
            coEvery { restore(inputStream) } returns Unit
        }
        settingsManager = mockk()
        every { settingsManager.settings() } returns emptyFlow()
        application = mockk<Application>().apply { every { contentResolver } returns resolver }

        settingsViewModel = SettingsViewModel(backupManager, settingsManager, application)
    }

    @Test
    fun `test backup with null uri`(): Unit = runTest {
        val actual = settingsViewModel.backup(null).toList()
        assertEquals(1, actual.size)
        assertTrue(actual[0] is BaseViewModel.ViewData.Failure)
    }

    @Test
    fun `test backup with error`(): Unit = runTest {
        val ex = RuntimeException("Random")
        every { application.contentResolver } throws ex

        val actual = settingsViewModel.backup(mockk()).toList()
        assertEquals(2, actual.size)
        assertTrue(actual[0] is BaseViewModel.ViewData.Loading)
        assertEquals(ex, (actual[1] as BaseViewModel.ViewData.Failure).throwable)
    }

    @Test
    fun `test backup with error from interactor`(): Unit = runTest {
        val ex = RuntimeException("Random")
        coEvery { backupManager.backup(outputStream) } throws ex

        val actual = settingsViewModel.backup(uri).toList()
        assertEquals(2, actual.size)
        assertTrue(actual[0] is BaseViewModel.ViewData.Loading)
        assertEquals(ex, (actual[1] as BaseViewModel.ViewData.Failure).throwable)
    }

    @Test
    fun `test backup`(): Unit = runTest {
        val actual = settingsViewModel.backup(uri).toList()
        assertEquals(2, actual.size)
        assertTrue(actual[0] is BaseViewModel.ViewData.Loading)
        assertEquals(R.string.toast_backed_up, (actual[1] as BaseViewModel.ViewData.Success<Int>).data)
    }

    @Test
    fun `test restore with null uri`(): Unit = runTest {
        val actual = settingsViewModel.restore(null).toList()
        assertEquals(1, actual.size)
        assertTrue(actual[0] is BaseViewModel.ViewData.Failure)
    }

    @Test
    fun `test restore with error`(): Unit = runTest {
        val ex = RuntimeException("Random")
        every { application.contentResolver } throws ex

        val actual = settingsViewModel.restore(mockk()).toList()
        assertEquals(2, actual.size)
        assertTrue(actual[0] is BaseViewModel.ViewData.Loading)
        assertEquals(ex, (actual[1] as BaseViewModel.ViewData.Failure).throwable)
    }

    @Test
    fun `test restore with error from interactor`(): Unit = runTest {
        val ex = RuntimeException("Random")
        coEvery { backupManager.restore(inputStream) } throws ex

        val actual = settingsViewModel.restore(uri).toList()
        assertEquals(2, actual.size)
        assertTrue(actual[0] is BaseViewModel.ViewData.Loading)
        assertEquals(ex, (actual[1] as BaseViewModel.ViewData.Failure).throwable)
    }

    @Test
    fun `test restore`(): Unit = runTest {
        val actual = settingsViewModel.restore(uri).toList()
        assertEquals(2, actual.size)
        assertTrue(actual[0] is BaseViewModel.ViewData.Loading)
        assertEquals(R.string.toast_restored, (actual[1] as BaseViewModel.ViewData.Success<Int>).data)
    }
}
