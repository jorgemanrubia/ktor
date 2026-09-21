/*
* Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.ktor.server.cio

import io.ktor.network.sockets.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Represents a server instance
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.cio.HttpServer)
 *
 * @property rootServerJob server job - root for all jobs
 * @property acceptJob client connections accepting job
 * @property serverSocket a deferred server socket instance, could be completed with error if it failed to bind
 */
public class HttpServer(
    public val rootServerJob: Job,
    public val acceptJob: Job,
    public val serverSocket: Deferred<ServerSocket>
)

/**
 * HTTP server connector settings
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.cio.HttpServerSettings)
 *
 * @property host to listen to
 * @property port to listen to
 * @property connectionIdleTimeoutSeconds time to live for IDLE connections
 * @property reuseAddress allow the server to bind to an address that is already in use
 */
public data class HttpServerSettings
@Deprecated(
    "Use the HttpServerSettings(host, port, connectionIdleTimeout, reuseAddress) factory with kotlin.time.Duration instead.",
    ReplaceWith(
        "HttpServerSettings(host, port, connectionIdleTimeoutSeconds.seconds, reuseAddress)",
        "kotlin.time.Duration.Companion.seconds"
    ),
    level = DeprecationLevel.WARNING
)
constructor(
    val host: String = "0.0.0.0",
    val port: Int = 8080,
    @Deprecated(
        "Use connectionIdleTimeout with kotlin.time.Duration instead.",
        ReplaceWith("connectionIdleTimeout"),
        level = DeprecationLevel.WARNING
    )
    val connectionIdleTimeoutSeconds: Long = 45,
    val reuseAddress: Boolean = false
) {
    /**
     * Creates settings with the default host, port and a 45 seconds idle timeout.
     */
    @Suppress("DEPRECATION")
    public constructor() : this(connectionIdleTimeoutSeconds = 45)

    /**
     * Time to live for IDLE connections.
     */
    @Suppress("DEPRECATION")
    public val connectionIdleTimeout: Duration
        get() = connectionIdleTimeoutSeconds.seconds
}

/**
 * Creates HTTP server connector settings.
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.cio.HttpServerSettings)
 *
 * @param host to listen to
 * @param port to listen to
 * @param connectionIdleTimeout time to live for IDLE connections
 * @param reuseAddress allow the server to bind to an address that is already in use
 */
@Suppress("DEPRECATION", "FunctionName")
public fun HttpServerSettings(
    host: String = "0.0.0.0",
    port: Int = 8080,
    connectionIdleTimeout: Duration,
    reuseAddress: Boolean = false
): HttpServerSettings = HttpServerSettings(host, port, connectionIdleTimeout.inWholeSeconds, reuseAddress)

/**
 * Represents the settings for a Unix-based HTTP server.
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.cio.UnixSocketServerSettings)
 *
 * @property connectionIdleTimeoutSeconds time to live for IDLE connections
 * @property socketPath the path to the Unix domain socket file used for the server communication.
 */
public class UnixSocketServerSettings
@Deprecated(
    "Use the UnixSocketServerSettings(socketPath, connectionIdleTimeout) factory with kotlin.time.Duration instead.",
    ReplaceWith(
        "UnixSocketServerSettings(socketPath, connectionIdleTimeoutSeconds.seconds)",
        "kotlin.time.Duration.Companion.seconds"
    ),
    level = DeprecationLevel.WARNING
)
constructor(
    public val socketPath: String,
    @Deprecated(
        "Use connectionIdleTimeout with kotlin.time.Duration instead.",
        ReplaceWith("connectionIdleTimeout"),
        level = DeprecationLevel.WARNING
    )
    public val connectionIdleTimeoutSeconds: Long = 45,
) {
    /**
     * Creates settings for the given [socketPath] with a 45 seconds idle timeout.
     */
    @Suppress("DEPRECATION")
    public constructor(socketPath: String) : this(socketPath, 45)

    /**
     * Time to live for IDLE connections.
     */
    @Suppress("DEPRECATION")
    public val connectionIdleTimeout: Duration
        get() = connectionIdleTimeoutSeconds.seconds
}

/**
 * Creates settings for a Unix-based HTTP server.
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.cio.UnixSocketServerSettings)
 *
 * @param socketPath the path to the Unix domain socket file used for the server communication.
 * @param connectionIdleTimeout time to live for IDLE connections
 */
@Suppress("DEPRECATION", "FunctionName")
public fun UnixSocketServerSettings(
    socketPath: String,
    connectionIdleTimeout: Duration,
): UnixSocketServerSettings = UnixSocketServerSettings(socketPath, connectionIdleTimeout.inWholeSeconds)
