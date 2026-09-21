/*
 * Copyright 2014-2026 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.tests.websocket

import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds

class WebSocketOptionsDurationTest {

    @Suppress("DEPRECATION")
    @Test
    fun `ping period duration and millis are views of the same value`() {
        val options = WebSockets.WebSocketOptions()

        assertNull(options.pingPeriod)
        assertEquals(PINGER_DISABLED, options.pingPeriodMillis)

        options.pingPeriod = 10.seconds
        assertEquals(10_000, options.pingPeriodMillis)

        options.pingPeriodMillis = PINGER_DISABLED
        assertNull(options.pingPeriod)
    }

    @Suppress("DEPRECATION")
    @Test
    fun `timeout duration and millis are views of the same value`() {
        val options = WebSockets.WebSocketOptions()

        assertEquals(15.seconds, options.timeout)

        options.timeout = 20.seconds
        assertEquals(20_000, options.timeoutMillis)
    }

    @Suppress("DEPRECATION")
    @Test
    fun `plugin created with durations exposes the same values as millis`() {
        val webSockets = WebSockets(pingInterval = 5.seconds, timeout = 7.seconds, maxFrameSize = 1024, masking = false)

        assertEquals(5.seconds, webSockets.pingInterval)
        assertEquals(5_000, webSockets.pingIntervalMillis)
        assertEquals(7.seconds, webSockets.timeout)
        assertEquals(7_000, webSockets.timeoutMillis)
    }
}
