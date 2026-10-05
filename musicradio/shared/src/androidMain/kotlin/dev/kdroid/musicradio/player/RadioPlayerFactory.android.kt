package dev.kdroid.musicradio.player

import dev.kdroid.musicradio.platform.androidContext

actual fun createRadioPlayer(): RadioPlayer = MediaSessionRadioPlayer(androidContext())
