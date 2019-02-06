package com.cauchymop.goblob.model

import com.cauchymop.goblob.proto.PlayGameData
import com.google.common.truth.Truth.assertThat
import dagger.Lazy
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class GameRepositoryTest {

  @Mock
  private lateinit var analytics: Analytics
  @Mock
  private lateinit var gameRepositoryImplementationDelegate: GameRepositoryImplementationDelegate

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
        gameCache = gameCache) {
      override fun forceCacheRefresh() {
        gameRepositoryImplementationDelegate.forceCacheRefresh()
      }

      override fun publishRemoteGameState(gameData: PlayGameData.GameData): Boolean {
        return gameRepositoryImplementationDelegate.publishRemoteGameState(gameData)
      }

      override fun log(message: String) {
      }
    }
  }

  @After
  fun tearDown() {
    verifyNoMoreInteractions(analytics, gameRepositoryImplementationDelegate)
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
    val remoteGame = gameDatas.createNewGameData("pizza", PlayGameData.GameType.REMOTE, black, white)
    assertThat(gameCache.gamesMap.get("pizza")).isNull()

    gameRepository.commitGameChanges(remoteGame)

    assertThat(gameCache.gamesMap.get("pizza")).isEqualTo(remoteGame)
    verify(gameRepositoryImplementationDelegate).forceCacheRefresh()
    verify(gameRepositoryImplementationDelegate).publishRemoteGameState(remoteGame)
  }

}


interface GameRepositoryImplementationDelegate {
  fun forceCacheRefresh()
  fun publishRemoteGameState(gameData: PlayGameData.GameData): Boolean
}