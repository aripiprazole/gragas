/*
 * The contents of this file is free and unencumbered software released into the
 * public domain. For more information, please refer to <http://unlicense.org/>
 */

package gragas.play

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.channel.VoiceChannel

class TrackService(val kord: Kord) {
  private val sessions: MutableMap<Snowflake, TrackSession> = mutableMapOf()

  fun get(channel: VoiceChannel): TrackSession {
    return sessions.getOrPut(channel.id) { TrackSession(channel, kord) }
  }
}
