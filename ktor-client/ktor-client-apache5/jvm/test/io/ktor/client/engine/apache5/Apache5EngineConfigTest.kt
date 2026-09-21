/*
 * Copyright 2014-2026 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.engine.apache5

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking
import org.apache.hc.client5.http.DnsResolver
import java.net.InetAddress
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class Apache5EngineConfigTest {

    @Test
    fun `dnsResolver config is invoked when set`() = runBlocking {
        val callCount = AtomicInteger()
        val resolver = object : DnsResolver {
            override fun resolve(host: String): Array<InetAddress> {
                callCount.incrementAndGet()
                return arrayOf(InetAddress.getByName("127.0.0.1"))
            }

            override fun resolveCanonicalHostname(host: String): String = "127.0.0.1"
        }

        val client = HttpClient(Apache5) {
            engine { dnsResolver = resolver }
        }

        try {
            runCatching { client.get("http://example.invalid:1/") }
        } finally {
            client.close()
        }

        assertTrue(callCount.get() > 0, "Custom DnsResolver should have been invoked")
    }

    @Suppress("DEPRECATION")
    @Test
    fun `duration timeouts map infinite to zero and null to minus one`() {
        val config = Apache5EngineConfig()

        assertEquals(10.seconds, config.socketTimeoutDuration)
        assertEquals(10.seconds, config.connectTimeoutDuration)
        assertEquals(20.seconds, config.connectionRequestTimeoutDuration)

        config.socketTimeoutDuration = Duration.INFINITE
        config.connectTimeoutDuration = null
        config.connectionRequestTimeoutDuration = 3.seconds

        assertEquals(0, config.socketTimeout)
        assertEquals(-1, config.connectTimeout)
        assertEquals(3000, config.connectionRequestTimeout)

        config.socketTimeout = 0
        config.connectTimeout = 0
        config.connectionRequestTimeout = -1

        assertEquals(Duration.INFINITE, config.socketTimeoutDuration)
        assertEquals(Duration.INFINITE, config.connectTimeoutDuration)
        assertNull(config.connectionRequestTimeoutDuration)
    }
}
