package com.cauchymop.goblob.model

import com.cauchymop.goblob.lobby.LobbyClient
import com.cauchymop.goblob.lobby.LobbyClientListener
import com.cauchymop.goblob.lobby.isMyGameWaitingForOpponent
import com.cauchymop.goblob.lobby.isOnGoingGameFromOtherPlayers
import com.cauchymop.goblob.proto.PlayGameData
import com.cauchymop.goblob.proto.PlayGameData.GameData
import com.google.common.collect.ImmutableSet
import com.google.common.collect.Lists
import dagger.Lazy
import net.yura.lobby.model.Game
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
    protected val gameCache: PlayGameData.GameList.Builder
) : LobbyClientListener {

    var currentMatchId: String = NO_MATCH_ID
        private set
    var pendingMatchId: String? = null

    private val gameListlisteners = Lists.newArrayList<GameListListener>()
    private val gameChangelisteners = Lists.newArrayList<GameChangeListener>()
    private val gameSelectionListeners = Lists.newArrayList<GameSelectionListener>()

    protected val lobbyGamesById: MutableMap<Int, Game> = mutableMapOf()

    val lobbyGames: Iterable<Game>
        get() = lobbyGamesById.values.sortedByDescending { it.inGame }

    val myTurnGames: Iterable<GameData>
        get() = gameCache.gamesMap.values.filter(gameDatas::isLocalTurn)

    val theirTurnGames: Iterable<GameData>
        get() = gameCache.gamesMap.values.filterNot(gameDatas::isLocalTurn)

    private val currentGame: GameData?
        get() = gameCache.gamesMap[currentMatchId]

    fun commitGameChanges(gameData: GameData) {
        saveToCache(gameData)
        if (gameDatas.isRemoteGame(gameData)) {
            publishRemoteGameState(gameData)
        }
        forceCacheRefresh()
    }

    protected abstract fun forceCacheRefresh()

    protected abstract fun getLobbyClient(): LobbyClient

    protected fun saveToCache(gameData: GameData): Boolean {
        log("saveToCache " + gameData.matchId)
        val existingGame = gameCache.gamesMap[gameData.matchId]
        log(" -> existingGame found = " + (existingGame != null))
        val changed = existingGame == null || gameData.sequenceNumber > existingGame.sequenceNumber
        if (changed) {
            gameCache.putGames(gameData.matchId, gameData)
            fireGameChanged(gameData)
        } else {
            log(
                String.format(
                    "Ignoring GameData with an old or same sequence number (%s when existing is %s)",
                    gameData.sequenceNumber,
                    existingGame?.sequenceNumber
                )
            )
        }
        return changed
    }

    fun publishUnpublishedGames() {
        for (matchId in ImmutableSet.copyOf(gameCache.unpublishedMap.keys)) {
            val gameData = gameCache.gamesMap[matchId]
            // The match can be absent if the user changed.
            if (gameData != null && publishRemoteGameState(gameData)) {
                gameCache.removeUnpublished(gameData.matchId)
            }
        }
    }

    protected fun publishRemoteGameState(gameData: GameData): Boolean {
        log("publishRemoteGameState: $gameData")
//            val playerId = gameDatas.getCurrentPlayer(gameData).id
            val gameDataBytes = gameData.toByteArray()
            getLobbyClient().sendGameMessage(gameData.matchId.toInt(), gameDataBytes)
//            log(Log.DEBUG, TAG, "takeTurn $turnParticipantId")
//            val turnBasedClient = turnBasedClientProvider.get()
//            turnBasedClient.takeTurn(gameData.matchId, gameDataBytes, turnParticipantId)
//            if (gameData.phase == Phase.FINISHED) {
//                turnBasedClient.finishMatch(gameData.matchId)
//                fireGameSelected(gameData)
//            }
            return true
//        } else {
//            gameCache.putUnpublished(gameData.matchId, IGNORED_VALUE)
//            return false
//        }
    }

    protected fun removeFromCache(matchId: String) {
        log("removeFromCache $matchId")
        gameCache.removeGames(matchId)
        forceCacheRefresh()
    }

    fun selectGame(matchId: String) {
        log("selectGame matchId = " + matchId)
        if (currentMatchId == matchId) {
            return
        }
        if (matchId != NO_MATCH_ID && !gameCache.containsGames(matchId)) {
            pendingMatchId = matchId
            matchId.toIntOrNull()?.let { lobbyGameId ->
                if (lobbyGamesById.containsKey(lobbyGameId)) {
                    val game = lobbyGamesById[lobbyGameId]?:return
                    if (game.players.size == 2) {
                        getLobbyClient().playGame(lobbyGameId)
                    } else if (game.isMyGameWaitingForOpponent(getLobbyClient().myPlayerName())) {
                        saveToCache(game.toNewGameData(gameDatas, getLobbyClient()))
                    } else {
                        getLobbyClient().joinGame(lobbyGameId)
                    }
                }
            }
            return
        }

        currentMatchId = matchId
        val lobbyGameId = currentMatchId.toIntOrNull()
        if (lobbyGameId != null && lobbyGamesById.containsKey(lobbyGameId)) {
            getLobbyClient().closeGame(lobbyGameId)
        }
        if (matchId == NO_MATCH_ID) {
            fireGameSelected(null)
        } else {
            fireGameSelected(gameCache.gamesMap[matchId])
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
        val localGame =
            gameDatas.createNewGameData(LOCAL_MATCH_ID, PlayGameData.GameType.LOCAL, black, white)
        analytics.gameCreated(localGame)
        commitGameChanges(localGame)
        return localGame
    }

    fun createNewRemoteGame() {
        // TODO: Find a nice way to set a Name
        val userName = getLobbyClient().myPlayerName()
        getLobbyClient().createNewGame("$userName's new Game")
    }

    override fun onAddOrUpdateLobbyGame(game: Game) {
        // Filter out games with 2 players where I am not a player
        val myPlayerName = getLobbyClient().myPlayerName()
        val isOnGoingGameFromOtherPlayers = game.isOnGoingGameFromOtherPlayers(myPlayerName)
//        val isMyGameWaitingForOpponent = game.isMyGameWaitingForOpponent(myPlayerName)
        if (isOnGoingGameFromOtherPlayers ) { //|| isMyGameWaitingForOpponent) {
            return
        }
        lobbyGamesById[game.id] = game
        fireGameListChanged()
    }

    override fun onLobbyGameDataChanged(gameId: Int, gameDataBytes: ByteArray?) {
        val game = lobbyGamesById[gameId] ?: return
        val gameData: GameData = gameDataBytes?.let { data: ByteArray ->
            if (data.isNotEmpty()) {
                GameData.parseFrom(data)
            } else null
        }?: game.toNewGameData(gameDatas, getLobbyClient())
        println("OLIVIER: onGameDataChanged for ${game.name} gameData = $gameData")
        println("OLIVIER: pendingMatchId = $pendingMatchId, gameData.matchId = ${gameData.matchId}, game.id = ${game.id}")
        saveToCache(gameData)
        if (pendingMatchId == game.id.toString()) {
            pendingMatchId = null
            selectGame(gameData.matchId)
        }
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

fun Game.toNewGameData(gameDatas: GameDatas, lobbyClient: LobbyClient): GameData {
    val black: String? = players.firstOrNull()?.toString()
    val white = players.firstOrNull { it.toString() != black }?.toString()
    val myPlayerName = lobbyClient.myPlayerName()
    val blackPlayer = black?.let { gameDatas.createGamePlayer(it, it, it == myPlayerName) }
    val whitePlayer = white?.let {gameDatas.createGamePlayer(it, it, it == myPlayerName)}
    return gameDatas.createNewGameData(
        id.toString(),
        PlayGameData.GameType.REMOTE,
        blackPlayer,
        whitePlayer,
    )
}
