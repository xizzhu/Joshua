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

package me.xizzhu.android.joshua.core.repository.remote.android

import android.os.Build
import android.util.JsonReader
import kotlinx.coroutines.channels.SendChannel
import me.xizzhu.android.logger.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.StringReader
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.Socket
import java.net.URL
import java.net.URLConnection
import java.security.KeyStore
import java.util.zip.ZipInputStream
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

private const val TAG = "AndroidHttpService"
private const val TIMEOUT_IN_MILLISECONDS = 30000 // 30 seconds
private const val BASE_URL = "https://xizzhu.me/bible/download"

internal fun download(downloadProgress: SendChannel<Int>, relativeUrl: String, contentLength: Long, destination: File) {
    if (destination.length() == contentLength) return

    var deleteDestinationOnError = true
    try {
        val connection = getHttpConnection(relativeUrl)
        var downloaded = destination.length()
        connection.addRequestProperty("range", "bytes=$downloaded-$contentLength")

        val supportsRangeRequests = connection.supportsRangeRequests()
        deleteDestinationOnError = !supportsRangeRequests
        connection.inputStream.use { input ->
            val buffer = ByteArray(4096)
            var progress = (downloaded * 99 / contentLength).toInt()
            FileOutputStream(destination, supportsRangeRequests).use { output ->
                while (true) {
                    val read = input.read(buffer)
                    if (read <= 0) break

                    output.write(buffer, 0, read)
                    downloaded += read

                    // only emits if the progress is actually changed
                    val currentProgress = (downloaded * 99 / contentLength).toInt()
                    if (currentProgress > progress) {
                        progress = currentProgress
                        downloadProgress.trySend(progress)
                    }
                }
            }
        }
    } catch (t: Throwable) {
        if (deleteDestinationOnError) {
            destination.delete()
        }
        throw t
    }
}

private fun getHttpConnection(relativeUrl: String): HttpURLConnection =
        (URL("$BASE_URL/$relativeUrl").openConnection() as HttpURLConnection)
                .apply {
                    configureTLSIfNecessary()
                    instanceFollowRedirects = true
                    connectTimeout = TIMEOUT_IN_MILLISECONDS
                    readTimeout = TIMEOUT_IN_MILLISECONDS
                }

private fun HttpURLConnection.supportsRangeRequests(): Boolean =
        getHeaderField("accept-ranges") == "bytes" && contentLength > 0L

internal fun getInputStream(relativeUrl: String): InputStream = getHttpConnection(relativeUrl).inputStream

// For more details, see https://github.com/square/okhttp/issues/2372#issuecomment-244807676
private fun HttpURLConnection.configureTLSIfNecessary(): URLConnection {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1 && this is HttpsURLConnection) {
        return this
    }
    Log.d(TAG, "Enabling TLS v1.2...")
    try {
        (this as HttpsURLConnection).sslSocketFactory = tls12SocketFactory
        Log.d(TAG, "TLS v1.2 enabled")
    } catch (e: Exception) {
        Log.e(TAG, "Failed to enable TLS v1.2", e)
    }
    return this
}

private val tls12SocketFactory: SSLSocketFactory by lazy {
    val trustManager = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            .apply { init(null as KeyStore?) }
            .trustManagers
            .first { it is X509TrustManager }
    val sslContext = SSLContext.getInstance("TLSv1.2")
            .apply { init(null, arrayOf(trustManager), null) }
    TLS12SocketFactory(sslContext.socketFactory)
}

private class TLS12SocketFactory(private val delegate: SSLSocketFactory) : SSLSocketFactory() {
    override fun createSocket(s: Socket?, host: String?, port: Int, autoClose: Boolean): Socket =
            delegate.createSocket(s, host, port, autoClose).patch()

    private fun Socket.patch(): Socket {
        (this as? SSLSocket)?.let { it.enabledProtocols = arrayOf("TLSv1.2") }
        return this
    }

    override fun createSocket(host: String?, port: Int): Socket = delegate.createSocket(host, port).patch()

    override fun createSocket(host: String?, port: Int, localHost: InetAddress?, localPort: Int): Socket =
            delegate.createSocket(host, port, localHost, localPort).patch()

    override fun createSocket(host: InetAddress?, port: Int): Socket = delegate.createSocket(host, port).patch()

    override fun createSocket(address: InetAddress?, port: Int, localAddress: InetAddress?, localPort: Int): Socket =
            delegate.createSocket(address, port, localAddress, localPort).patch()

    override fun getDefaultCipherSuites(): Array<String> = delegate.defaultCipherSuites

    override fun getSupportedCipherSuites(): Array<String> = delegate.supportedCipherSuites
}

internal inline fun ZipInputStream.forEach(action: (entryName: String, contentReader: JsonReader) -> Unit) = use {
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

        val contentReader = JsonReader(StringReader(os.toString("UTF-8")))
        action(entryName, contentReader)
        contentReader.close()
    }
}
