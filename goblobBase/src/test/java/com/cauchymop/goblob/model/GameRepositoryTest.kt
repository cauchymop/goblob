package com.cauchymop.goblob.model

import com.cauchymop.goblob.lobby.LobbyClient
import com.cauchymop.goblob.proto.PlayGameData
import com.google.common.truth.Truth.assertThat
import dagger.Lazy
import net.yura.lobby.model.Game
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class GameRepositoryTest {

  @Mock
  private lateinit var analytics: Analytics
  @Mock
  private lateinit var gameRepositoryImplementationDelegate: GameRepositoryImplementationDelegate
  @Mock
  private lateinit var lobbyClient: LobbyClient

  private lateinit var gameCache: PlayGameData.GameList.Builder
  private val gameDatas = GameDatas()

  private lateinit var gameRepository: GameRepository


  @Before
  fun setUp() {
    gameCache = PlayGameData.GameList.newBuilder()

    gameRepository = object : GameRepository(analytics = analytics,
        playerOneDefaultName = Lazy { "Pipo" },
        playerTwoDefaultName = "Bimbo",
        gameDatas = gameDatas,
        gameCache = gameCache),
        GameRepositoryImplementationDelegate by gameRepositoryImplementationDelegate {
      override fun getLobbyClient(): LobbyClient {
        return lobbyClient
      }

      override fun onAddOrUpdateLobbyGame(game: Game) {
        TODO("Not yet implemented")
      }
    }
  }

  @After
  fun tearDown() {
    verify(gameRepositoryImplementationDelegate, atLeast(0)).log(safeAnyString())
    verifyNoMoreInteractions(analytics, gameRepositoryImplementationDelegate)
    verifyNoMoreInteractions(lobbyClient)
  }

  @Test
  fun commitGameChanges_localGame() {
    val black = gameDatas.createGamePlayer("pipo", "player1", true)
    val white = gameDatas.createGamePlayer("bimbo", "player2", true)
    val localGame = gameDatas.createNewGameData("pizza", PlayGameData.GameType.LOCAL, black, white)
    assertThat(gameCache.gamesMap.get("pizza")).isNull()

    gameRepository.commitGameChanges(localGame)

    assertThat(gameCache.gamesMap.get("pizza")).isEqualTo(localGame)
    verify(gameRepositoryImplementationDelegate).forceCacheRefresh()
  }

  @Test
  fun commitGameChanges_remoteGame() {
    val black = gameDatas.createGamePlayer("pipo", "player1", true)
    val white = gameDatas.createGamePlayer("bimbo", "player2", true)
    val remoteGame = gameDatas.createNewGameData("123", PlayGameData.GameType.REMOTE, black, white)
    assertThat(gameCache.gamesMap.get("123")).isNull()

    gameRepository.commitGameChanges(remoteGame)

    assertThat(gameCache.gamesMap.get("123")).isEqualTo(remoteGame)
    verify(gameRepositoryImplementationDelegate).forceCacheRefresh()
    // Verify interaction with LobbyClient instead of internal method call
    verify(lobbyClient).sendGameMessage(eq(123), safeEq(remoteGame.toByteArray()))
  }

  private fun safeAnyString(): String {
    Mockito.anyString()
    return ""
  }

  private fun <T> safeEq(value: T): T {
    Mockito.eq(value)
    return value
  }
}


interface GameRepositoryImplementationDelegate {
  fun forceCacheRefresh()
  fun log(message: String)
}
