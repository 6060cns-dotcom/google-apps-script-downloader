package dev.kdroid.musicradio.platform

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig

/**
 * Every target but the desktop already resolves certificates the way the rest of the machine does:
 * Android goes through the app's network security config, and the browser never hands the decision
 * to the page at all. The JDK is the exception - it trusts its own bundled `cacerts` and ignores
 * the operating system's store - so a filtered line whose root the user installed once, and which
 * the browser and every other app on the machine accept, still fails the handshake there.
 *
 * The metadata poll is the only thing in the app that goes out over this client, and it swallows
 * its own failures, so the symptom was a track title that was simply never there.
 */
internal expect fun createHttpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient
