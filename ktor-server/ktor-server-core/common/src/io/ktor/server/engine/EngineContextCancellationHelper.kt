/*
* Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.ktor.server.engine

import io.ktor.server.application.*
import io.ktor.server.engine.internal.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Stop server on job cancellation with the default grace period of 50 milliseconds and timeout of 5 seconds.
 * The returned [CompletableJob] needs to be completed or cancelled.
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.engine.stopServerOnCancellation)
 */
public fun ApplicationEngine.stopServerOnCancellation(application: Application): CompletableJob =
    stopServerOnCancellation(application, 50.milliseconds, 5.seconds)

/**
 * Stop server on job cancellation. The returned [CompletableJob] needs to be completed or cancelled.
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.engine.stopServerOnCancellation)
 *
 * @param gracePeriod the maximum amount of time for activity to cool down
 * @param timeout the maximum amount of time to wait until a server stops gracefully
 */
@OptIn(InternalAPI::class)
public fun ApplicationEngine.stopServerOnCancellation(
    application: Application,
    gracePeriod: Duration,
    timeout: Duration = 5.seconds
): CompletableJob =
    application.parentCoroutineContext[Job]?.launchOnCancellation {
        stop(gracePeriod, timeout)
    } ?: Job()

/**
 * Stop server on job cancellation. The returned [CompletableJob] needs to be completed or cancelled.
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.engine.stopServerOnCancellation)
 */
@Deprecated(
    "Use stopServerOnCancellation(application, gracePeriod, timeout) with kotlin.time.Duration instead.",
    ReplaceWith(
        "stopServerOnCancellation(application, gracePeriodMillis.milliseconds, timeoutMillis.milliseconds)",
        "kotlin.time.Duration.Companion.milliseconds"
    ),
    level = DeprecationLevel.WARNING
)
public fun ApplicationEngine.stopServerOnCancellation(
    application: Application,
    gracePeriodMillis: Long = 50,
    timeoutMillis: Long = 5000
): CompletableJob = stopServerOnCancellation(application, gracePeriodMillis.milliseconds, timeoutMillis.milliseconds)

/**
 * Launch a coroutine with [block] body when either the parent job or the returned job is cancelled.
 * It is important to complete or cancel the returned [CompletableJob]
 * otherwise the parent job will be unable to complete successfully.
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.engine.launchOnCancellation)
 */
@OptIn(DelicateCoroutinesApi::class)
@InternalAPI
public fun Job.launchOnCancellation(block: suspend () -> Unit): CompletableJob {
    val completableJob: CompletableJob = Job(parent = this)

    GlobalScope.launch(this + Dispatchers.IOBridge + CoroutineName("cancellation-watcher")) {
        var cancelled = false
        try {
            completableJob.join()
        } catch (_: Throwable) {
            cancelled = true
        }

        if (cancelled || completableJob.isCancelled) {
            block()
        }
    }

    return completableJob
}
