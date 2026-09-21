/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.engine.android

import io.ktor.client.engine.*
import java.net.*
import javax.net.ssl.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * A configuration for the [Android] client engine.
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.android.AndroidEngineConfig)
 */
public class AndroidEngineConfig : HttpClientEngineConfig() {
    /**
     * Specifies a time period (in milliseconds) in which a client should establish a connection with a server.
     *
     * Set this value to `0` to use an infinite timeout.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.android.AndroidEngineConfig.connectTimeout)
     */
    @Deprecated(
        "Use connectTimeoutDuration with kotlin.time.Duration instead.",
        ReplaceWith("connectTimeoutDuration"),
        level = DeprecationLevel.WARNING
    )
    public var connectTimeout: Int = 100_000

    /**
     * Specifies a time period in which a client should establish a connection with a server.
     *
     * Use [Duration.INFINITE] for an infinite timeout.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.android.AndroidEngineConfig.connectTimeoutDuration)
     */
    @Suppress("DEPRECATION")
    public var connectTimeoutDuration: Duration
        get() = connectTimeout.toDurationWithZeroAsInfinite()
        set(value) {
            connectTimeout = value.toMillisWithInfiniteAsZero()
        }

    /**
     * Specifies a maximum time (in milliseconds) of inactivity between two data packets when exchanging data with a server.
     *
     * Set this value to `0` to use an infinite timeout.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.android.AndroidEngineConfig.socketTimeout)
     */
    @Deprecated(
        "Use socketTimeoutDuration with kotlin.time.Duration instead.",
        ReplaceWith("socketTimeoutDuration"),
        level = DeprecationLevel.WARNING
    )
    public var socketTimeout: Int = 100_000

    /**
     * Specifies a maximum time of inactivity between two data packets when exchanging data with a server.
     *
     * Use [Duration.INFINITE] for an infinite timeout.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.android.AndroidEngineConfig.socketTimeoutDuration)
     */
    @Suppress("DEPRECATION")
    public var socketTimeoutDuration: Duration
        get() = socketTimeout.toDurationWithZeroAsInfinite()
        set(value) {
            socketTimeout = value.toMillisWithInfiniteAsZero()
        }

    /**
     * Allows you to configure [HTTPS](https://ktor.io/docs/client-ssl.html) settings for this engine.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.android.AndroidEngineConfig.sslManager)
     */
    public var sslManager: (HttpsURLConnection) -> Unit = {}

    /**
     * Allows you to set engine-specific request configuration.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.client.engine.android.AndroidEngineConfig.requestConfig)
     */
    public var requestConfig: HttpURLConnection.() -> Unit = {}
}

private fun Duration.toMillisWithInfiniteAsZero(): Int =
    if (isInfinite()) 0 else inWholeMilliseconds.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()

private fun Int.toDurationWithZeroAsInfinite(): Duration = if (this == 0) Duration.INFINITE else milliseconds
