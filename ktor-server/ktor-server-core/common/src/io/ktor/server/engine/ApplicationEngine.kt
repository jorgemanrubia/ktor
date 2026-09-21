/*
 * Copyright 2014-2026 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.server.engine

import io.ktor.server.application.*
import io.ktor.server.engine.internal.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * An engine which runs an application.
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.engine.ApplicationEngine)
 */
public interface ApplicationEngine {

    /**
     * A configuration for the [ApplicationEngine].
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.engine.ApplicationEngine.Configuration)
     */
    @Suppress("MemberVisibilityCanBePrivate", "ktlint:standard:no-consecutive-comments")
    // TODO KTOR-8809: Uncomment the annotation
    // @KtorDsl
    public open class Configuration {
        /**
         * Returns the current parallelism level (e.g. the number of available processors).
         *
         * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.engine.ApplicationEngine.Configuration.parallelism)
         */
        public val parallelism: Int = availableProcessorsBridge()

        /**
         * Specifies how many threads are used to accept new connections and start call processing.
         *
         * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.engine.ApplicationEngine.Configuration.connectionGroupSize)
         */
        public var connectionGroupSize: Int = parallelism / 2 + 1

        /**
         * Specifies size of the event group for processing connections, parsing messages and doing engine's internal work
         *
         * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.engine.ApplicationEngine.Configuration.workerGroupSize)
         */
        public var workerGroupSize: Int = parallelism / 2 + 1

        /**
         * Specifies the minimum size of a thread pool used to process application calls.
         *
         * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.engine.ApplicationEngine.Configuration.callGroupSize)
         */
        public var callGroupSize: Int = parallelism

        /**
         * Specifies the maximum amount of time for activity to cool down
         *
         * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.engine.ApplicationEngine.Configuration.shutdownGracePeriodDuration)
         */
        public var shutdownGracePeriodDuration: Duration = 1.seconds

        /**
         * Specifies the maximum amount of time to wait until server stops gracefully
         *
         * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.engine.ApplicationEngine.Configuration.shutdownTimeoutDuration)
         */
        public var shutdownTimeoutDuration: Duration = 5.seconds

        /**
         * Specifies the maximum amount of time in milliseconds for activity to cool down
         *
         * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.engine.ApplicationEngine.Configuration.shutdownGracePeriod)
         */
        @Deprecated(
            "Use shutdownGracePeriodDuration with kotlin.time.Duration instead.",
            ReplaceWith("shutdownGracePeriodDuration"),
            level = DeprecationLevel.WARNING
        )
        public var shutdownGracePeriod: Long
            get() = shutdownGracePeriodDuration.inWholeMilliseconds
            set(value) {
                shutdownGracePeriodDuration = value.milliseconds
            }

        /**
         * Specifies the maximum amount of time in milliseconds to wait until server stops gracefully
         *
         * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.engine.ApplicationEngine.Configuration.shutdownTimeout)
         */
        @Deprecated(
            "Use shutdownTimeoutDuration with kotlin.time.Duration instead.",
            ReplaceWith("shutdownTimeoutDuration"),
            level = DeprecationLevel.WARNING
        )
        public var shutdownTimeout: Long
            get() = shutdownTimeoutDuration.inWholeMilliseconds
            set(value) {
                shutdownTimeoutDuration = value.milliseconds
            }

        /**
         * List of connectors describing where and how the server should listen.
         *
         * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.engine.ApplicationEngine.Configuration.connectors)
         */
        public var connectors: MutableList<EngineConnectorConfig> = mutableListOf()

        /**
         * Uses [other] configuration and overrides this with its values.
         *
         * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.engine.ApplicationEngine.Configuration.takeFrom)
         */
        public fun takeFrom(other: Configuration) {
            connectionGroupSize = other.connectionGroupSize
            workerGroupSize = other.workerGroupSize
            callGroupSize = other.callGroupSize
            shutdownGracePeriodDuration = other.shutdownGracePeriodDuration
            shutdownTimeoutDuration = other.shutdownTimeoutDuration
            connectors.addAll(other.connectors)
        }
    }

    /**
     * Local addresses for application connectors.
     * If [environment]'s [connector]s was configured to use port=0, you can use this function to get an actual port
     * for these connectors.
     * Available after a server is started.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.engine.ApplicationEngine.resolvedConnectors)
     */
    public suspend fun resolvedConnectors(): List<EngineConnectorConfig>

