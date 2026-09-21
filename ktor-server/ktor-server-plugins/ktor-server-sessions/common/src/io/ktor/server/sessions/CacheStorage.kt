/*
 * Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.server.sessions

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * A caching storage for sessions.
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.sessions.CacheStorage)
 */
public class CacheStorage
@Deprecated(
    "Use the CacheStorage(delegate, idleTimeout) factory with kotlin.time.Duration instead.",
    ReplaceWith(
        "CacheStorage(delegate, idleTimeout.milliseconds)",
        "kotlin.time.Duration.Companion.milliseconds"
    ),
    level = DeprecationLevel.WARNING
)
constructor(
    public val delegate: SessionStorage,
    idleTimeout: Long,
) : SessionStorage {

    private val cache = platformCache(delegate, idleTimeout)

    override suspend fun read(id: String): String {
        return cache.getOrCompute(id)
    }

    override suspend fun write(id: String, value: String) {
        val cachedValue = try {
            read(id)
        } catch (_: Throwable) {
            null
        }
        if (cachedValue == value) return
        cache.invalidate(id)
        delegate.write(id, value)
    }

    override suspend fun invalidate(id: String) {
        cache.invalidate(id)
        delegate.invalidate(id)
    }
}

/**
 * Creates a caching storage for sessions.
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.sessions.CacheStorage)
 *
 * @param delegate the underlying storage.
 * @param idleTimeout the time after which an unused cached entry is evicted.
 */
@Suppress("DEPRECATION", "FunctionName")
public fun CacheStorage(delegate: SessionStorage, idleTimeout: Duration): CacheStorage =
    CacheStorage(delegate, idleTimeout.inWholeMilliseconds)

internal expect fun platformCache(delegate: SessionStorage, idleTimeout: Long): Cache<String, String>
