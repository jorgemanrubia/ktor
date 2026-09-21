/*
 * Copyright 2014-2026 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.server.engine

import io.ktor.events.*
import io.ktor.server.application.*
import io.ktor.util.logging.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration

/**
 * Represents an embedded server that hosts an application.
 * It's an entry point to the application and handles the lifecycle of the application engine.
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.engine.EmbeddedServer)
 *
 * @param TEngine The type of the application engine used by the server.
 * @param TConfiguration The type of the configuration used by the engine.
 */
public expect class EmbeddedServer<TEngine : ApplicationEngine, TConfiguration : ApplicationEngine.Configuration>(
    rootConfig: ServerConfig,
    engineFactory: ApplicationEngineFactory<TEngine, TConfiguration>,
    engineConfigBlock: TConfiguration.() -> Unit = {}
) {
    /**
     * Provides events on Application lifecycle
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.engine.EmbeddedServer.monitor)
     */
    public val monitor: Events

    public val environment: ApplicationEnvironment
    public val application: Application
    public val engine: TEngine
    public val engineConfig: TConfiguration

    public fun start(wait: Boolean = false): EmbeddedServer<TEngine, TConfiguration>

    public suspend fun startSuspend(wait: Boolean = false): EmbeddedServer<TEngine, TConfiguration>

    /**
     * Stops this server using the grace period and timeout from the engine configuration.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.engine.EmbeddedServer.stop)
     */
    public fun stop()

    /**
     * Stops this server.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.engine.EmbeddedServer.stop)
     *
     * @param gracePeriod the maximum amount of time for activity to cool down
     * @param timeout the maximum amount of time to wait until a server stops gracefully
     */
    public fun stop(
        gracePeriod: Duration = engineConfig.shutdownGracePeriodDuration,
        timeout: Duration = engineConfig.shutdownTimeoutDuration
    )

    @Deprecated(
        "Use stop(gracePeriod, timeout) with kotlin.time.Duration instead.",
        ReplaceWith(
            "stop(gracePeriodMillis.milliseconds, timeoutMillis.milliseconds)",
            "kotlin.time.Duration.Companion.milliseconds"
        ),
        level = DeprecationLevel.WARNING
    )
    @Suppress("DEPRECATION")
    public fun stop(
        gracePeriodMillis: Long = engineConfig.shutdownGracePeriod,
        timeoutMillis: Long = engineConfig.shutdownTimeout
    )

    /**
     * Stops this server using the grace period and timeout from the engine configuration.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.engine.EmbeddedServer.stopSuspend)
     */
    public suspend fun stopSuspend()

    /**
     * Stops this server.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.engine.EmbeddedServer.stopSuspend)
     *
     * @param gracePeriod the maximum amount of time for activity to cool down
     * @param timeout the maximum amount of time to wait until a server stops gracefully
     */
    public suspend fun stopSuspend(
        gracePeriod: Duration = engineConfig.shutdownGracePeriodDuration,
        timeout: Duration = engineConfig.shutdownTimeoutDuration
    )

    @Deprecated(
        "Use stopSuspend(gracePeriod, timeout) with kotlin.time.Duration instead.",
        ReplaceWith(
            "stopSuspend(gracePeriodMillis.milliseconds, timeoutMillis.milliseconds)",
            "kotlin.time.Duration.Companion.milliseconds"
        ),
        level = DeprecationLevel.WARNING
    )
    @Suppress("DEPRECATION")
    public suspend fun stopSuspend(
        gracePeriodMillis: Long = engineConfig.shutdownGracePeriod,
        timeoutMillis: Long = engineConfig.shutdownTimeout
    )
}

/**
 * Factory interface for creating [ApplicationEngine] instances.
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.engine.ApplicationEngineFactory)
 */
public interface ApplicationEngineFactory<
    out TEngine : ApplicationEngine,
    TConfiguration : ApplicationEngine.Configuration
    > {

    public fun configuration(configure: TConfiguration.() -> Unit): TConfiguration

    /**
     * Creates an engine from the given [environment] and [configuration].
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.engine.ApplicationEngineFactory.create)
     */
    public fun create(
        environment: ApplicationEnvironment,
        monitor: Events,
        developmentMode: Boolean,
        configuration: TConfiguration,
        applicationProvider: () -> Application
    ): TEngine
}

