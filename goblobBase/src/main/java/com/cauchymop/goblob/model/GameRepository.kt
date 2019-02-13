package com.cauchymop.goblob.model

import com.cauchymop.goblob.proto.PlayGameData
import com.cauchymop.goblob.proto.PlayGameData.GameData
import com.google.common.annotations.VisibleForTesting
import com.google.common.collect.Lists
import dagger.Lazy
import javax.inject.Named

const val NO_MATCH_ID = "No Selected MatchId"

private const val LOCAL_MATCH_ID = "local"
private const val PLAYER_ONE_ID = "player1"
private const val PLAYER_TWO_ID = "player2"

abstract class GameRepository(
    protected var analytics: Analytics,
    @param:Named("PlayerOneDefaultName") private val playerOneDefaultName: Lazy<String>,
    @param:Named("PlayerTwoDefaultName") private val playerTwoDefaultName: String,
    protected val gameDatas: GameDatas,
    protected val gameCache: GameCache) {

  var currentMatchId: String = NO_MATCH_ID
    private set
  var pendingMatchId: String? = null

  private val currentGame: PlayGameData.GameData?
    get() = gameCache[currentMatchId]

  private val gameListlisteners = Lists.newArrayList<GameListListener>()
  private val gameChangelisteners = Lists.newArrayList<GameChangeListener>()
  private val gameSelectionListeners = Lists.newArrayList<GameSelectionListener>()

  fun commitGameChanges(gameData: GameData) {
    saveToCache(gameData)
    if (gameDatas.isRemoteGame(gameData)) {
      publishRemoteGameState(gameData)
    }
    forceCacheRefresh()
  }

  protected abstract fun forceCacheRefresh()

  @VisibleForTesting
  fun saveToCache(gameData: GameData): Boolean {
    log("saveToCache " + gameData.matchId)
    val existingGame = gameCache[gameData.matchId]
    log(" -> existingGame found = " + (existingGame != null))
    val changed = existingGame == null || gameData.sequenceNumber > existingGame.sequenceNumber
    if (changed) {
      gameCache[gameData.matchId] = gameData
      fireGameChanged(gameData)
    } else {
      log(String.format("Ignoring GameData with an old or same sequence number (%s when existing is %s)", gameData.sequenceNumber, existingGame?.sequenceNumber))
    }
    return changed
  }

  fun publishUnpublishedGames() {
    gameCache.getUnpublished()
        .mapNotNull(gameCache::get)
        .filter(::publishRemoteGameState)
        .map(GameData::getMatchId)
        .forEach(gameCache::removeUnpublished)
  }

  protected abstract fun publishRemoteGameState(gameData: GameData): Boolean

  protected fun removeFromCache(matchId: String) {
    log("removeFromCache $matchId")
    gameCache.removeGame(matchId)
    forceCacheRefresh()
  }

  fun selectGame(matchId: String) {
    log("selectGame matchId = " + matchId)
    if (currentMatchId == matchId) {
      return
    }
    if (matchId != NO_MATCH_ID && gameCache[matchId] == null) {
      pendingMatchId = matchId
      return
    }

    currentMatchId = matchId
    if (matchId == NO_MATCH_ID) {
      fireGameSelected(null)
    } else {
      fireGameSelected(gameCache[matchId])
    }
  }

  fun addGameListListener(listener: GameListListener) {
    gameListlisteners.add(listener)
    listener.gameListChanged()
  }

  fun removeGameListListener(listener: GameListListener) {
    gameListlisteners.remove(listener)
  }

  fun addGameChangeListener(listener: GameChangeListener) {
    gameChangelisteners.add(listener)
  }

  fun removeGameChangeListener(listener: GameChangeListener) {
    gameChangelisteners.remove(listener)
  }

  fun addGameSelectionListener(listener: GameSelectionListener) {
    gameSelectionListeners.add(listener)
    listener.gameSelected(currentGame)
  }

  fun removeGameSelectionListener(listener: GameSelectionListener) {
    gameSelectionListeners.remove(listener)
  }

  protected fun fireGameListChanged() {
    for (listener in gameListlisteners) {
      listener.gameListChanged()
    }
  }

  private fun fireGameChanged(gameData: GameData) {
    for (listener in gameChangelisteners) {
      listener.gameChanged(gameData)
    }
  }

  protected fun fireGameSelected(gameData: GameData?) {
    for (listener in gameSelectionListeners) {
      listener.gameSelected(gameData)
    }
  }

  protected abstract fun log(message: String)

  fun createNewLocalGame(): GameData {
    val black = gameDatas.createGamePlayer(PLAYER_ONE_ID, playerOneDefaultName.get(), true)
    val white = gameDatas.createGamePlayer(PLAYER_TWO_ID, playerTwoDefaultName, true)
    removeFromCache(LOCAL_MATCH_ID)
    val localGame = gameDatas.createNewGameData(LOCAL_MATCH_ID, PlayGameData.GameType.LOCAL, black, white)
    analytics.gameCreated(localGame)
    commitGameChanges(localGame)
    return localGame
  }

}

interface GameListListener {
  fun gameListChanged()
}

interface GameChangeListener {
  fun gameChanged(gameData: GameData)
}

interface GameSelectionListener {
  fun gameSelected(gameData: GameData?)
}

