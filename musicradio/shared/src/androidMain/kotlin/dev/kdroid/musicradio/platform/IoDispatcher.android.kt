package dev.kdroid.musicradio.platform

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal actual val IoDispatcher: CoroutineDispatcher = Dispatchers.IO
