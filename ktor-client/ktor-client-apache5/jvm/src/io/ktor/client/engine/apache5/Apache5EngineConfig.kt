/*
 * Copyright 2014-2026 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.engine.apache5

import io.ktor.client.engine.*
import org.apache.hc.client5.http.DnsResolver
import org.apache.hc.client5.http.config.RequestConfig
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder
import org.apache.hc.client5.http.ssl.HostnameVerificationPolicy
import javax.net.ssl.SSLContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * A configuration for the [Apache5] client engine.
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.apache5.Apache5EngineConfig)
 */
public class Apache5EngineConfig : HttpClientEngineConfig() {
    /**
     * Specifies whether to follow redirects automatically.
     * Disabled by default.
     *
     * _Note: By default, the Apache client allows `50` redirects._
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.apache5.Apache5EngineConfig.followRedirects)
     */
    public var followRedirects: Boolean = false

    /**
     * Specifies a maximum time (in milliseconds) of inactivity between two data packets when exchanging data with a server.
     *
     * Set this value to `0` to use an infinite timeout.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.apache5.Apache5EngineConfig.socketTimeout)
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
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.apache5.Apache5EngineConfig.socketTimeoutDuration)
     */
    @Suppress("DEPRECATION")
    public var socketTimeoutDuration: Duration
        get() = if (socketTimeout == 0) Duration.INFINITE else socketTimeout.milliseconds
        set(value) {
            socketTimeout = value.toMillisWithInfiniteAsZero().coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        }

    /**
     * Specifies a time period (in milliseconds) in which a client should establish a connection with a server.
     *
     * A `0` value represents an infinite timeout, while `-1` represents a system's default value.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.apache5.Apache5EngineConfig.connectTimeout)
     */
    @Deprecated(
        "Use connectTimeoutDuration with kotlin.time.Duration instead.",
        ReplaceWith("connectTimeoutDuration"),
        level = DeprecationLevel.WARNING
    )
    public var connectTimeout: Long = 10_000

    /**
     * Specifies a time period in which a client should establish a connection with a server.
     *
     * Use [Duration.INFINITE] for an infinite timeout, or `null` for the system's default value.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.apache5.Apache5EngineConfig.connectTimeoutDuration)
     */
    @Suppress("DEPRECATION")
    public var connectTimeoutDuration: Duration?
        get() = connectTimeout.toDurationWithZeroAsInfinite()
        set(value) {
            connectTimeout = value?.toMillisWithInfiniteAsZero() ?: -1L
        }

    /**
     * Specifies a time period (in milliseconds) in which a client should start a request.
     *
     * A `0` value represents an infinite timeout, while `-1` represents a system's default value.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.apache5.Apache5EngineConfig.connectionRequestTimeout)
     */
    @Deprecated(
        "Use connectionRequestTimeoutDuration with kotlin.time.Duration instead.",
        ReplaceWith("connectionRequestTimeoutDuration"),
        level = DeprecationLevel.WARNING
    )
    public var connectionRequestTimeout: Long = 20_000

    /**
     * Specifies a time period in which a client should start a request.
     *
     * Use [Duration.INFINITE] for an infinite timeout, or `null` for the system's default value.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.apache5.Apache5EngineConfig.connectionRequestTimeoutDuration)
     */
    @Suppress("DEPRECATION")
    public var connectionRequestTimeoutDuration: Duration?
        get() = connectionRequestTimeout.toDurationWithZeroAsInfinite()
        set(value) {
            connectionRequestTimeout = value?.toMillisWithInfiniteAsZero() ?: -1L
        }

    /**
     * Allows you to configure [SSL](https://ktor.io/docs/client-ssl.html) settings for this engine.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.apache5.Apache5EngineConfig.sslContext)
     */
    public var sslContext: SSLContext? = null

    /**
     * Specifies the policy for verifying hostnames during SSL/TLS connections.
     *
     * The policy determines when hostname verification occurs during the connection process:
     * - During TLS handshake (by JSSE)
     * - After TLS handshake (by HttpClient)
     * - Or both (default)
     *
     * Default value is [HostnameVerificationPolicy.BOTH] which provides maximum security
     * by performing verification at both stages.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.apache5.Apache5EngineConfig.sslHostnameVerificationPolicy)
     *
     * @see HostnameVerificationPolicy
     */
    public var sslHostnameVerificationPolicy: HostnameVerificationPolicy = HostnameVerificationPolicy.BOTH

    /**
     * Specifies the [DnsResolver] used by the Apache5 connection manager to look up IP addresses for hostnames.
     * When `null`, Apache HttpClient's default resolver is used.
     *
     * Set this to inject a custom resolver, for example to enable DNS-over-HTTPS or
     * to override host resolution in tests:
     * ```kotlin
     * install(Apache5) {
     *     engine {
     *         dnsResolver = SystemDefaultDnsResolver.INSTANCE
     *     }
     * }
     * ```
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.apache5.Apache5EngineConfig.dnsResolver)
     */
    public var dnsResolver: DnsResolver? = null

    internal var customRequest: (RequestConfig.Builder.() -> RequestConfig.Builder) = { this }

    internal var customClient: (HttpAsyncClientBuilder.() -> HttpAsyncClientBuilder) = { this }

    internal var configureConnectionManager: PoolingAsyncClientConnectionManagerBuilder.() -> Unit = {}

    /**
     * Customizes a [RequestConfig.Builder] in the specified [block].
     *
     * Apache's automatic response decompression is disabled after this block.
     * Use Ktor's `ContentEncoding` plugin to configure response decoding.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.apache5.Apache5EngineConfig.customizeRequest)
     */
    public fun customizeRequest(block: RequestConfig.Builder.() -> Unit) {
        val current = customRequest
        customRequest = { current().apply(block) }
    }

    /**
     * Customizes a [HttpAsyncClientBuilder] in the specified [block].
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.apache5.Apache5EngineConfig.customizeClient)
     */
    public fun customizeClient(block: HttpAsyncClientBuilder.() -> Unit) {
        val current = customClient
        customClient = { current().apply(block) }
    }

    /**
     * Configures the `AsyncClientConnectionManager` used by the Apache5 engine.
     *
     * We recommend using this function instead of `customizeClient { setConnectionManager(...) }`,
     * to avoid replacing the Ktor‑managed manager and inadvertently bypass engine settings and timeout mapping.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.apache5.Apache5EngineConfig.configureConnectionManager)
     */
    public fun configureConnectionManager(block: PoolingAsyncClientConnectionManagerBuilder.() -> Unit) {
        configureConnectionManager = { block() }
    }
}

private fun Duration.toMillisWithInfiniteAsZero(): Long = if (isInfinite()) 0L else inWholeMilliseconds

private fun Long.toDurationWithZeroAsInfinite(): Duration? = when {
    this < 0 -> null
    this == 0L -> Duration.INFINITE
    else -> milliseconds
}