/**
 * Creates an embedded server with the given [factory], listening on [host]:[port].
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.engine.embeddedServer)
 */
@OptIn(DelicateCoroutinesApi::class)
@Deprecated("Replaced with suspend function parameter", level = DeprecationLevel.HIDDEN)
public fun <TEngine : ApplicationEngine, TConfiguration : ApplicationEngine.Configuration> embeddedServer(
    factory: ApplicationEngineFactory<TEngine, TConfiguration>,
    port: Int = 80,
    host: String = "0.0.0.0",
    watchPaths: List<String> = listOf(WORKING_DIRECTORY_PATH),
    module: Application.() -> Unit
): EmbeddedServer<TEngine, TConfiguration> =
    GlobalScope.embeddedServer(factory, port, host, watchPaths, module = module)

/**
 * Creates an embedded server with the given [factory], listening on [host]:[port].
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.engine.embeddedServer)
 */
@OptIn(DelicateCoroutinesApi::class)
public fun <TEngine : ApplicationEngine, TConfiguration : ApplicationEngine.Configuration> embeddedServer(
    factory: ApplicationEngineFactory<TEngine, TConfiguration>,
    port: Int = 80,
    host: String = "0.0.0.0",
    watchPaths: List<String> = listOf(WORKING_DIRECTORY_PATH),
    module: suspend Application.() -> Unit
): EmbeddedServer<TEngine, TConfiguration> =
    GlobalScope.embeddedServer(factory, port, host, watchPaths, module = module)

/**
 * Creates an embedded server with the given [factory], listening on [host]:[port].
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.engine.embeddedServer)
 */
@Suppress("ktlint:standard:max-line-length")
@Deprecated("Replaced with suspend function parameter", level = DeprecationLevel.HIDDEN)
public fun <TEngine : ApplicationEngine, TConfiguration : ApplicationEngine.Configuration> CoroutineScope.embeddedServer(
    factory: ApplicationEngineFactory<TEngine, TConfiguration>,
    port: Int = 80,
    host: String = "0.0.0.0",
    watchPaths: List<String> = listOf(WORKING_DIRECTORY_PATH),
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext,
    module: Application.() -> Unit
): EmbeddedServer<TEngine, TConfiguration> {
    val connectors: Array<EngineConnectorConfig> = arrayOf(
        EngineConnectorBuilder().apply {
            this.port = port
            this.host = host
        }
    )
    return embeddedServer(
        factory = factory,
        connectors = connectors,
        watchPaths = watchPaths,
        parentCoroutineContext = parentCoroutineContext,
        module = module
    )
}

/**
 * Creates an embedded server with the given [factory], listening on [host]:[port].
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.engine.embeddedServer)
 */
@Suppress("ktlint:standard:max-line-length")
public fun <TEngine : ApplicationEngine, TConfiguration : ApplicationEngine.Configuration> CoroutineScope.embeddedServer(
    factory: ApplicationEngineFactory<TEngine, TConfiguration>,
    port: Int = 80,
    host: String = "0.0.0.0",
    watchPaths: List<String> = listOf(WORKING_DIRECTORY_PATH),
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext,
    module: suspend Application.() -> Unit
): EmbeddedServer<TEngine, TConfiguration> {
    val connectors: Array<EngineConnectorConfig> = arrayOf(
        EngineConnectorBuilder().apply {
            this.port = port
            this.host = host
        }
    )
    return embeddedServer(
        factory = factory,
        connectors = connectors,
        watchPaths = watchPaths,
        parentCoroutineContext = parentCoroutineContext,
        module = module
    )
}

/**
 * Creates an embedded server with the given [factory], listening on given [connectors].
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.engine.embeddedServer)
 */
