/*
 * The contents of this file is free and unencumbered software released into the
 * public domain. For more information, please refer to <http://unlicense.org/>
 */

package gragas.commands

import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder

abstract class Command(val name: String, val description: String) {
  var settings: ChatInputCreateBuilder.() -> Unit = {}
    private set

  fun settings(settings: ChatInputCreateBuilder.() -> Unit) {
    this.settings = settings
  }

  abstract suspend fun CommandScope.execute(event: GuildChatInputCommandInteractionCreateEvent)
}
