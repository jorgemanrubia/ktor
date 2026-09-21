/*
 * Copyright 2014-2025 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.plugins

import io.ktor.client.engine.*
import io.ktor.client.network.sockets.*
import io.ktor.client.plugins.api.*
import io.ktor.client.plugins.sse.*
import io.ktor.client.request.*
import io.ktor.client.utils.*
import io.ktor.http.*
import io.ktor.util.*
import io.ktor.util.logging.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private val LOGGER = KtorSimpleLogger("io.ktor.client.plugins.HttpTimeout")

/**
 * An [HttpTimeout] extension configuration that is used during installation.
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.plugins.HttpTimeoutConfig)
 */
@KtorDsl
public class HttpTimeoutConfig {

    private var _requestTimeout: Duration? = null
    private var _connectTimeout: Duration? = null
    private var _socketTimeout: Duration? = null

    /**
     * Creates a new instance of [HttpTimeoutConfig] with no timeouts configured.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.plugins.HttpTimeoutConfig.HttpTimeoutConfig)
     */
    public constructor()

    /**
     * Creates a new instance of [HttpTimeoutConfig].
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.plugins.HttpTimeoutConfig.HttpTimeoutConfig)
     *
     * @param requestTimeout a request timeout or `null` if not configured.
     * @param connectTimeout a connection timeout or `null` if not configured.
     * @param socketTimeout a socket timeout or `null` if not configured.
     */
    public constructor(
        requestTimeout: Duration?,
        connectTimeout: Duration? = null,
        socketTimeout: Duration? = null
    ) {
        this.requestTimeout = requestTimeout
        this.connectTimeout = connectTimeout
        this.socketTimeout = socketTimeout
    }

    /**
     * Creates a new instance of [HttpTimeoutConfig].
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.plugins.HttpTimeoutConfig.HttpTimeoutConfig)
     */
    @Deprecated(
        "Use the constructor with kotlin.time.Duration instead.",
        ReplaceWith(
            "HttpTimeoutConfig(" +
                "requestTimeoutMillis?.milliseconds, " +
                "connectTimeoutMillis?.milliseconds, " +
                "socketTimeoutMillis?.milliseconds" +
                ")",
            "kotlin.time.Duration.Companion.milliseconds"
        ),
        level = DeprecationLevel.WARNING
    )
    public constructor(
        requestTimeoutMillis: Long? = null,
        connectTimeoutMillis: Long? = null,
        socketTimeoutMillis: Long? = null
    ) {
        this.requestTimeout = requestTimeoutMillis?.milliseconds
        this.connectTimeout = connectTimeoutMillis?.milliseconds
        this.socketTimeout = socketTimeoutMillis?.milliseconds
    }

    /**
     * Specifies a request timeout.
     * The request timeout is the time period required to process an HTTP call: from sending a request to receiving a
     * response.
     *
     * Use [Duration.INFINITE] for an infinite timeout, or `null` to leave the timeout unspecified.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.plugins.HttpTimeoutConfig.requestTimeout)
     */
    public var requestTimeout: Duration?
        get() = _requestTimeout
        set(value) {
            _requestTimeout = checkTimeoutValue(value)
        }

    /**
     * Specifies a connection timeout.
     * The connection timeout is the time period in which a client should establish a connection with a server.
     *
     * Use [Duration.INFINITE] for an infinite timeout, or `null` to leave the timeout unspecified.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.plugins.HttpTimeoutConfig.connectTimeout)
     */
    public var connectTimeout: Duration?
        get() = _connectTimeout
        set(value) {
            _connectTimeout = checkTimeoutValue(value)
        }

    /**
     * Specifies a socket timeout (read and write).
     * The socket timeout is the maximum time of inactivity between two data packets when exchanging data with a server.
     *
     * Use [Duration.INFINITE] for an infinite timeout, or `null` to leave the timeout unspecified.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.plugins.HttpTimeoutConfig.socketTimeout)
     */
    public var socketTimeout: Duration?
        get() = _socketTimeout
        set(value) {
            _socketTimeout = checkTimeoutValue(value)
        }

    /**
     * Specifies a request timeout in milliseconds.
     * The request timeout is the time period required to process an HTTP call: from sending a request to receiving a
     * response.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.plugins.HttpTimeoutConfig.requestTimeoutMillis)
     */
    @Deprecated(
        "Use requestTimeout with kotlin.time.Duration instead.",
        ReplaceWith("requestTimeout"),
        level = DeprecationLevel.WARNING
    )
    public var requestTimeoutMillis: Long?
        get() = _requestTimeout?.inWholeMilliseconds
        set(value) {
            requestTimeout = value?.milliseconds
        }

