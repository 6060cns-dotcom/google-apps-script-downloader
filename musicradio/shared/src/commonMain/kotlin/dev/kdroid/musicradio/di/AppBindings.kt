package dev.kdroid.musicradio.di

import dev.kdroid.musicradio.platform.IoDispatcher
import dev.kdroid.musicradio.platform.createHttpClient
import dev.kdroid.musicradio.player.IcyMetadata
import dev.kdroid.musicradio.player.MediaControls
import dev.kdroid.musicradio.player.RadioPlayer
import dev.kdroid.musicradio.player.createMediaControls
import dev.kdroid.musicradio.player.createRadioPlayer
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@ContributesTo(AppScope::class)
@BindingContainer
object AppBindings {
    @Provides
    fun provideDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @Io
    fun provideIoDispatcher(): CoroutineDispatcher = IoDispatcher

    // One backend per process: two live decoders would fight over the audio device.
    @Provides
    @SingleIn(AppScope::class)
    fun provideRadioPlayer(): RadioPlayer = createRadioPlayer()

    // Likewise one registration per process: the OS media center keys on the app, not the window.
    @Provides
    @SingleIn(AppScope::class)
    fun provideMediaControls(): MediaControls = createMediaControls()

    @Provides
    @SingleIn(AppScope::class)
    fun provideHttpClient(): HttpClient = createHttpClient {
        // Metadata is a nicety: a station that stalls must never hold a request open for long.
        install(HttpTimeout) {
            connectTimeoutMillis = 10_000
            requestTimeoutMillis = 20_000
            socketTimeoutMillis = 15_000
        }
        expectSuccess = false
    }

    @Provides
    @SingleIn(AppScope::class)
    fun provideIcyMetadata(http: HttpClient): IcyMetadata = IcyMetadata(http)
}
