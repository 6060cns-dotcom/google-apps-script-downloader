package dev.kdroid.musicradio.player

import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

/**
 * Holds the one ExoPlayer the app ever creates, wrapped in a [MediaSession].
 *
 * Playback lives in a service rather than in the activity for the reason radio needs most: the
 * screen going off must not stop the sound. The session is what gives Android the notification,
 * the lock screen controls and the hardware media keys, all of it for free.
 */
class PlaybackService : MediaSessionService() {

    private var session: MediaSession? = null

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this)
            // The second argument asks ExoPlayer to take audio focus, so a call or another
            // player pauses the radio and it resumes afterwards instead of talking over them.
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true,
            )
            // Unplugging headphones pauses rather than blasting the speaker.
            .setHandleAudioBecomingNoisy(true)
            // Keeps CPU and wifi awake while playing; without it a live stream dies moments
            // after the screen turns off.
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .build()
        // ICY declares no charset, so ExoPlayer reads the Hebrew stations that send windows-1255
        // as UTF-8 and produces mojibake, which then overrides whatever the app set. The app
        // decodes those titles correctly itself, so the in-stream metadata track is switched off
        // and the notification shows only what the app supplies.
        player.trackSelectionParameters = player.trackSelectionParameters
            .buildUpon()
            .setTrackTypeDisabled(C.TRACK_TYPE_METADATA, true)
            .build()
        session = MediaSession.Builder(this, LiveAwarePlayer(player)).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    /** Swiping the app away should stop the radio, but only when it is not actually playing. */
    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = session?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        session?.run {
            player.release()
            release()
        }
        session = null
        super.onDestroy()
    }
}

/**
 * Makes "play" mean *live* again on a source that has no position.
 *
 * ExoPlayer resumes a paused source out of its buffer, which is as old as the pause: pausing
 * through an ad break and coming back minutes later carried on in the middle of the ads. The app's
 * own play button asks for the URL again, but the notification, the lock screen and Android Auto
 * talk to this session directly and never reach the app - so the reset has to live on the player
 * the session actually holds.
 *
 * Only a source sitting paused and ready is reset. The app's own path prepares first and arrives
 * here while still buffering, so it is not prepared twice.
 */
private class LiveAwarePlayer(player: Player) : ForwardingPlayer(player) {
    override fun play() {
        // isCurrentMediaItemSeekable is false for exactly the sources with nothing to seek to -
        // an endless Icecast stream. Anything that reports a duration is left alone.
        if (playbackState == Player.STATE_READY && !playWhenReady && mediaItemCount > 0 && !isCurrentMediaItemSeekable) {
            stop()
            seekToDefaultPosition()
            prepare()
        }
        super.play()
    }
}