    /**
     * Specifies a connection timeout in milliseconds.
     * The connection timeout is the time period in which a client should establish a connection with a server.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.plugins.HttpTimeoutConfig.connectTimeoutMillis)
     */
    @Deprecated(
        "Use connectTimeout with kotlin.time.Duration instead.",
        ReplaceWith("connectTimeout"),
        level = DeprecationLevel.WARNING
    )
    public var connectTimeoutMillis: Long?
        get() = _connectTimeout?.inWholeMilliseconds
        set(value) {
            connectTimeout = value?.milliseconds
        }

    /**
     * Specifies a socket timeout (read and write) in milliseconds.
     * The socket timeout is the maximum time of inactivity between two data packets when exchanging data with a server.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.plugins.HttpTimeoutConfig.socketTimeoutMillis)
     */
    @Deprecated(
        "Use socketTimeout with kotlin.time.Duration instead.",
        ReplaceWith("socketTimeout"),
        level = DeprecationLevel.WARNING
    )
    public var socketTimeoutMillis: Long?
        get() = _socketTimeout?.inWholeMilliseconds
        set(value) {
            socketTimeout = value?.milliseconds
        }

    private fun checkTimeoutValue(value: Duration?): Duration? {
        require(value == null || value.isPositive()) {
            "Only positive timeout values are allowed, for infinite timeout use Duration.INFINITE"
        }
        return value
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as HttpTimeoutConfig

        if (_requestTimeout != other._requestTimeout) return false
        if (_connectTimeout != other._connectTimeout) return false
        if (_socketTimeout != other._socketTimeout) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _requestTimeout.hashCode()
        result = 31 * result + _connectTimeout.hashCode()
        result = 31 * result + _socketTimeout.hashCode()
        return result
    }

    public companion object {
        public const val INFINITE_TIMEOUT_MS: Long = Long.MAX_VALUE
        public val key: AttributeKey<HttpTimeoutConfig> = AttributeKey("TimeoutConfiguration")
    }
}

public data object HttpTimeoutCapability : HttpClientEngineCapability<HttpTimeoutConfig>

/**
 * A plugin that allows you to configure the following timeouts:
 * - __request timeout__ — a time period required to process an HTTP call: from sending a request to receiving
 * a response.
 * - __connection timeout__ — a time period in which a client should establish a connection with a server.
 * - __socket timeout__ — a maximum time of inactivity between two data packets when exchanging data with a server.
 *
 * You can learn more from [Timeout](https://ktor.io/docs/timeout.html).
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.plugins.HttpTimeout)
 */
public val HttpTimeout: ClientPlugin<HttpTimeoutConfig> = createClientPlugin(
    "HttpTimeout",
    ::HttpTimeoutConfig
) {
    val requestTimeout: Duration? = pluginConfig.requestTimeout
    val connectTimeout: Duration? = pluginConfig.connectTimeout
    val socketTimeout: Duration? = pluginConfig.socketTimeout

    /**
     * Utils method that return `true` if at least one timeout is configured (has not null value).
     */
    fun hasNotNullTimeouts(supportsRequestTimeout: Boolean) =
        (supportsRequestTimeout && requestTimeout != null) ||
            connectTimeout != null ||
            socketTimeout != null

    on(Send) { request ->
        val supportsRequestTimeout = request.supportsRequestTimeout
        var configuration = request.getCapabilityOrNull(HttpTimeoutCapability)
        if (configuration == null && hasNotNullTimeouts(supportsRequestTimeout)) {
            configuration = HttpTimeoutConfig()
            request.setCapability(HttpTimeoutCapability, configuration)
        }

        configuration?.apply {
            this.connectTimeout = this.connectTimeout ?: connectTimeout
            this.socketTimeout = this.socketTimeout ?: socketTimeout

            if (supportsRequestTimeout) {
                this.requestTimeout = this.requestTimeout ?: requestTimeout
                applyRequestTimeout(request, this.requestTimeout)
            }
        }
        proceed(request)
    }
}

/** Request timeout shouldn't be applied to WebSocket or SSE connection. */
@OptIn(InternalAPI::class)
private val HttpRequestBuilder.supportsRequestTimeout: Boolean
    get() = !url.protocol.isWebsocket() &&
        body !is ClientUpgradeContent &&
        body !is SSEClientContent

private fun CoroutineScope.applyRequestTimeout(request: HttpRequestBuilder, requestTimeout: Duration?) {
    if (requestTimeout == null || requestTimeout.isInfinite()) return

    val executionContext = request.executionContext
    val killer = launch(CoroutineName("request-timeout")) {
        delay(requestTimeout)
        val cause = HttpRequestTimeoutException(request)
        LOGGER.trace { "Request timeout: ${request.url}" }
        executionContext.cancel(cause.message!!, cause)
    }

    request.executionContext.invokeOnCompletion {
        killer.cancel()
    }
}

