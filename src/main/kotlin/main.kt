/*
 * The contents of this file is free and unencumbered software released into the
 * public domain. For more information, please refer to <http://unlicense.org/>
 */

@file:JvmName("Main")

package gragas

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.string
import java.lang.System.getenv
import java.lang.System.setProperty
import java.util.concurrent.Executors
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.DEBUG_PROPERTY_NAME
import kotlinx.coroutines.DEBUG_PROPERTY_VALUE_ON
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging

private val log = KotlinLogging.logger { }

fun main() {
  setProperty(DEBUG_PROPERTY_NAME, DEBUG_PROPERTY_VALUE_ON)

  val rootDispatcher = Executors.newFixedThreadPool(8).asCoroutineDispatcher()

  runBlocking(rootDispatcher + CoroutineName("gragas")) {
    startBot()
  }
}

suspend fun startBot() {
  log.info { "Starting bot..." }

  val token = getenv("DISCORD_TOKEN") ?: error("Can not find Bot token.")

  val kord = Kord(token)
  kord.playCommand()
  kord.login()
}

suspend fun Kord.playCommand() {
  events
    .filterIsInstance<GuildChatInputCommandInteractionCreateEvent>()
    .filter { it.interaction.command.rootName == "play" }
    .map { event ->
      val command = event.interaction.command

      val url = command.strings["url"] ?: error("unreachable")

      event.interaction.deferPublicResponse().respond {
        content = "You have played the song $url"
      }
    }
    .launchIn(this)

  createGuildChatInputCommand(
    guildId = Snowflake(731161106797559841L),
    name = "play",
    description = "Plays a specified song in your channel",
  ) {
    string("url", "The song URL") {
      required = true
    }
  }
}
