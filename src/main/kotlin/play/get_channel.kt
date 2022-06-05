/*
 * The contents of this file is free and unencumbered software released into the
 * public domain. For more information, please refer to <http://unlicense.org/>
 */

package gragas.play

import dev.kord.core.entity.channel.VoiceChannel
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import gragas.commands.CommandFailure

suspend fun GuildChatInputCommandInteraction.getCurrentVoiceChannel(): VoiceChannel {
  val state = user
    .asMember()
    .getVoiceStateOrNull()
    ?: throw CommandFailure("You must be in a voice channel to play a song!")

  val channelId = state.channelId
    ?: throw CommandFailure("Can not find channel with id null")

  return kord.getChannel(channelId) as? VoiceChannel
    ?: throw CommandFailure("Can not find channel with id $channelId")
}
