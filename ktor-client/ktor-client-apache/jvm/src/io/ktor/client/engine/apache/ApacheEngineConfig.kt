/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.engine.apache

import io.ktor.client.engine.*
import org.apache.http.client.config.RequestConfig
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import javax.net.ssl.SSLContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * A configuration for the [Apache] client engine.
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.apache.ApacheEngineConfig)
 */
@Deprecated(
    message = "Apache engine is deprecated. Consider using Apache5EngineConfig instead.",
    replaceWith = ReplaceWith(
        "Apache5EngineConfig",
        "io.ktor.client.engine.apache5.Apache5EngineConfig"
    ),
    level = DeprecationLevel.WARNING
)
public class ApacheEngineConfig : HttpClientEngineConfig() {
    /**
     * Specifies whether to follow redirects automatically.
     * Disabled by default.
     *
     * _Note: By default, the Apache client allows `50` redirects._
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.apache.ApacheEngineConfig.followRedirects)
     */
    public var followRedirects: Boolean = false

    /**
     * Specifies a maximum time (in milliseconds) of inactivity between two data packets when exchanging data with a server.
     *
     * Set this value to `0` to use an infinite timeout.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.apache.ApacheEngineConfig.socketTimeout)
     */
    @Deprecated(
        "Use socketTimeoutDuration with kotlin.time.Duration instead.",
        ReplaceWith("socketTimeoutDuration"),
        level = DeprecationLevel.WARNING
    )
    public var socketTimeout: Int = 10_000

    /**
     * Specifies a maximum time of inactivity between two data packets when exchanging data with a server.
     *
     * Use [Duration.INFINITE] for an infinite timeout.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.apache.ApacheEngineConfig.socketTimeoutDuration)
     */
    @Suppress("DEPRECATION")
    public var socketTimeoutDuration: Duration
        get() = if (socketTimeout == 0) Duration.INFINITE else socketTimeout.milliseconds
        set(value) {
            socketTimeout = value.toMillisWithInfiniteAsZero()
        }

    /**
     * Specifies a time period (in milliseconds) in which a client should establish a connection with a server.
     *
     * A `0` value represents an infinite timeout, while `-1` represents a system's default value.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.apache.ApacheEngineConfig.connectTimeout)
     */
    @Deprecated(
        "Use connectTimeoutDuration with kotlin.time.Duration instead.",
        ReplaceWith("connectTimeoutDuration"),
        level = DeprecationLevel.WARNING
    )
    public var connectTimeout: Int = 10_000

    /**
     * Specifies a time period in which a client should establish a connection with a server.
     *
     * Use [Duration.INFINITE] for an infinite timeout, or `null` for the system's default value.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.apache.ApacheEngineConfig.connectTimeoutDuration)
     */
    @Suppress("DEPRECATION")
    public var connectTimeoutDuration: Duration?
        get() = connectTimeout.toDurationWithZeroAsInfinite()
        set(value) {
            connectTimeout = value?.toMillisWithInfiniteAsZero() ?: -1
        }

    /**
     * Specifies a time period (in milliseconds) in which a client should start a request.
     *
     * A `0` value represents an infinite timeout, while `-1` represents a system's default value.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.apache.ApacheEngineConfig.connectionRequestTimeout)
     */
    @Deprecated(
        "Use connectionRequestTimeoutDuration with kotlin.time.Duration instead.",
        ReplaceWith("connectionRequestTimeoutDuration"),
        level = DeprecationLevel.WARNING
    )
    public var connectionRequestTimeout: Int = 20_000

    /**
     * Specifies a time period in which a client should start a request.
     *
     * Use [Duration.INFINITE] for an infinite timeout, or `null` for the system's default value.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.apache.ApacheEngineConfig.connectionRequestTimeoutDuration)
     */
    @Suppress("DEPRECATION")
    public var connectionRequestTimeoutDuration: Duration?
        get() = connectionRequestTimeout.toDurationWithZeroAsInfinite()
        set(value) {
            connectionRequestTimeout = value?.toMillisWithInfiniteAsZero() ?: -1
        }

    /**
     * Allows you to configure [SSL](https://ktor.io/docs/client-ssl.html) settings for this engine.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.apache.ApacheEngineConfig.sslContext)
     */
    public var sslContext: SSLContext? = null

    /**
     * Specifies a custom processor for [RequestConfig.Builder].
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.apache.ApacheEngineConfig.customRequest)
     */
    public var customRequest: (RequestConfig.Builder.() -> RequestConfig.Builder) = { this }
        private set

    /**
     * Specifies a custom processor for [HttpAsyncClientBuilder].
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.apache.ApacheEngineConfig.customClient)
     */
    public var customClient: (HttpAsyncClientBuilder.() -> HttpAsyncClientBuilder) = { this }
        private set

    /**
     * Customizes a [RequestConfig.Builder] in the specified [block].
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.apache.ApacheEngineConfig.customizeRequest)
     */
    public fun customizeRequest(block: RequestConfig.Builder.() -> Unit) {
        val current = customRequest
        customRequest = { current().apply(block) }
    }

    /**
     * Customizes a [HttpAsyncClientBuilder] in the specified [block].
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.apache.ApacheEngineConfig.customizeClient)
     */
    public fun customizeClient(block: HttpAsyncClientBuilder.() -> Unit) {
        val current = customClient
        customClient = { current().apply(block) }
    }
}

private fun Duration.toMillisWithInfiniteAsZero(): Int =
    if (isInfinite()) 0 else inWholeMilliseconds.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()

private fun Int.toDurationWithZeroAsInfinite(): Duration? = when {
    this < 0 -> null
    this == 0 -> Duration.INFINITE
    else -> milliseconds
}
