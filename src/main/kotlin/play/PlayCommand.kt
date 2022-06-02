/*
 * The contents of this file is free and unencumbered software released into the
 * public domain. For more information, please refer to <http://unlicense.org/>
 */

@file:OptIn(KordVoice::class)

package gragas.play

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
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

class PlayCommand(val kord: Kord) : Command(
  name = "play",
  description = "Plays a song or a track with the specified url",
) {
  val playerManager: AudioPlayerManager = DefaultAudioPlayerManager()
    .apply {
      configuration.setFrameBufferFactory(::NonAllocatingAudioFrameBuffer)
    }
    .also { AudioSourceManagers.registerRemoteSources(it) }

  val player: AudioPlayer = playerManager.createPlayer()
  val scheduler = TrackScheduler(player)

  init {
    settings {
      string("url", "The song URL") {
        required = true
      }
    }
  }

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

    playerManager.loadItem(url, scheduler)

    channel.connect {
      audioProvider(LavaAudioProvider(player))
    }

    interaction.deferPublicResponse().respond {
      content = "You have played the song <$url> in ${channel.data.name.value}"
    }
  }
}