/**
 * Adds timeout boundaries to the request. Requires the [HttpTimeout] plugin to be installed.
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.plugins.timeout)
 */
public fun HttpRequestBuilder.timeout(block: HttpTimeoutConfig.() -> Unit): Unit =
    setCapability(HttpTimeoutCapability, HttpTimeoutConfig().apply(block))

/**
 * This exception is thrown in case the request timeout is exceeded.
 * The request timeout is the time period required to process an HTTP call: from sending a request to receiving
 * a response.
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.plugins.HttpRequestTimeoutException)
 */
@OptIn(ExperimentalCoroutinesApi::class)
public class HttpRequestTimeoutException(
    private val url: String,
    private val timeoutMillis: Long?,
    cause: Throwable? = null
) : IOException("Request timeout has expired [url=$url, request_timeout=${timeoutMillis ?: "unknown"} ms]", cause),
    CopyableThrowable<HttpRequestTimeoutException> {

    public constructor(request: HttpRequestBuilder) : this(
        request.url.buildString(),
        request.getCapabilityOrNull(HttpTimeoutCapability)?.requestTimeout?.inWholeMilliseconds
    )

    public constructor(request: HttpRequestData) : this(
        request.url.toString(),
        request.getCapabilityOrNull(HttpTimeoutCapability)?.requestTimeout?.inWholeMilliseconds
    )

    override fun createCopy(): HttpRequestTimeoutException {
        return HttpRequestTimeoutException(url, timeoutMillis, cause)
    }
}

/**
 * This exception is thrown in case the connection timeout is exceeded.
 * It indicates the client took too long to establish a connection with a server.
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.plugins.ConnectTimeoutException)
 */
public fun ConnectTimeoutException(
    request: HttpRequestData,
    cause: Throwable? = null
): ConnectTimeoutException = ConnectTimeoutException(
    "Connect timeout has expired [url=${request.url}, " +
        "connect_timeout=${
            request.getCapabilityOrNull(HttpTimeoutCapability)?.connectTimeout?.inWholeMilliseconds ?: "unknown"
        } ms]",
    cause
)

/**
 * This exception is thrown in case the connection timeout is exceeded.
 * It indicates the client took too long to establish a connection with a server.
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.plugins.ConnectTimeoutException)
 */
public fun ConnectTimeoutException(
    url: String,
    timeout: Long?,
    cause: Throwable? = null
): ConnectTimeoutException = ConnectTimeoutException(
    "Connect timeout has expired [url=$url, connect_timeout=${timeout ?: "unknown"} ms]",
    cause
)

/**
 * This exception is thrown in case the socket timeout (read or write) is exceeded.
 * It indicates the time between two data packets when exchanging data with a server was too long.
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.plugins.SocketTimeoutException)
 */
public fun SocketTimeoutException(
    request: HttpRequestData,
    cause: Throwable? = null
): SocketTimeoutException = SocketTimeoutException(
    "Socket timeout has expired [url=${request.url}, " +
        "socket_timeout=${
            request.getCapabilityOrNull(HttpTimeoutCapability)?.socketTimeout?.inWholeMilliseconds ?: "unknown"
        }] ms",
    cause
)

/**
 * Converts a long timeout in milliseconds to int value. To do that, we need to consider [HttpTimeoutConfig.INFINITE_TIMEOUT_MS]
 * as zero and convert timeout value to [Int].
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.plugins.convertLongTimeoutToIntWithInfiniteAsZero)
 */
@InternalAPI
public fun convertLongTimeoutToIntWithInfiniteAsZero(timeout: Long): Int = when {
    timeout == HttpTimeoutConfig.INFINITE_TIMEOUT_MS -> 0
    timeout < Int.MIN_VALUE -> Int.MIN_VALUE
    timeout > Int.MAX_VALUE -> Int.MAX_VALUE
    else -> timeout.toInt()
}

/**
 * Converts long timeout in milliseconds to long value. To do that, we need to consider [HttpTimeoutConfig.INFINITE_TIMEOUT_MS]
 * as zero and convert timeout value to [Long].
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.plugins.convertLongTimeoutToLongWithInfiniteAsZero)
 */
@InternalAPI
public fun convertLongTimeoutToLongWithInfiniteAsZero(timeout: Long): Long = when (timeout) {
    HttpTimeoutConfig.INFINITE_TIMEOUT_MS -> 0L
    else -> timeout
}

@PublishedApi
internal inline fun <T> unwrapRequestTimeoutException(block: () -> T): T {
    try {
        return block()
    } catch (cause: CancellationException) {
        throw cause.unwrapCancellationException()
    }
}
