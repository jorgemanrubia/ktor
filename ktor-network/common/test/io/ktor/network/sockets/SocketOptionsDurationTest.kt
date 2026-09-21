/*
 * Copyright 2014-2026 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.network.sockets

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class SocketOptionsDurationTest {

    private fun tcpOptions(): SocketOptions.TCPClientSocketOptions = SocketOptions.create().peer().tcpConnect()

    @Suppress("DEPRECATION")
    @Test
    fun `socket timeout duration and millis are views of the same value`() {
        val options = tcpOptions()

        assertEquals(Duration.INFINITE, options.socketTimeoutDuration)
        assertEquals(Long.MAX_VALUE, options.socketTimeout)

        options.socketTimeoutDuration = 3.seconds
        assertEquals(3000, options.socketTimeout)

        options.socketTimeout = Long.MAX_VALUE
        assertEquals(Duration.INFINITE, options.socketTimeoutDuration)
    }

    @Suppress("DEPRECATION")
    @Test
    fun `linger duration maps null to minus one seconds`() {
        val options = tcpOptions()

        assertNull(options.linger)
        assertEquals(-1, options.lingerSeconds)

        options.linger = 2.seconds
        assertEquals(2, options.lingerSeconds)

        options.lingerSeconds = -1
        assertNull(options.linger)
    }

    @Test
    fun `copy keeps linger`() {
        val options = tcpOptions().apply { linger = 4.seconds }

        assertEquals(4.seconds, options.copy().linger)
    }
}
