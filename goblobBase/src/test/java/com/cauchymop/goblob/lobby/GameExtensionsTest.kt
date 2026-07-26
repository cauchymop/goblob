package com.cauchymop.goblob.lobby

import com.google.common.truth.Truth.assertThat
import net.yura.lobby.model.Game
import net.yura.lobby.model.Player
import org.junit.Test

class GameExtensionsTest {

    @Test
    fun isJoinable_waitingForOpponent_returnsTrue() {
        val game = createGame(playerNames = listOf("player1"))
        assertThat(game.isJoinable("myPlayerName")).isTrue()
    }

    @Test
    fun isJoinable_iAmAPlayer_returnsFalse() {
        val game = createGame(playerNames = listOf("myPlayerName"))
        assertThat(game.isJoinable("myPlayerName")).isFalse()
    }

    @Test
    fun isJoinable_full_returnsFalse() {
        val game = createGame(playerNames = listOf("player1", "player2"))
        assertThat(game.isJoinable("myPlayerName")).isFalse()
    }

    @Test
    fun isMyGame_iAmTheOnlyPlayer_returnsTrue() {
        val game = createGame(playerNames = listOf("myPlayerName"))
        assertThat(game.isMyGame("myPlayerName")).isTrue()
    }

    @Test
    fun isMyGame_iAmOneOfTwoPlayers_returnsTrue() {
        val game = createGame(playerNames = listOf("player1", "myPlayerName"))
        assertThat(game.isMyGame("myPlayerName")).isTrue()
    }

    @Test
    fun isMyGame_iAmNotAPlayer_returnsFalse() {
        val game = createGame(playerNames = listOf("player1", "player2"))
        assertThat(game.isMyGame("myPlayerName")).isFalse()
    }

    private fun createGame(playerNames: List<String>): Game {
        val game = Game().apply {
            maxPlayers = 2
        }
        val players = playerNames.map { Player(it, Player.PLAYER_NORMAL) }.toSet()
        game.players = players
        return game
    }
}
