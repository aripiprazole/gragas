/*
 * The contents of this file is free and unencumbered software released into the
 * public domain. For more information, please refer to <http://unlicense.org/>
 */

@file:OptIn(KordVoice::class, ObsoleteCoroutinesApi::class)

package gragas.play

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer
import dev.kord.common.annotation.KordVoice
import dev.kord.core.behavior.channel.connect
import dev.kord.core.entity.channel.VoiceChannel
import dev.kord.voice.VoiceConnection
import kotlin.Result.Companion.failure
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import mu.KLogging

class TrackSession(val channel: VoiceChannel, val scope: CoroutineScope) {
  companion object : KLogging() {
    val playerManager: AudioPlayerManager = DefaultAudioPlayerManager()
      .apply {
        configuration.setFrameBufferFactory(::NonAllocatingAudioFrameBuffer)
      }
      .also { AudioSourceManagers.registerRemoteSources(it) }
  }

  private val player: AudioPlayer = playerManager.createPlayer()
  private var queue: BroadcastChannel<AudioTrack> = BroadcastChannel(Channel.CONFLATED)

  private var connection: VoiceConnection? = null

  val queueFlow: Flow<AudioTrack> get() = queue.openSubscription().receiveAsFlow()

  suspend fun play(song: String): Unit = suspendCoroutine { cont ->
    initialize()

    playerManager.loadItem(
      song,
      object : AudioLoadResultHandler {
        override fun trackLoaded(track: AudioTrack) {
          queue.trySend(track).onFailure { error -> error?.printStackTrace() }

          cont.resume(Unit)
        }

        override fun playlistLoaded(playlist: AudioPlaylist) {
          playlist.tracks.forEach { track ->
            queue.trySend(track).onFailure { error -> error?.printStackTrace() }
          }

          cont.resume(Unit)
        }

        override fun loadFailed(exception: FriendlyException) {
          cont.resumeWith(failure(exception))
        }

        override fun noMatches() {
          cont.resumeWith(failure(IllegalStateException("Could not find any song with `$song`")))
        }
      },
    )
  }

  suspend fun close() {
    if (connection == null) return

    connection?.shutdown()
    connection = null
    queue.cancel()
    queue = BroadcastChannel(Channel.CONFLATED)
  }

  private fun initialize() {
    if (connection != null) return

    scope.launch(CoroutineName("session-${channel.name}/play-tracks")) {
      setupSession()
      playRegisteredTracks()
    }
  }

  private suspend fun setupSession() {
    logger.info("setting up session")

    connection = channel.connect {
      audioProvider(LavaAudioProvider(player))
    }

    logger.info("connection set up")

//    val time = atomic(0)
//
//    queueFlow.map { time.value = 0 }.launchIn(scope)
//
//    scope.launch(CoroutineName("session-${channel.name}/connection-releaser")) {
//      while (time.value <= 15) {
//        delay(1.seconds)
//        time.incrementAndGet()
//      }
//
//      close()
//    }
  }

  private suspend fun playRegisteredTracks() {
    queueFlow.onEach { song ->
      player.playTrack(song)
    }.collect()
  }
}
