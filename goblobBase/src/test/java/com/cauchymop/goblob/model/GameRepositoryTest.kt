package com.cauchymop.goblob.model

import com.cauchymop.goblob.proto.PlayGameData
import com.google.common.truth.Truth.assertThat
import dagger.Lazy
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

private const val TEST_MATCH_ID = "pizza"

@RunWith(MockitoJUnitRunner::class)
class GameRepositoryTest {

  @Mock
  private lateinit var analytics: Analytics
  @Mock
  private lateinit var gameRepositoryImplementationDelegate: GameRepositoryImplementationDelegate
  @Mock
  private lateinit var gameChangeListener: GameChangeListener;

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
        GameRepositoryImplementationDelegate by gameRepositoryImplementationDelegate {}
  }

  @Test
  fun commitGameChanges_savesToCache() {
    val localGame = createGameData(matchId = TEST_MATCH_ID)
    assertThat(gameCache.gamesMap.get(TEST_MATCH_ID)).isNull()

    gameRepository.commitGameChanges(localGame)

    assertThat(gameCache.gamesMap.get(TEST_MATCH_ID)).isEqualTo(localGame)
  }

  @Test
  fun commitGameChanges_notifiesGameChangeListeners() {
    val gameData = createGameData()
    gameRepository.addGameChangeListener(gameChangeListener)

    gameRepository.commitGameChanges(gameData)

    verify(gameChangeListener).gameChanged(gameData)
  }

  @Test
  fun commitGameChanges_callsImplements_forceCacheRefresh() {
    gameRepository.commitGameChanges(createGameData())

    verify(gameRepositoryImplementationDelegate).forceCacheRefresh()
  }

  @Test
  fun commitGameChanges_publish_remoteGame() {
    val remoteGame = createGameData(gameType = PlayGameData.GameType.REMOTE)

    gameRepository.commitGameChanges(remoteGame)

    verify(gameRepositoryImplementationDelegate).publishRemoteGameState(remoteGame)
  }

  private fun createGameData(gameType: PlayGameData.GameType = PlayGameData.GameType.LOCAL, matchId:String = TEST_MATCH_ID): PlayGameData.GameData {
    val black = gameDatas.createGamePlayer("pipo", "player1", true)
    val white = gameDatas.createGamePlayer("bimbo", "player2", true)
    return gameDatas.createNewGameData(matchId, gameType, black, white)
  }

}


interface GameRepositoryImplementationDelegate {
  fun forceCacheRefresh()
  fun publishRemoteGameState(gameData: PlayGameData.GameData): Boolean
  fun log(message: String)
}
