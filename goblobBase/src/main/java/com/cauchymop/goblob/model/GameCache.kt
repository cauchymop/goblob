package com.cauchymop.goblob.model

import com.cauchymop.goblob.proto.PlayGameData
import com.cauchymop.goblob.proto.PlayGameData.GameData
import com.cauchymop.goblob.proto.PlayGameData.GameList
import com.google.protobuf.TextFormat
import java.io.Closeable
import javax.inject.Inject
import javax.inject.Named

private const val IGNORED_VALUE = ""

class GameCache @Inject constructor(@Named("SerializedCache") serializedCache: String, private val gameDatas: GameDatas) {
  val cache: PlayGameData.GameList.Builder = GameList.newBuilder().apply {
    TextFormat.merge(serializedCache, this)
  }

  val myTurnGames: Iterable<PlayGameData.GameData>
    get() = cache.gamesMap.values.filter(gameDatas::isLocalTurn)

  val theirTurnGames: Iterable<PlayGameData.GameData>
    get() = cache.gamesMap.values.filterNot(gameDatas::isLocalTurn)

  operator fun get(matchId: String): PlayGameData.GameData? {
    return cache.gamesMap.get(matchId);
  }

  private operator fun set(matchId: String, gameData: PlayGameData.GameData) {
    cache.putGames(matchId, gameData)
  }

  private fun removeGame(matchId: String) {
    cache.removeGames(matchId)
  }

  fun removeUnpublished(matchId: String) {
    cache.removeUnpublished(matchId)
  }

  fun addUnpublished(matchId: String) {
    cache.unpublishedMap.put(matchId, IGNORED_VALUE)
  }

  fun getUnpublished(): List<String> {
    return cache.unpublishedMap.keys.toList()
  }

  private fun clearRemoteGamesIfAbsent(games: Set<PlayGameData.GameData>): List<String> =
      cache.gamesMap.values
          .filter { gameDatas.isRemoteGame(it) && !games.contains(it) }
          .map(GameData::getMatchId)
          .onEach { cache.removeGames(it) }

  fun toSerializedString(): String = TextFormat.printToString(cache)

  fun getUpdater(fireOnClose: () -> Unit) = Updater(fireOnClose)

  inner class Updater(val fireOnClose: () -> Unit) : Closeable {
    var listChanged: Boolean = false;

    override fun close() {
      if (listChanged) {
        fireOnClose()
      }
    }

    operator fun set(matchId: String, gameData: PlayGameData.GameData) {
      if (!cache.gamesMap.containsKey(matchId)) {
        listChanged = true;
      }
      this@GameCache[matchId] = gameData
    }

    fun removeGame(matchId: String) {
      if (cache.gamesMap.containsKey(matchId)) {
        listChanged = true;
      }
      this@GameCache.removeGame(matchId)
    }

    fun clearRemoteGamesIfAbsent(games: Set<PlayGameData.GameData>): List<String> {
      val removedGames = this@GameCache.clearRemoteGamesIfAbsent(games)
      if (removedGames.isNotEmpty()) {
        listChanged = true
      }
      return removedGames
    }
  }
}
