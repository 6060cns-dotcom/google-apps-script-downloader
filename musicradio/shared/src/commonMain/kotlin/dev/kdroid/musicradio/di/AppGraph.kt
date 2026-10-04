package dev.kdroid.musicradio.di

import dev.kdroid.musicradio.app.AppViewModel
import dev.kdroid.musicradio.data.AppStore
import dev.kdroid.musicradio.player.RadioPlayer
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.createGraph

@DependencyGraph(AppScope::class)
interface AppGraph {
    val viewModelFactory: AppViewModel.Factory
    val store: AppStore
    val player: RadioPlayer
}

fun createAppGraph(): AppGraph = createGraph()
