/*
 * The contents of this file is free and unencumbered software released into the
 * public domain. For more information, please refer to <http://unlicense.org/>
 */

package gragas.play

import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import gragas.commands.Command
import gragas.commands.CommandScope
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList

class TrackCommand(val trackService: TrackService) : Command(
  name = "track",
  description = "returns the current track",
) {
  override suspend fun CommandScope.execute(event: GuildChatInputCommandInteractionCreateEvent) {
    val interaction = event.interaction
    val channel = interaction.getCurrentVoiceChannel()

    val allTracks = trackService.get(channel).queueFlow

    val nextTracks = allTracks.take(5).toList()

    interaction.deferPublicResponse().respond {
      content = buildString {
        appendLine("**Current track**")
        appendLine("  All tracks: ${allTracks.count()}")
        appendLine()

        nextTracks.forEach { track ->
          val duration = track.duration.milliseconds
          appendLine("${track.info.identifier} - $duration")
        }
      }
    }
  }
}
