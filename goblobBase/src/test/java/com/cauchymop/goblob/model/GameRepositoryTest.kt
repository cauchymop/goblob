package com.cauchymop.goblob.model

import com.cauchymop.goblob.proto.PlayGameData
import com.google.common.truth.Truth.assertThat
import dagger.Lazy
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import java.util.concurrent.atomic.AtomicBoolean

private const val TEST_MATCH_ID = "pizza"

@RunWith(MockitoJUnitRunner::class)
class GameRepositoryTest {

  @Mock
  private lateinit var analytics: Analytics
  @Mock
  private lateinit var gameRepositoryImplementationDelegate: GameRepositoryImplementationDelegate
  @Mock
  private lateinit var gameChangeListener: GameChangeListener;
  @Mock
  private lateinit var gameListListener: GameListListener;

  private val gameDatas = GameDatas()
  private lateinit var gameCache: GameCache
  private lateinit var gameRepository: GameRepository


  @Before
  fun setUp() {
    gameCache = GameCache("", gameDatas)
    gameRepository = object : GameRepository(analytics = analytics,
        playerOneDefaultName = Lazy { "Pipo" },
        playerTwoDefaultName = "Bimbo",
        gameDatas = gameDatas,
        gameCache = gameCache),
        GameRepositoryImplementationDelegate by gameRepositoryImplementationDelegate {}
  }

  @Test
  fun commitGameChanges_savesToCache_andPersistCache() {
    val localGame = createGameData(matchId = TEST_MATCH_ID)
    gameRepository.addGameListListener(gameListListener)
    reset(gameListListener)

    val persistCacheCorrect = AtomicBoolean()
    `when`(gameRepositoryImplementationDelegate.persistCache()).thenAnswer { a ->
      persistCacheCorrect.getAndSet(gameCache.get(TEST_MATCH_ID) == localGame)
      null
    }

    gameRepository.commitGameChanges(localGame)

    assertThat(persistCacheCorrect.get()).isTrue()
    inOrder(gameListListener, gameRepositoryImplementationDelegate).apply {
      verify(gameRepositoryImplementationDelegate).persistCache()
      verify(gameListListener).gameListChanged()
    }
  }

  @Test
  fun commitGameChanges_notifiesGameChangeListeners() {
    val gameData = createGameData()
    gameRepository.addGameChangeListener(gameChangeListener)

    gameRepository.commitGameChanges(gameData)

    verify(gameChangeListener).gameChanged(gameData)
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
  fun persistCache()
  fun publishRemoteGameState(gameData: PlayGameData.GameData): Boolean
  fun log(message: String)
}
