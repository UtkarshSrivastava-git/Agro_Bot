package com.example.agrobot

import android.os.Build
import android.util.Log
import java.net.InetAddress
import java.net.Socket
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

/**
 * A custom SSLSocketFactory that enables older TLS protocols.
 * This is sometimes necessary to connect to older servers that do not support the latest TLS versions.
 */
class TlsEnabler(private val defaultFactory: SSLSocketFactory) : SSLSocketFactory() {

    companion object {
        private const val TAG = "TlsEnabler"

        /**
         * Creates and returns a new SSLSocketFactory that attempts to enable TLSv1.1 and TLSv1.2.
         */
        fun createSslSocketFactory(): SSLSocketFactory {
            val context = SSLContext.getInstance("TLS")
            context.init(null, null, null)
            return TlsEnabler(context.socketFactory)
        }
    }

    private fun enableTlsOnSocket(socket: Socket?): Socket? {
        if (socket != null && socket is SSLSocket) {
            try {
                // Get the list of supported protocols
                val supportedProtocols = socket.supportedProtocols
                // Enable all TLS versions (TLSv1, TLSv1.1, TLSv1.2, TLSv1.3)
                val enabledProtocols = supportedProtocols.filter {
                    it.startsWith("TLS")
                }.toTypedArray()

                if (enabledProtocols.isNotEmpty()) {
                    socket.enabledProtocols = enabledProtocols
                    Log.d(TAG, "Enabled protocols: ${enabledProtocols.joinToString()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error enabling TLS protocols", e)
            }
        }
        return socket
    }

    override fun getDefaultCipherSuites(): Array<String> = defaultFactory.defaultCipherSuites
    override fun getSupportedCipherSuites(): Array<String> = defaultFactory.supportedCipherSuites
    override fun createSocket(s: Socket?, host: String?, port: Int, autoClose: Boolean): Socket? =
        enableTlsOnSocket(defaultFactory.createSocket(s, host, port, autoClose))
    override fun createSocket(host: String?, port: Int): Socket? =
        enableTlsOnSocket(defaultFactory.createSocket(host, port))
    override fun createSocket(host: String?, port: Int, localHost: InetAddress?, localPort: Int): Socket? =
        enableTlsOnSocket(defaultFactory.createSocket(host, port, localHost, localPort))
    override fun createSocket(host: InetAddress?, port: Int): Socket? =
        enableTlsOnSocket(defaultFactory.createSocket(host, port))
    override fun createSocket(address: InetAddress?, port: Int, localAddress: InetAddress?, localPort: Int): Socket? =
        enableTlsOnSocket(defaultFactory.createSocket(address, port, localAddress, localPort))
}
