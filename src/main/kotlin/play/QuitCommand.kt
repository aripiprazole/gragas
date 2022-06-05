/*
 * The contents of this file is free and unencumbered software released into the
 * public domain. For more information, please refer to <http://unlicense.org/>
 */

package gragas.play

import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import gragas.commands.Command
import gragas.commands.CommandScope

class QuitCommand(val trackService: TrackService) : Command(
  name = "quit",
  description = "Quit bot from current channel",
) {
  override suspend fun CommandScope.execute(event: GuildChatInputCommandInteractionCreateEvent) {
    val interaction = event.interaction
    val channel = interaction.getCurrentVoiceChannel()

    trackService.get(channel).close()

    interaction.deferPublicResponse().respond {
      content = "Bot leaved ${channel.name} and cleared the playlist"
    }
  }
}
