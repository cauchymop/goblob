package com.cauchymop.goblob.ui

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import com.cauchymop.goblob.model.*
import com.cauchymop.goblob.proto.PlayGameData
import com.cauchymop.goblob.proto.PlayGameData.GameData
import com.cauchymop.goblob.proto.PlayGameData.GameData.Phase
import com.cauchymop.goblob.proto.PlayGameData.GameList
import com.google.android.gms.games.Games
import com.google.android.gms.games.TurnBasedMultiplayerClient
import com.google.android.gms.games.multiplayer.Multiplayer
import com.google.android.gms.games.multiplayer.realtime.RoomConfig
import com.google.android.gms.games.multiplayer.turnbased.LoadMatchesResponse
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchUpdateCallback
import com.google.common.base.Strings
import com.google.common.collect.ImmutableMap
import com.google.protobuf.InvalidProtocolBufferException
import com.google.protobuf.TextFormat
import dagger.Lazy
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Class to persist games.
 */

private const val GAME_DATA = "gameData"
private const val GAMES = "games"
private const val TAG = "AndroidGameRepository"
private const val IGNORED_VALUE = ""

@Singleton
class AndroidGameRepository @Inject
constructor(private val prefs: SharedPreferences, gameDatas: GameDatas,
            private val googleAccountManager: GoogleAccountManager,
            private val turnBasedClientProvider: Provider<TurnBasedMultiplayerClient>,
            private val avatarManager: AvatarManager, analytics: Analytics,
            @Named("PlayerOneDefaultName") playerOneDefaultName: Lazy<String>,
            @Named("PlayerTwoDefaultName") playerTwoDefaultName: String) : GameRepository(analytics, playerOneDefaultName, playerTwoDefaultName, gameDatas, gameCache = loadGameCache(prefs)) {

  init {
    loadLegacyLocalGame()
    fireGameListChanged()
    googleAccountManager.addAccountStateListener(object : AccountStateListener {
      override fun accountStateChanged(isSignInComplete: Boolean) {
        if (isSignInComplete) {
          initTurnBasedUpdateListeners()
        }
      }
    })
  }

  private fun initTurnBasedUpdateListeners() {
    val turnBasedClient = turnBasedClientProvider.get()
    turnBasedClient.registerTurnBasedMatchUpdateCallback(object : TurnBasedMatchUpdateCallback() {
      override fun onTurnBasedMatchReceived(turnBasedMatch: TurnBasedMatch) {
        Log.d(TAG, "onTurnBasedMatchReceived")
        val gameData = getGameData(turnBasedMatch)
        if (gameData != null) {
          if (saveToCache(gameData)) {
            forceCacheRefresh()
          }
        }
      }

      override fun onTurnBasedMatchRemoved(matchId: String) {
        Log.d(TAG, "onTurnBasedMatchRemoved: $matchId")
        removeFromCache(matchId)
      }
    })
  }

  override fun forceCacheRefresh() {
    Log.d(TAG, "forceCacheRefresh")
    persistCache()
    fireGameListChanged()
  }


  private fun persistCache() {
    val editor = prefs.edit()
    editor.putString(GAMES, TextFormat.printToString(gameCache))
    editor.apply()
  }

  override fun publishRemoteGameState(gameData: GameData): Boolean {
    if (googleAccountManager.signInComplete) {
      Log.d(TAG, "publishRemoteGameState: $gameData")
      val turnParticipantId = gameDatas.getCurrentPlayer(gameData).id
      val gameDataBytes = gameData.toByteArray()
      Log.d(TAG, "takeTurn $turnParticipantId")
      val turnBasedClient = turnBasedClientProvider.get()
      turnBasedClient.takeTurn(gameData.matchId, gameDataBytes, turnParticipantId)
      if (gameData.phase == Phase.FINISHED) {
        turnBasedClient.finishMatch(gameData.matchId)
        fireGameSelected(gameData)
      }
      return true
    } else {
      gameCache.putUnpublished(gameData.matchId, IGNORED_VALUE)
      return false
    }
  }

  override fun log(message: String) {
    Log.d(TAG, message)
  }

  private fun loadLegacyLocalGame() {
    val gameDataString = prefs.getString(GAME_DATA, null) ?: return
    log("loadLegacyLocalGame")
    val gameDataBuilder = GameData.newBuilder()
    try {
      TextFormat.merge(gameDataString, gameDataBuilder)
    } catch (e: TextFormat.ParseException) {
      Log.e(TAG, "Error parsing local GameData: ${e.message}")
    }

    val localGame = gameDataBuilder.build()
    if (saveToCache(localGame)) {
      forceCacheRefresh()
    }
    prefs.edit().remove(GAME_DATA).apply()
  }

  fun refreshRemoteGameListFromServer() {
    Log.d(TAG, "refreshRemoteGameListFromServer -  currentMatchId = $currentMatchId")
    val requestId = System.currentTimeMillis()

    val turnBasedClient = turnBasedClientProvider.get()
    val matchListResult = turnBasedClient.loadMatchesByStatus(Multiplayer.SORT_ORDER_SOCIAL_AGGREGATION,
        intArrayOf(TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN, TurnBasedMatch.MATCH_TURN_STATUS_THEIR_TURN, TurnBasedMatch.MATCH_TURN_STATUS_COMPLETE))
    matchListResult.addOnCompleteListener { task ->
      task.result?.get()?.let { matchResponseCallback(requestId, it) }

    }
  }

  private fun matchResponseCallback(requestId: Long, loadMatchesResponse: LoadMatchesResponse) {
    Log.d(TAG, String.format("matchResult: requestId = %d, latency = %d ms", requestId, System.currentTimeMillis() - requestId))
    val allMatches = with(loadMatchesResponse) { myTurnMatches.asSequence() + theirTurnMatches.asSequence() + completedMatches.asSequence() }
    val games = HashSet<GameData>()
    for (match in allMatches) {
      updateAvatars(match)
      val gameData = getGameData(match)
      if (gameData != null) {
        games.add(gameData)
      }
    }

    val removedMatchIds = clearRemoteGamesIfAbsent(games)
    val selectedIsGone = removedMatchIds.contains(currentMatchId)
    val changedCount = removedMatchIds.size + games.filter { saveToCache(it) }.count()
    if (changedCount > 0) {
      forceCacheRefresh()
    }

    // Select invitation if one has arrived
    invitationMatchId?.let {
      invitationMatchId = null
      selectGame(it)
    }

    Log.d(TAG, " ===> (in refreshRemoteGameListFromServer) changedCount is $changedCount and selectedIsGone is $selectedIsGone")
    if (selectedIsGone) {
      // selected game was removed, we select new game instead
      selectGame(NO_MATCH_ID)
    }

    loadMatchesResponse.release()
  }

  private fun clearRemoteGamesIfAbsent(games: Set<GameData>): List<String> {
    val keysToRemove = gameCache.gamesMap.filter { gameDatas.isRemoteGame(it.value) && !games.contains(it.value) }.map { it.key }
    keysToRemove.forEach { gameCache.removeGames(it) }
    return keysToRemove
  }

  private fun updateAvatars(match: TurnBasedMatch) {
    for (participant in match.participants) {
      val player = participant.player
      avatarManager.setAvatarUri(player.displayName, player.iconImageUri)
    }
  }

  private fun getGameData(turnBasedMatch: TurnBasedMatch): GameData? {
    val data = turnBasedMatch.data
        ?: // When a crash happens during game creation, the TurnBasedMatch contains a null game
        // that can't be recovered, and would prevent starting the app.
        return null

    handleMatchStatusComplete(turnBasedMatch)
    try {
      var gameData: GameData = GameData.parseFrom(data)
      gameData = handleBackwardCompatibility(turnBasedMatch, gameData)
      return gameData
    } catch (exception: InvalidProtocolBufferException) {
      throw RuntimeException(exception)
    }

  }

  private fun handleMatchStatusComplete(turnBasedMatch: TurnBasedMatch) {
    val myTurn = turnBasedMatch.turnStatus == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN
    if (myTurn && turnBasedMatch.status == TurnBasedMatch.MATCH_STATUS_COMPLETE) {
      val turnBasedClient = turnBasedClientProvider.get()
      turnBasedClient.finishMatch(turnBasedMatch.matchId)
    }
  }

  private fun createNewGameData(turnBasedMatch: TurnBasedMatch): GameData {
    val myId = getMyId(turnBasedMatch)
    val opponentId = getOpponentId(turnBasedMatch)
    val blackPlayer = createGoPlayer(turnBasedMatch, myId, true)
    val whitePlayer = createGoPlayer(turnBasedMatch, opponentId, false)
    val gameData = gameDatas.createNewGameData(turnBasedMatch.matchId,
        PlayGameData.GameType.REMOTE, blackPlayer, whitePlayer)
    analytics.gameCreated(gameData)

    commitGameChanges(gameData)
    return gameData
  }

  @Suppress("DEPRECATION")
  private fun handleBackwardCompatibility(turnBasedMatch: TurnBasedMatch,
                                          initialGameData: GameData): GameData {
    val gameData = initialGameData.toBuilder()

    // No players
    if (!gameData.gameConfiguration.hasBlack() || !gameData.gameConfiguration.hasWhite()) {
      val myId = getMyId(turnBasedMatch)
      val opponentId = getOpponentId(turnBasedMatch)
      val goPlayers = ImmutableMap.of(
          myId, createGoPlayer(turnBasedMatch, myId, true),
          opponentId, createGoPlayer(turnBasedMatch, opponentId, false))
      val gameConfiguration = gameData.gameConfiguration

      val blackPlayer = goPlayers[gameConfiguration.blackId]
      val whitePlayer = goPlayers[gameConfiguration.whiteId]

      gameData.gameConfigurationBuilder
          .setBlack(blackPlayer)
          .setWhite(whitePlayer)
    }

    // No match Id
    if (Strings.isNullOrEmpty(gameData.matchId)) {
      gameData.matchId = turnBasedMatch.matchId
    }

    // No phase (version < 2)
    if (gameData.phase == Phase.UNKNOWN) {
      gameData.phase = if (gameData.hasMatchEndStatus()) {
        if (gameData.matchEndStatus.gameFinished) {
          Phase.FINISHED
        } else {
          Phase.DEAD_STONE_MARKING
        }
      } else {
        Phase.IN_GAME
      }
    }

    // No turn (version < 2)
    if (!gameData.hasTurn()) {
      if (gameData.hasMatchEndStatus()) {
        gameData.turn = gameData.matchEndStatus.turn
      } else {
        val currentTurn = gameDatas.computeInGameTurn(gameData.gameConfiguration, gameData.moveCount)
        gameData.turn = currentTurn
      }
    }

    return fillLocalStates(turnBasedMatch, gameData).build()
  }

  private fun fillLocalStates(turnBasedMatch: TurnBasedMatch, gameData: GameData.Builder): GameData.Builder {
    val isMyTurn = turnBasedMatch.turnStatus == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN
    val turnIsBlack = gameData.turn == PlayGameData.Color.BLACK
    val iAmBlack = isMyTurn && turnIsBlack || !isMyTurn && !turnIsBlack
    val gameConfiguration = gameData.gameConfigurationBuilder
    val blackPlayer = gameConfiguration.blackBuilder
    val whitePlayer = gameConfiguration.whiteBuilder
    blackPlayer.isLocal = iAmBlack
    whitePlayer.isLocal = !iAmBlack
    Log.d(TAG, String.format("black: %s", blackPlayer))
    Log.d(TAG, String.format("white %s", whitePlayer))
    return gameData
  }

  private fun getOpponentId(turnBasedMatch: TurnBasedMatch): String {
    val myId = getMyId(turnBasedMatch)
    for (participantId in turnBasedMatch.participantIds) {
      if (participantId != myId) {
        return participantId
      }
    }
    throw RuntimeException("Our TurnBasedMatch should contain 2 players!")
  }

  private fun getMyId(turnBasedMatch: TurnBasedMatch) =
      turnBasedMatch.getParticipantId(googleAccountManager.currentPlayerId)

  private fun createGoPlayer(match: TurnBasedMatch, participantId: String,
                             isLocal: Boolean): PlayGameData.GoPlayer {
    val goPlayer: PlayGameData.GoPlayer
    val player = match.getParticipant(participantId).player
    goPlayer = gameDatas.createGamePlayer(participantId, player.displayName, isLocal)
    avatarManager.setAvatarUri(player.displayName, player.iconImageUri)
    return goPlayer
  }

  fun handlePlayersSelected(intent: Intent) {
    Log.d(TAG, "handlePlayersSelected")

    // get the invitee list
    val invitees = intent.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS)
    Log.d(TAG, "Invitees: $invitees")

    // get the automatch criteria
    var autoMatchCriteria: Bundle? = null
    val minAutoMatchPlayers = intent.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0)
    val maxAutoMatchPlayers = intent.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0)
    if (minAutoMatchPlayers > 0 || maxAutoMatchPlayers > 0) {
      autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
          minAutoMatchPlayers, maxAutoMatchPlayers, 0)
      Log.d(TAG, "Automatch criteria: " + autoMatchCriteria!!)
    }

    // create game
    val turnBasedMatchConfig = TurnBasedMatchConfig.builder()
        .addInvitedPlayers(invitees)
        .setVariant(TurnBasedMatch.MATCH_VARIANT_DEFAULT)
        .setAutoMatchCriteria(autoMatchCriteria).build()

    // kick the match off
    val turnBasedClient = turnBasedClientProvider.get()
    turnBasedClient.createMatch(turnBasedMatchConfig).addOnCompleteListener { initiateMatchResult ->
      Log.d(TAG, "InitiateMatchResult $initiateMatchResult")
      if (!initiateMatchResult.isSuccessful) {
        return@addOnCompleteListener
      }
      initiateMatchResult.result?.let {
        createNewGameData(it)

        Log.d(TAG, "Game created...")
        selectGame(it.matchId)
      }
    }
  }

  fun handleCheckMatchesResult(responseCode: Int, intent: Intent?) {
    Log.d(TAG, "handleCheckMatchesResult")
    // Refresh is done in all cases as games can be dismissed for the Check Matches Screen if we then
    // do not select a game and press back
    refreshRemoteGameListFromServer()

    if (responseCode == Activity.RESULT_OK && intent != null) {
      val match = intent.getParcelableExtra<TurnBasedMatch>(Multiplayer.EXTRA_TURN_BASED_MATCH)
      selectGame(match.matchId)
    }
  }

}

private fun loadGameCache(sharedPreferences: SharedPreferences): GameList.Builder {
  Log.d(TAG, "loadGameList")
  val gameListString = sharedPreferences.getString(GAMES, "")
  val gameListBuilder = GameList.newBuilder()
  try {
    TextFormat.merge(gameListString, gameListBuilder)
  } catch (e: TextFormat.ParseException) {
    Log.e(TAG, "Error parsing local GameList: " + e.message)
  }

  Log.d(TAG, "loadGameList: " + gameListBuilder.gamesCount + " games loaded.")
  return gameListBuilder
}
