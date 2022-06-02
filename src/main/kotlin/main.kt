/*
 * The contents of this file is free and unencumbered software released into the
 * public domain. For more information, please refer to <http://unlicense.org/>
 */

@file:JvmName("Main")
@file:OptIn(KordVoice::class)

package gragas

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
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.connect
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.channel.VoiceChannel
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.string
import dev.kord.voice.AudioFrame
import dev.kord.voice.AudioProvider
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
  log.info { "Running bot..." }

  val token = getenv("DISCORD_TOKEN") ?: error("Can not find Bot token.")

  val kord = Kord(token)
  kord.playCommand()
  kord.login()
}

class LavaPlayerAudioProvider(val player: AudioPlayer) : AudioProvider {
  override suspend fun provide(): AudioFrame? {
    val frame = player.provide() ?: return null

    return AudioFrame(frame.data)
  }
}

class LavaAudioLoadResultHandler(val player: AudioPlayer) : AudioLoadResultHandler {
  override fun trackLoaded(track: AudioTrack) {
    player.playTrack(track)
  }

  override fun playlistLoaded(playlist: AudioPlaylist?) {
    println("playlistLoaded(): Not yet implemented")
  }

  override fun noMatches() {
    println("noMatched(): Not yet implemented")
  }

  override fun loadFailed(exception: FriendlyException?) {
    println("loadFailed(): Not yet implemented")
  }
}

suspend fun Kord.playCommand() {
  val playerManager: AudioPlayerManager = DefaultAudioPlayerManager()
    .apply {
      configuration.setFrameBufferFactory(::NonAllocatingAudioFrameBuffer)
    }
    .also { AudioSourceManagers.registerRemoteSources(it) }

  val player: AudioPlayer = playerManager.createPlayer()
  val scheduler = LavaAudioLoadResultHandler(player)

  suspend fun GuildChatInputCommandInteractionCreateEvent.onExecution() {
    val command = interaction.command

    val channel = run {
      val state = interaction.user
        .asMember()
        .getVoiceStateOrNull()
        ?: return interaction
          .deferPublicResponse()
          .respond {
            content = "You must be in a voice channel to play a song!"
          }
          .unit()

      val channelId = state.channelId ?: return interaction
        .deferPublicResponse()
        .respond {
          content = "Can not find channel with id null"
        }
        .unit()

      getChannel(channelId) as? VoiceChannel ?: return interaction
        .deferPublicResponse()
        .respond {
          content = "Can not find channel with id $channelId"
        }
        .unit()
    }

    val url = command.strings["url"] ?: error("unreachable")

    playerManager.loadItem(url, scheduler)

    channel.connect {
      audioProvider(LavaPlayerAudioProvider(player))
    }

    interaction.deferPublicResponse().respond {
      content = "You have played the song <$url> in ${channel.data.name.value}"
    }
  }

  events
    .filterIsInstance<GuildChatInputCommandInteractionCreateEvent>()
    .filter { it.interaction.command.rootName == "play" }
    .map { it.onExecution() }
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

fun Any?.unit(): Unit = Unit
