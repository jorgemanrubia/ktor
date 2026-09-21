/*
 * Copyright 2014-2026 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.plugins

import io.ktor.client.request.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class HttpRequestRetryConfigTest {

    private fun HttpRequestRetryConfig.delayFor(retry: Int): Long =
        delayMillis(HttpRetryDelayContext(HttpRequestBuilder(), null, null), retry)

    @Test
    fun `retryDelay uses the duration returned by the block`() {
        val config = HttpRequestRetryConfig().apply {
            retryDelay(respectRetryAfterHeader = false) { retry -> (retry * 3).seconds }
        }

        assertEquals(3000, config.delayFor(1))
        assertEquals(6000, config.delayFor(2))
    }

    @Test
    fun `constantDelay with duration produces the configured delay plus randomization`() {
        val config = HttpRequestRetryConfig().apply {
            constantDelay(delay = 2.seconds, randomization = 500.milliseconds, respectRetryAfterHeader = false)
        }

        val delay = config.delayFor(1)
        assertTrue(delay in 2000..2500, "Unexpected delay: $delay")
    }

    @Test
    fun `exponentialDelay with duration grows and is capped`() {
        val config = HttpRequestRetryConfig().apply {
            exponentialDelay(
                baseDelay = 1.seconds,
                maxDelay = 3.seconds,
                randomization = 0.milliseconds,
                respectRetryAfterHeader = false
            )
        }

        assertEquals(1000, config.delayFor(1))
        assertEquals(2000, config.delayFor(2))
        assertEquals(3000, config.delayFor(3))
    }

    @Suppress("DEPRECATION")
    @Test
    fun `deprecated millis overloads delegate to the duration ones`() {
        val config = HttpRequestRetryConfig().apply {
            delayMillis(respectRetryAfterHeader = false) { retry -> retry * 100L }
        }

        assertEquals(200, config.delayFor(2))
    }
}
