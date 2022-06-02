/*
 * The contents of this file is free and unencumbered software released into the
 * public domain. For more information, please refer to <http://unlicense.org/>
 */

@file:OptIn(KordVoice::class)

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
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.connect
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.channel.VoiceChannel
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.string
import gragas.commands.Command
import gragas.commands.CommandScope
import kotlinx.coroutines.launch

class PlayCommand(val kord: Kord) : Command(
  name = "play",
  description = "Plays a song or a track with the specified url",
) {
  val playerManager: AudioPlayerManager = DefaultAudioPlayerManager()
    .apply {
      configuration.setFrameBufferFactory(::NonAllocatingAudioFrameBuffer)
    }
    .also { AudioSourceManagers.registerRemoteSources(it) }

  init {
    settings {
      string("url", "The song URL") {
        required = true
      }
    }
  }

  @Suppress("ControlFlowWithEmptyBody")
  override suspend fun CommandScope.execute(event: GuildChatInputCommandInteractionCreateEvent) {
    val interaction = event.interaction
    val command = interaction.command

    val channel = run {
      val state = interaction.user
        .asMember()
        .getVoiceStateOrNull()
        ?: fail("You must be in a voice channel to play a song!")

      val channelId = state.channelId ?: fail("Can not find channel with id null")

      kord.getChannel(channelId) as? VoiceChannel ?: fail("Can not find channel with id $channelId")
    }

    val url = command.strings["url"] ?: error("unreachable")

    val player: AudioPlayer = playerManager.createPlayer()

    val connection = channel.connect {
      audioProvider(LavaAudioProvider(player))
    }

    interaction.deferPublicResponse().respond {
      content = "You have played the song <$url> in ${channel.data.name.value}"
    }

    playerManager.loadItem(
      url,
      object : AudioLoadResultHandler {
        override fun trackLoaded(track: AudioTrack) {
          player.playTrack(track)

          kord.launch {
            while (track.position < track.duration) {
              println("${track.position} < ${track.duration}")
            }

            connection.shutdown()
            println("stoped")
          }
        }

        override fun playlistLoaded(playlist: AudioPlaylist) {
        }

        override fun loadFailed(exception: FriendlyException) {
          fail(exception.message ?: "Failed to run song")
        }

        override fun noMatches() {
        }
      },
    )
  }
}
