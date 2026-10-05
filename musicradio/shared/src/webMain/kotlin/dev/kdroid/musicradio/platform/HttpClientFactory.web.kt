package dev.kdroid.musicradio.platform

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig

/** The browser owns the trust store; a request that it refuses never reaches this code. */
internal actual fun createHttpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient = HttpClient(config)
