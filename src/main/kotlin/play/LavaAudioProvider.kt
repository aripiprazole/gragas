/*
 * The contents of this file is free and unencumbered software released into the
 * public domain. For more information, please refer to <http://unlicense.org/>
 */

@file:OptIn(KordVoice::class)

package gragas.play

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import dev.kord.common.annotation.KordVoice
import dev.kord.voice.AudioFrame
import dev.kord.voice.AudioProvider

class LavaAudioProvider(val player: AudioPlayer) : AudioProvider {
  override suspend fun provide(): AudioFrame? {
    val frame = player.provide() ?: return null

    return AudioFrame(frame.data)
  }
}
