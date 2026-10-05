package dev.kdroid.musicradio.player

/**
 * The two platforms need genuinely different backends: desktop plays through Compose Media
 * Player's Rodio engine, while Android needs an ExoPlayer the app itself owns so it can be
 * attached to a media session.
 */
expect fun createRadioPlayer(): RadioPlayer
