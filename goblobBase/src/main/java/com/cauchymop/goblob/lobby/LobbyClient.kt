package com.cauchymop.goblob.lobby

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import net.yura.lobby.client.LobbyClient
import net.yura.lobby.client.LobbyCom
import net.yura.lobby.model.Game
import net.yura.lobby.model.GameType
import net.yura.lobby.model.Player
import java.lang.reflect.Modifier
import java.util.concurrent.ConcurrentHashMap

@OptIn(FlowPreview::class)
class LobbyClient(
    uuid: String,
    appName: String,
    version: String,
    private val pushTokenProvider: (((String?) -> Unit) -> Unit)? = null
) : LobbyClient {

    private var myPlayerName: String = ""
    private val mycom: LobbyCom = LobbyCom(uuid, appName, version)
    private val listeners = mutableListOf<LobbyClientListener>()
    var gameType: GameType? = null
        private set

    val isConnected: Boolean
        get() = gameType != null && myPlayerName.isNotEmpty()

    private val games = ConcurrentHashMap<Int, Game>()
    private val _gamesFlow = MutableSharedFlow<Unit>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val gamesFlow: Flow<List<Game>> = _gamesFlow
        .debounce(500)
        .map { games.values.toList() }

    init {
        mycom.addEventListener(this)
        mycom.connect("lobby.yura.net", 1964)
    }

    fun myPlayerName(): String = myPlayerName

    fun addListener(listener: LobbyClientListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: LobbyClientListener) {
        listeners.remove(listener)
    }

    fun removeAllListeners() {
        listeners.clear()
    }

    fun createNewGame(name: String) {
        val game = Game(name, "", 2, Integer.MAX_VALUE)
        gameType?.let {
            game.type = gameType
        }
        println("OLIVIER: createNewGame $game with type: ${game.type}")
        mycom.createNewGame(game)
    }

    fun setGoogleLogin(email: String, idToken: String) {
        mycom.setEmail(email)
        mycom.setOAuthToken("googleIdToken", idToken)
    }

    fun openGame(gameId: Int) {
        mycom.openGame(gameId)
    }

    fun closeGame(gameId: Int) {
        mycom.closeGame(gameId)
    }

    fun leaveGame(gameId: Int) {
        mycom.leaveGame(gameId)
    }

    fun joinGame(gameId: Int) {
        mycom.joinGame(gameId, null)
    }

    fun sendGameMessage(gameId: Int, message: ByteArray) {
        mycom.sendGameMessage(gameId, message)
    }


    override fun connected() {
        println("OLIVIER: connected!")
        mycom.getGameTypes()
    }

    override fun disconnected() {
        println("OLIVIER: disconnected!")
    }

    override fun connecting(msg: String) {
        println("OLIVIER: connecting: $msg")
    }

    override fun error(msg: String) {
        println("OLIVIER: error: $msg")
    }

    override fun ping(time: Long) {
        println("ping: $time")
    }

    override fun setUsername(userName: String, playerType: Int) {
        println("setUsername")
        myPlayerName = userName
    }

    override fun addGameType(types: MutableList<Any?>) {
        val gameTypes = types.filterIsInstance<GameType>()
        println("addGameType: types = $gameTypes")
        gameType = gameTypes.first { it.name == "Go Blob" }
        gameType?.let { sendPushToken(it) }
        mycom.getGames(gameType)
    }

    private fun sendPushToken(gameType: GameType) {
        val provider = pushTokenProvider ?: return
        provider { pushToken ->
            if (pushToken.isNullOrBlank()) {
                println("addGameType: push token is empty, skipping push registration")
                return@provider
            }

            try {
                val pushSystemFcm = getPushSystemFcm()
                if (invokePushTokenSetter(mycom, pushSystemFcm, gameType, pushToken)) {
                    println("addGameType: push token registered")
                } else {
                    println("addGameType: failed to find setPushToken implementation")
                }
            } catch (e: Throwable) {
                println("addGameType: failed to register push token ${e.message}")
            }
        }
    }

    private fun getPushSystemFcm(): Any {
        val pushLobbyClientClass = Class.forName("net.yura.lobby.client.PushLobbyClient")
        val field = pushLobbyClientClass.getField("PUSH_SYSTEM_FCM")
        return field.get(null)
    }

    private fun invokePushTokenSetter(target: Any, pushSystemFcm: Any, gameType: GameType, pushToken: String): Boolean {
        if (invokeSetPushToken(target, pushSystemFcm, gameType, pushToken)) {
            return true
        }

        val nestedTargets = mutableListOf<Any>()
        target.javaClass.methods
            .asSequence()
            .filter { it.parameterCount == 0 }
            .filter { it.returnType.name == "net.yura.lobby.client.Connection" }
            .forEach {
                val nested = it.invoke(target)
                if (nested != null) {
                    nestedTargets += nested
                }
            }

        target.javaClass.declaredFields
            .asSequence()
            .filter { it.type.name == "net.yura.lobby.client.Connection" }
            .forEach {
                it.isAccessible = true
                val nested = it.get(target)
                if (nested != null) {
                    nestedTargets += nested
                }
            }

        return nestedTargets.any { invokeSetPushToken(it, pushSystemFcm, gameType, pushToken) }
    }

    private fun invokeSetPushToken(target: Any, pushSystemFcm: Any, gameType: GameType, pushToken: String): Boolean {
        val candidates = target.javaClass.methods.filter {
            it.name == "setPushToken" && it.parameterCount == 3
        }
        val matched = candidates.firstOrNull { method ->
            val parameterTypes = method.parameterTypes
            parameterTypes[1].isAssignableFrom(gameType.javaClass) &&
                parameterTypes[2].isAssignableFrom(String::class.java)
        } ?: return false

        if (!Modifier.isPublic(matched.modifiers)) {
            matched.isAccessible = true
        }
        matched.invoke(target, pushSystemFcm, gameType, pushToken)
        return true
    }

    override fun addOrUpdateGame(game: Game) {
        println("addOrUpdateGame $game")
        games[game.id] = game
        _gamesFlow.tryEmit(Unit)
        listeners.forEach {
            it.onAddOrUpdateLobbyGame(game)
        }
    }

    override fun removeGame(gameId: Int) {
        println("removeGame $gameId")
        games.remove(gameId)
        _gamesFlow.tryEmit(Unit)
    }

    override fun gameStarted(p0: Int) {
        println("gameStarted $p0")
    }

    override fun messageForGame(gameId: Int, gameData: Any?) {
        println("messageForGame gameId = $gameId gameData ${gameData?.javaClass?.simpleName}")
        val gameDataBytes: ByteArray? = when (gameData) {
            is ByteArray -> gameData
            else -> null
        }
        listeners.forEach {
            it.onLobbyGameDataChanged(gameId, gameDataBytes)
        }
    }

    override fun getClassLoader(p0: GameType?): ClassLoader {
        TODO("Not yet implemented")
    }

    override fun renamePlayer(p0: String?, p1: String?, p2: Int) {
        println("renamePlayer p0 = $p0 p1 = $p1, p2 = $p2")
    }

    override fun addPlayer(player: Player?) {
        println("addPlayer $player")
    }

    override fun addPlayer(playerId: Int, player: Player?) {
        println("addPlayer id = $playerId player = $player")
    }

    override fun removePlayer(p0: String?) {
        println("removePlayer $p0")
    }

    override fun removePlayer(id: Int, player: String?) {
        println("removePlayer id = $id player = $player")
    }

    override fun incomingChat(p0: String?, p1: String?) {
        println("incomingChat p0 = $p0 p1 = $p1")
    }

    override fun incomingChat(p0: Int, p1: String?, p2: String?) {
        println("incomingChat p0 = $p0 p1 = $p1, P2 = $p2")
    }

    override fun privateMessage(p0: String?, p1: String?) {
        println("privateMessage p0 = $p0 p1 = $p1")
    }

    override fun setUserInfo(p0: String, p1: MutableList<Any?>?) {
        println("setUserInfo p0 = $p0 p1 = $p1")
    }
}

interface LobbyClientListener {
    fun onAddOrUpdateLobbyGame(game: Game)
    fun onLobbyGameDataChanged(gameId: Int, gameDataBytes: ByteArray?)
}
