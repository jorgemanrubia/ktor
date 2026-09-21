/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.server.sessions

import kotlin.test.*
import kotlin.time.Duration.Companion.hours

class SessionTransportCookieTest {

    @Test
    fun testClearBypassSecure() {
        val configuration = CookieConfiguration().apply {
            secure = true
        }

        val cookie = SessionTransportCookie("session", configuration, emptyList())
        assertTrue(cookie.clearCookie().secure)
    }

    @Test
    fun testClearBypassExtensions() {
        val configuration = CookieConfiguration().apply {
            sameSite = SameSite.None
        }

        val cookie = SessionTransportCookie("session", configuration, emptyList())
        assertEquals("None", cookie.clearCookie().sameSite)
    }

    @Test
    fun testClearZerosMaxAge() {
        val configuration = CookieConfiguration().apply {
            @Suppress("DEPRECATION")
            maxAgeInSeconds = 1000
        }

        val cookie = SessionTransportCookie("session", configuration, emptyList())
        assertEquals(0, cookie.clearCookie().maxAge)
    }

    @Suppress("DEPRECATION")
    @Test
    fun testMaxAgeDurationAndSecondsAreViewsOfTheSameValue() {
        val configuration = CookieConfiguration().apply {
            maxAge = 2.hours
        }

        assertEquals(7200, configuration.maxAgeInSeconds)

        configuration.maxAgeInSeconds = null
        assertNull(configuration.maxAge)
    }
}