@Suppress("ktlint:standard:max-line-length")
@Deprecated("Replaced with suspend function parameter", level = DeprecationLevel.HIDDEN)
public fun <TEngine : ApplicationEngine, TConfiguration : ApplicationEngine.Configuration> CoroutineScope.embeddedServer(
    factory: ApplicationEngineFactory<TEngine, TConfiguration>,
    vararg connectors: EngineConnectorConfig = arrayOf(),
    watchPaths: List<String> = listOf(WORKING_DIRECTORY_PATH),
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext,
    module: Application.() -> Unit
): EmbeddedServer<TEngine, TConfiguration> {
    val environment = applicationEnvironment {
        this.log = KtorSimpleLogger("io.ktor.server.Application")
    }
    val applicationProperties = serverConfig(environment) {
        this.parentCoroutineContext = coroutineContext + parentCoroutineContext
        this.watchPaths = watchPaths
        this.module(module)
    }
    val config: TConfiguration.() -> Unit = {
        this.connectors.addAll(connectors)
    }

    return embeddedServer(factory, applicationProperties, config)
}

/**
 * Creates an embedded server with the given [factory], listening on given [connectors].
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.engine.embeddedServer)
 */
@Suppress("ktlint:standard:max-line-length")
public fun <TEngine : ApplicationEngine, TConfiguration : ApplicationEngine.Configuration> CoroutineScope.embeddedServer(
    factory: ApplicationEngineFactory<TEngine, TConfiguration>,
    vararg connectors: EngineConnectorConfig = arrayOf(),
    watchPaths: List<String> = listOf(WORKING_DIRECTORY_PATH),
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext,
    module: suspend Application.() -> Unit
): EmbeddedServer<TEngine, TConfiguration> {
    val environment = applicationEnvironment {
        this.log = KtorSimpleLogger("io.ktor.server.Application")
    }
    val applicationProperties = serverConfig(environment) {
        this.parentCoroutineContext = coroutineContext + parentCoroutineContext
        this.watchPaths = watchPaths
        this.module(module)
    }
    val config: TConfiguration.() -> Unit = {
        this.connectors.addAll(connectors)
    }

    return embeddedServer(factory, applicationProperties, config)
}

/**
 * Creates an embedded server with the given [factory], [environment] and [configure] script.
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.engine.embeddedServer)
 */
@Deprecated("Replaced with suspend function parameter", level = DeprecationLevel.HIDDEN)
public fun <TEngine : ApplicationEngine, TConfiguration : ApplicationEngine.Configuration> embeddedServer(
    factory: ApplicationEngineFactory<TEngine, TConfiguration>,
    environment: ApplicationEnvironment = applicationEnvironment(),
    configure: TConfiguration.() -> Unit = {},
    module: Application.() -> Unit = {}
): EmbeddedServer<TEngine, TConfiguration> {
    val applicationProperties = serverConfig(environment) {
        module(body = module)
    }
    return embeddedServer(factory, applicationProperties, configure)
}

/**
 * Creates an embedded server with the given [factory], [environment] and [configure] script.
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.engine.embeddedServer)
 */
public fun <TEngine : ApplicationEngine, TConfiguration : ApplicationEngine.Configuration> embeddedServer(
    factory: ApplicationEngineFactory<TEngine, TConfiguration>,
    environment: ApplicationEnvironment = applicationEnvironment(),
    configure: TConfiguration.() -> Unit = {},
    module: suspend Application.() -> Unit = {}
): EmbeddedServer<TEngine, TConfiguration> {
    val applicationProperties = serverConfig(environment) {
        module(body = module)
    }
    return embeddedServer(factory, applicationProperties, configure)
}

/**
 * Creates an embedded server with the given [factory], [environment] and [configure] script.
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.engine.embeddedServer)
 */
public fun <TEngine : ApplicationEngine, TConfiguration : ApplicationEngine.Configuration> embeddedServer(
    factory: ApplicationEngineFactory<TEngine, TConfiguration>,
    rootConfig: ServerConfig,
    configure: TConfiguration.() -> Unit = {}
): EmbeddedServer<TEngine, TConfiguration> {
    return EmbeddedServer(rootConfig, factory, configure)
}
