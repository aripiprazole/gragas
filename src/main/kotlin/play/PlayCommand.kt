/*
 * The contents of this file is free and unencumbered software released into the
 * public domain. For more information, please refer to <http://unlicense.org/>
 */

@file:OptIn(KordVoice::class)

package gragas.play

import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
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
  init {
    settings {
      string("url", "The specified song") {
        required = true
      }
    }
  }

  val sessions: MutableMap<Snowflake, TrackSession> = mutableMapOf()

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

    val song = command.strings["url"] ?: error("unreachable")

    sessions
      .getOrPut(channel.id) { TrackSession(channel) }
      .play(song)

    interaction.deferPublicResponse().respond {
      content = "You have played the song <$song> in ${channel.data.name.value}"
    }
  }
}
