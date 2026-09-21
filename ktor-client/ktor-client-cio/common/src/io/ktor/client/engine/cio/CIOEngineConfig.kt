/*
 * Copyright 2014-2026 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.engine.cio

import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.network.tls.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * A configuration for the [CIO] client engine.
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.cio.CIOEngineConfig)
 */
public class CIOEngineConfig : HttpClientEngineConfig() {
    /**
     * Provides access to [Endpoint] settings.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.cio.CIOEngineConfig.endpoint)
     */
    public val endpoint: EndpointConfig = EndpointConfig()

    /**
     * Allows you to configure [HTTPS](https://ktor.io/docs/client-ssl.html) settings for this engine.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.cio.CIOEngineConfig.https)
     */
    public val https: TLSConfigBuilder = TLSConfigBuilder()

    /**
     * Specifies the maximum number of connections used to make [requests](https://ktor.io/docs/request.html).
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.cio.CIOEngineConfig.maxConnectionsCount)
     */
    public var maxConnectionsCount: Int = 1000

    /**
     * Specifies a request timeout in milliseconds.
     * The request timeout is the time period required to process an HTTP call:
     * from sending a request to receiving a response.
     *
     * To disable this timeout, set its value to `0`.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.cio.CIOEngineConfig.requestTimeout)
     */
    @Deprecated(
        "Use requestTimeoutDuration with kotlin.time.Duration instead.",
        ReplaceWith("requestTimeoutDuration"),
        level = DeprecationLevel.WARNING
    )
    public var requestTimeout: Long = 15000

    /**
     * Specifies a request timeout.
     * The request timeout is the time period required to process an HTTP call:
     * from sending a request to receiving a response.
     *
     * To disable this timeout, set its value to [Duration.INFINITE].
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.cio.CIOEngineConfig.requestTimeoutDuration)
     */
    @Suppress("DEPRECATION")
    public var requestTimeoutDuration: Duration
        get() = if (requestTimeout == 0L) Duration.INFINITE else requestTimeout.milliseconds
        set(value) {
            requestTimeout = if (value.isInfinite()) 0L else value.inWholeMilliseconds
        }

    /**
     * Resolves a hostname to a list of IP address strings used to establish connections.
     * When `null`, the platform's default name service is used.
     *
     * Bypassed for proxied requests and Unix domain sockets. TLS SNI keeps using the original
     * hostname, so resolving to an IP does not break certificate validation.
     *
     * An empty result fails the connection with [FailToConnectException]. Currently only the
     * first address is used; multi-address fallback is left to a follow-up.
     *
     * ```kotlin
     * HttpClient(CIO) {
     *     engine {
     *         dnsResolver = { hostname -> listOf("127.0.0.1") }
     *     }
     * }
     * ```
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.cio.CIOEngineConfig.dnsResolver)
     */
    public var dnsResolver: (suspend (hostname: String) -> List<String>)? = null

    /**
     * Allows you to configure [HTTPS](https://ktor.io/docs/client-ssl.html) settings for this engine.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.cio.CIOEngineConfig.https)
     */
    public fun https(block: TLSConfigBuilder.() -> Unit): TLSConfigBuilder = https.apply(block)
}

/**
 * Provides access to [Endpoint] settings.
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.cio.endpoint)
 */
public fun CIOEngineConfig.endpoint(block: EndpointConfig.() -> Unit): EndpointConfig = endpoint.apply(block)

/**
 * Contains [Endpoint] settings.
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.cio.EndpointConfig)
 */
public class EndpointConfig {
    /**
     * Specifies the maximum number of connections for each host.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.cio.EndpointConfig.maxConnectionsPerRoute)
     *
     * @see [CIOEngineConfig.maxConnectionsCount]
     */
    public var maxConnectionsPerRoute: Int = 100

    /**
     * Specifies a connection keep-alive time (in milliseconds).
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.cio.EndpointConfig.keepAliveTime)
     */
    @Deprecated(
        "Use keepAliveDuration with kotlin.time.Duration instead.",
        ReplaceWith("keepAliveDuration"),
        level = DeprecationLevel.WARNING
    )
    public var keepAliveTime: Long = 5000

    /**
     * Specifies a connection keep-alive time.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.cio.EndpointConfig.keepAliveDuration)
     */
    @Suppress("DEPRECATION")
    public var keepAliveDuration: Duration
        get() = keepAliveTime.milliseconds
        set(value) {
            keepAliveTime = value.inWholeMilliseconds
        }

    /**
     * Specifies a maximum number of requests to be sent over a single connection without waiting for the corresponding responses (HTTP pipelining).
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.cio.EndpointConfig.pipelineMaxSize)
     */
    public var pipelineMaxSize: Int = 20

    /**
     * Specifies a time period (in milliseconds) in which a client should establish a connection with a server.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.cio.EndpointConfig.connectTimeout)
     */
    @Deprecated(
        "Use connectTimeoutDuration with kotlin.time.Duration instead.",
        ReplaceWith("connectTimeoutDuration"),
        level = DeprecationLevel.WARNING
    )
    public var connectTimeout: Long = 5000

    /**
     * Specifies a time period in which a client should establish a connection with a server.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.cio.EndpointConfig.connectTimeoutDuration)
     */
    @Suppress("DEPRECATION")
    public var connectTimeoutDuration: Duration
        get() = connectTimeout.milliseconds
        set(value) {
            connectTimeout = value.inWholeMilliseconds
        }

    /**
     * Specifies a maximum time (in milliseconds) of inactivity between two data packets when exchanging data with a server.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.cio.EndpointConfig.socketTimeout)
     */
    @Deprecated(
        "Use socketTimeoutDuration with kotlin.time.Duration instead.",
        ReplaceWith("socketTimeoutDuration"),
        level = DeprecationLevel.WARNING
    )
    public var socketTimeout: Long = HttpTimeoutConfig.INFINITE_TIMEOUT_MS

    /**
     * Specifies a maximum time of inactivity between two data packets when exchanging data with a server.
     * Use [Duration.INFINITE] to disable the timeout.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.cio.EndpointConfig.socketTimeoutDuration)
     */
    @Suppress("DEPRECATION")
    public var socketTimeoutDuration: Duration
        get() = socketTimeout.milliseconds
        set(value) {
            socketTimeout = value.inWholeMilliseconds
        }

    /**
     * Specifies a maximum number of connection attempts.
     * Note: this property affects only connection retries, but not request retries.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.cio.EndpointConfig.connectAttempts)
     */
    public var connectAttempts: Int = 1

    /**
     * Allows a socket to close an output channel immediately on writing completion (half-closed TCP connection).
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.cio.EndpointConfig.allowHalfClose)
     */
    public var allowHalfClose: Boolean = false
        @Deprecated("Half closed TCP connection is not supported by all servers, use it at your own risk.")
        set
}
