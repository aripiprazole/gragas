/*
 * The contents of this file is free and unencumbered software released into the
 * public domain. For more information, please refer to <http://unlicense.org/>
 */

package gragas.play

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack

class TrackScheduler(val player: AudioPlayer) : AudioLoadResultHandler {
  override fun trackLoaded(track: AudioTrack) {
    player.playTrack(track)
  }

  override fun playlistLoaded(playlist: AudioPlaylist) {
    println("playlistLoaded(): Not yet implemented")
  }

  override fun loadFailed(exception: FriendlyException) {
    println("loadFailed(): Not yet implemented")
  }

  override fun noMatches() {
    println("noMatched(): Not yet implemented")
  }
}
