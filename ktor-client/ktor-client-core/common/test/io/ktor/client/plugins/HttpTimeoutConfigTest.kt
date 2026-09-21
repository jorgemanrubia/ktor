/*
 * Copyright 2014-2026 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.plugins

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class HttpTimeoutConfigTest {

    @Test
    fun `duration properties are unset by default`() {
        val config = HttpTimeoutConfig()

        assertNull(config.requestTimeout)
        assertNull(config.connectTimeout)
        assertNull(config.socketTimeout)
    }

    @Suppress("DEPRECATION")
    @Test
    fun `duration and millis properties are views of the same value`() {
        val config = HttpTimeoutConfig()

        config.requestTimeout = 5.seconds
        config.connectTimeoutMillis = 250
        config.socketTimeout = Duration.INFINITE

        assertEquals(5000, config.requestTimeoutMillis)
        assertEquals(250.milliseconds, config.connectTimeout)
        assertEquals(HttpTimeoutConfig.INFINITE_TIMEOUT_MS, config.socketTimeoutMillis)
        assertEquals(Duration.INFINITE, HttpTimeoutConfig.INFINITE_TIMEOUT_MS.milliseconds)
    }

    @Suppress("DEPRECATION")
    @Test
    fun `duration constructor matches millis constructor`() {
        val fromDuration = HttpTimeoutConfig(1.seconds, 2.seconds, 3.seconds)
        val fromMillis = HttpTimeoutConfig(1000L, 2000L, 3000L)

        assertEquals(fromMillis, fromDuration)
        assertEquals(fromMillis.hashCode(), fromDuration.hashCode())
    }

    @Test
    fun `non positive durations are rejected`() {
        val config = HttpTimeoutConfig()

        assertFailsWith<IllegalArgumentException> { config.requestTimeout = Duration.ZERO }
        assertFailsWith<IllegalArgumentException> { config.connectTimeout = (-1).seconds }
    }
}
