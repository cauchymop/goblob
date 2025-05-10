package com.cauchymop.goblob.lobby

import net.yura.lobby.client.LobbyClient
import net.yura.lobby.client.LobbyCom
import net.yura.lobby.model.Game
import net.yura.lobby.model.GameType
import net.yura.lobby.model.Player


class LobbyClient(uuid: String, appName: String, version: String) : LobbyClient {

    private val mycom: LobbyCom = LobbyCom(uuid, appName, version)
    private val listeners = mutableListOf<LobbyClientListener>()
    var gameType: GameType? = null
        private set

    init {
        mycom.addEventListener(this)
        mycom.connect("lobby.yura.net", 1964)
    }


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

    override fun setUsername(p0: String, p1: Int) {
        println("setUsername")
    }

    override fun addGameType(types: MutableList<Any?>) {
        val gameTypes = types.filterIsInstance<GameType>()
        println("addGameType: types = $gameTypes")
        gameType = gameTypes.first { it.name == "Go Blob" }
        mycom.getGames(gameType)
    }

    override fun addOrUpdateGame(game: Game) {
        println("addOrUpdateGame $game")
        listeners.forEach {
            it.onGameChanged(game)
        }
    }

    override fun removeGame(p0: Int) {
        println("removeGame $p0")
    }

    override fun gameStarted(p0: Int) {
        println("gameStarted $p0")
    }

    override fun messageForGame(p0: Int, p1: Any?) {
        TODO("Not yet implemented")
    }

    override fun getClassLoader(p0: GameType?): ClassLoader {
        TODO("Not yet implemented")
    }

    override fun renamePlayer(p0: String?, p1: String?, p2: Int) {
        TODO("Not yet implemented")
    }

    override fun addPlayer(p0: Player?) {
        TODO("Not yet implemented")
    }

    override fun addPlayer(p0: Int, p1: Player?) {
        TODO("Not yet implemented")
    }

    override fun removePlayer(p0: String?) {
        TODO("Not yet implemented")
    }

    override fun removePlayer(p0: Int, p1: String?) {
        TODO("Not yet implemented")
    }

    override fun incomingChat(p0: String?, p1: String?) {
        TODO("Not yet implemented")
    }

    override fun incomingChat(p0: Int, p1: String?, p2: String?) {
        TODO("Not yet implemented")
    }

    override fun privateMessage(p0: String?, p1: String?) {
        TODO("Not yet implemented")
    }

    override fun setUserInfo(p0: String, p1: MutableList<Any?>?) {
        TODO("Not yet implemented")
    }
}

interface LobbyClientListener {
    fun onGameChanged(game: Game)
}