    /**
     * An environment used to run this engine.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.engine.ApplicationEngine.environment)
     */
    public val environment: ApplicationEnvironment

    /**
     * Starts this [ApplicationEngine].
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.engine.ApplicationEngine.start)
     *
     * @param wait if true, then the `start` call blocks a current thread until it finishes its execution.
     * If you run `start` from the main thread with `wait = false` and nothing else blocking this thread,
     * then your application will be terminated without handling any requests.
     * @return returns this instance
     */
    public fun start(wait: Boolean = false): ApplicationEngine

    /**
     * Starts this [ApplicationEngine].
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.engine.ApplicationEngine.startSuspend)
     *
     * @param wait if true, then the `start` call blocks a current thread until it finishes its execution.
     * If you run `start` from the main thread with `wait = false` and nothing else blocking this thread,
     * then your application will be terminated without handling any requests.
     * @return returns this instance
     */
    public suspend fun startSuspend(wait: Boolean = false): ApplicationEngine {
        return withContext(Dispatchers.IOBridge) { start(wait) }
    }

    /**
     * Stops this [ApplicationEngine] with the default grace period and timeout of 500 milliseconds each.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.engine.ApplicationEngine.stop)
     */
    public fun stop() {
        stop(DEFAULT_STOP_GRACE_PERIOD, DEFAULT_STOP_TIMEOUT)
    }

    /**
     * Stops this [ApplicationEngine].
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.engine.ApplicationEngine.stop)
     *
     * @param gracePeriod the maximum amount of time for activity to cool down
     * @param timeout the maximum amount of time to wait until a server stops gracefully
     */
    @Suppress("DEPRECATION")
    public fun stop(gracePeriod: Duration, timeout: Duration = DEFAULT_STOP_TIMEOUT) {
        stop(gracePeriod.inWholeMilliseconds, timeout.inWholeMilliseconds)
    }

    /**
     * Stops this [ApplicationEngine].
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.engine.ApplicationEngine.stop)
     *
     * @param gracePeriodMillis the maximum amount of time for activity to cool down
     * @param timeoutMillis the maximum amount of time to wait until a server stops gracefully
     */
    @Deprecated(
        "Use stop(gracePeriod, timeout) with kotlin.time.Duration instead.",
        ReplaceWith(
            "stop(gracePeriodMillis.milliseconds, timeoutMillis.milliseconds)",
            "kotlin.time.Duration.Companion.milliseconds"
        ),
        level = DeprecationLevel.WARNING
    )
    public fun stop(gracePeriodMillis: Long = 500, timeoutMillis: Long = 500)

    /**
     * Stops this [ApplicationEngine] with the default grace period and timeout of 500 milliseconds each.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.engine.ApplicationEngine.stopSuspend)
     */
    public suspend fun stopSuspend() {
        stopSuspend(DEFAULT_STOP_GRACE_PERIOD, DEFAULT_STOP_TIMEOUT)
    }

    /**
     * Stops this [ApplicationEngine].
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.engine.ApplicationEngine.stopSuspend)
     *
     * @param gracePeriod the maximum amount of time for activity to cool down
     * @param timeout the maximum amount of time to wait until a server stops gracefully
     */
    public suspend fun stopSuspend(gracePeriod: Duration, timeout: Duration = DEFAULT_STOP_TIMEOUT) {
        withContext(Dispatchers.IOBridge) { stop(gracePeriod, timeout) }
    }

    /**
     * Stops this [ApplicationEngine].
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.engine.ApplicationEngine.stopSuspend)
     *
     * @param gracePeriodMillis the maximum amount of time for activity to cool down
     * @param timeoutMillis the maximum amount of time to wait until a server stops gracefully
     */
    @Deprecated(
        "Use stopSuspend(gracePeriod, timeout) with kotlin.time.Duration instead.",
        ReplaceWith(
            "stopSuspend(gracePeriodMillis.milliseconds, timeoutMillis.milliseconds)",
            "kotlin.time.Duration.Companion.milliseconds"
        ),
        level = DeprecationLevel.WARNING
    )
    public suspend fun stopSuspend(gracePeriodMillis: Long = 500, timeoutMillis: Long = 500) {
        stopSuspend(gracePeriodMillis.milliseconds, timeoutMillis.milliseconds)
    }

    public companion object {
        private val DEFAULT_STOP_GRACE_PERIOD: Duration = 500.milliseconds
        private val DEFAULT_STOP_TIMEOUT: Duration = 500.milliseconds
    }
}
