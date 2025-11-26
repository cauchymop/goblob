package com.cauchymop.goblob.lobby

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import net.yura.lobby.model.Game
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LobbyClientTest {

    @Test
    fun `gamesFlow debounces updates`() = runTest {
        val lobbyClient = LobbyClient("uuid", "app", "version")
        val games = mutableListOf<List<Game>>()

        val job = launch {
            lobbyClient.gamesFlow.collect {
                games.add(it)
            }
        }

        // Simulate receiving games quickly
        // Game constructor: public Game(String name, String options, int maxPlayers, long timeout)
        val game1 = Game("Game 1", "", 2, 1000)
        game1.id = 1
        lobbyClient.addOrUpdateGame(game1)

        val game2 = Game("Game 2", "", 2, 1000)
        game2.id = 2
        lobbyClient.addOrUpdateGame(game2)

        val game3 = Game("Game 3", "", 2, 1000)
        game3.id = 3
        lobbyClient.addOrUpdateGame(game3)

        // Wait less than debounce time
        delay(100)
        assertEquals(0, games.size) // Should not have emitted yet

        // Wait more than debounce time
        delay(600)
        assertEquals(1, games.size)
        assertEquals(3, games[0].size)

        // Simulate removal
        lobbyClient.removeGame(2)

        // Wait more than debounce time
        delay(600)
        assertEquals(2, games.size)
        assertEquals(2, games[1].size)
        assertEquals(1, games[1][0].id)
        assertEquals(3, games[1][1].id)

        job.cancel()
    }
}
