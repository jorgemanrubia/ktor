/*
 * Copyright 2014-2026 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.server.engine

import io.ktor.server.config.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class ApplicationEngineConfigurationTest {

    private class TestConfiguration : ApplicationEngine.Configuration()

    @Suppress("DEPRECATION")
    @Test
    fun `shutdown durations and millis are views of the same value`() {
        val config = TestConfiguration()

        assertEquals(1.seconds, config.shutdownGracePeriodDuration)
        assertEquals(1000, config.shutdownGracePeriod)
        assertEquals(5.seconds, config.shutdownTimeoutDuration)
        assertEquals(5000, config.shutdownTimeout)

        config.shutdownGracePeriodDuration = 250.milliseconds
        config.shutdownTimeout = 7000

        assertEquals(250, config.shutdownGracePeriod)
        assertEquals(7.seconds, config.shutdownTimeoutDuration)
    }

    @Test
    fun `takeFrom copies shutdown durations`() {
        val source = TestConfiguration().apply {
            shutdownGracePeriodDuration = 2.seconds
            shutdownTimeoutDuration = 9.seconds
        }
        val target = TestConfiguration().apply { takeFrom(source) }

        assertEquals(2.seconds, target.shutdownGracePeriodDuration)
        assertEquals(9.seconds, target.shutdownTimeoutDuration)
    }

    @Test
    fun `deployment config shutdown values are read as milliseconds`() {
        val deploymentConfig = MapApplicationConfig(
            "shutdownGracePeriod" to "1500",
            "shutdownTimeout" to "2500"
        )
        val config = TestConfiguration().apply { loadCommonConfiguration(deploymentConfig) }

        assertEquals(1500.milliseconds, config.shutdownGracePeriodDuration)
        assertEquals(2500.milliseconds, config.shutdownTimeoutDuration)
    }
}
