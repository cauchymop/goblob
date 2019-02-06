package com.cauchymop.goblob.analytics

import any
import capture
import com.cauchymop.goblob.logger.EventLogger
import com.cauchymop.goblob.model.GameDatas
import com.cauchymop.goblob.proto.PlayGameData
import com.google.common.truth.Truth.assertThat
import eq
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class FirebaseAnalyticsSenderTest {

  @Mock
  private lateinit var eventLogger: EventLogger

  private lateinit var analyticsSender: FirebaseAnalyticsSender

  private val gameDatas: GameDatas = GameDatas()

  @Before
  @Throws(Exception::class)
  fun setUp() {
    analyticsSender = FirebaseAnalyticsSender(eventLogger)
  }

  @After
  @Throws(Exception::class)
  fun tearDown() {
  }

  @Test
  fun gameCreated() {
    val black = gameDatas.createGamePlayer("pipo", "player1", true)
    val white = gameDatas.createGamePlayer("bimbo", "player2", true)
    val localGame = gameDatas.createNewGameData("pizza", PlayGameData.GameType.LOCAL, black, white)

    analyticsSender.gameCreated(localGame)

    val captor = ArgumentCaptor.forClass(Map::class.java)
    verify(eventLogger).logEvent(eq("gameCreated"), capture(captor) as Map<String, String>?)
    assertThat(captor.value).containsExactly("type", "LOCAL", "size", "9", "handicap", "0")
  }

  @Test
  fun configurationChanged_local() {
    val black = gameDatas.createGamePlayer("pipo", "player1", true)
    val white = gameDatas.createGamePlayer("bimbo", "player2", true)
    val localGame = gameDatas.createNewGameData("pizza", PlayGameData.GameType.LOCAL, black, white)

    analyticsSender.configurationChanged(localGame)

    verify(eventLogger, never()).logEvent(any(), any())
  }

  @Test
  fun configurationChanged_remote_not_agreed() {
    val black = gameDatas.createGamePlayer("pipo", "player1", true)
    val white = gameDatas.createGamePlayer("bimbo", "player2", true)
    val remoteGame = gameDatas.createNewGameData("pizza", PlayGameData.GameType.REMOTE, black, white)

    analyticsSender.configurationChanged(remoteGame)

    val captor = ArgumentCaptor.forClass(Map::class.java)
    verify(eventLogger).logEvent(eq("configurationChanged"), capture(captor) as Map<String, String>?)
    assertThat(captor.value).containsExactly("type", "REMOTE", "size", "9", "handicap", "0", "agreed", "false")
  }

  @Test
  fun configurationChanged_remote_agreed() {
    val black = gameDatas.createGamePlayer("pipo", "player1", true)
    val white = gameDatas.createGamePlayer("bimbo", "player2", true)
    val remoteGame = gameDatas.createNewGameData("pizza", PlayGameData.GameType.REMOTE, black, white)
        .toBuilder().setPhase(PlayGameData.GameData.Phase.IN_GAME).build()

    analyticsSender.configurationChanged(remoteGame)

    val captor = ArgumentCaptor.forClass(Map::class.java)
    verify(eventLogger).logEvent(eq("configurationChanged"), capture(captor) as Map<String, String>?)
    assertThat(captor.value).containsExactly("type", "REMOTE", "size", "9", "handicap", "0", "agreed", "true")
  }

  @Test
  fun undo() {
    analyticsSender.undo()

    verify(eventLogger).logEvent("undo", null)
  }

  @Test
  fun redo() {
    analyticsSender.redo()

    verify(eventLogger).logEvent("redo", null)
  }

  @Test
  fun resign() {
    analyticsSender.resign()

    verify(eventLogger).logEvent("resign", null)
  }

  @Test
  fun movePlayed_passed() {
    val gameConfig = PlayGameData.GameConfiguration.newBuilder()
        .setBoardSize(9)
        .setGameType(PlayGameData.GameType.LOCAL)
        .setHandicap(0)
        .setKomi(7.5f)
        .setScoreType(PlayGameData.GameConfiguration.ScoreType.CHINESE)
        .build()
    val move = PlayGameData.Move.newBuilder().setType(PlayGameData.Move.MoveType.PASS).build()

    analyticsSender.movePlayed(gameConfig, move)

    val captor = ArgumentCaptor.forClass(Map::class.java)
    verify(eventLogger).logEvent(eq("passed"), capture(captor) as Map<String, String>?)
    assertThat(captor.value).containsExactly("type", "LOCAL", "size", "9", "handicap", "0")
  }

  @Test
  fun movePlayed_played() {
    val gameConfig = PlayGameData.GameConfiguration.newBuilder()
        .setBoardSize(9)
        .setGameType(PlayGameData.GameType.LOCAL)
        .setHandicap(0)
        .setKomi(7.5f)
        .setScoreType(PlayGameData.GameConfiguration.ScoreType.CHINESE)
        .build()
    val move = PlayGameData.Move.newBuilder().setType(PlayGameData.Move.MoveType.MOVE).build()

    analyticsSender.movePlayed(gameConfig, move)

    val captor = ArgumentCaptor.forClass(Map::class.java)
    verify(eventLogger).logEvent(eq("movePlayed"), capture(captor) as Map<String, String>?)
    assertThat(captor.value).containsExactly("type", "LOCAL", "size", "9", "handicap", "0")
  }

  @Test
  fun deadStoneToggled() {
    val gameConfig = PlayGameData.GameConfiguration.newBuilder()
        .setBoardSize(9)
        .setGameType(PlayGameData.GameType.LOCAL)
        .setHandicap(0)
        .setKomi(7.5f)
        .setScoreType(PlayGameData.GameConfiguration.ScoreType.CHINESE)
        .build()

    analyticsSender.deadStoneToggled(gameConfig)

    val captor = ArgumentCaptor.forClass(Map::class.java)
    verify(eventLogger).logEvent(eq("deadStoneToggled"), capture(captor) as Map<String, String>?)
    assertThat(captor.value).containsExactly("type", "LOCAL", "size", "9", "handicap", "0")
  }

  @Test
  fun invalidMovePlayed() {
    val gameConfig = PlayGameData.GameConfiguration.newBuilder()
        .setBoardSize(9)
        .setGameType(PlayGameData.GameType.LOCAL)
        .setHandicap(0)
        .setKomi(7.5f)
        .setScoreType(PlayGameData.GameConfiguration.ScoreType.CHINESE)
        .build()

    analyticsSender.invalidMovePlayed(gameConfig)

    val captor = ArgumentCaptor.forClass(Map::class.java)
    verify(eventLogger).logEvent(eq("invalidMovePlayed"), capture(captor) as Map<String, String>?)
    assertThat(captor.value).containsExactly("type", "LOCAL", "size", "9", "handicap", "0")
  }

  @Test
  fun gameFinished() {
    val gameConfig = PlayGameData.GameConfiguration.newBuilder()
        .setBoardSize(9)
        .setGameType(PlayGameData.GameType.LOCAL)
        .setHandicap(0)
        .setKomi(7.5f)
        .setScoreType(PlayGameData.GameConfiguration.ScoreType.CHINESE)
        .build()
    val score = PlayGameData.Score.newBuilder()
        .setResigned(false)
        .setWinner(PlayGameData.Color.BLACK)
        .setWonBy(3f)
        .build()

    analyticsSender.gameFinished(gameConfig, score)

    val captor = ArgumentCaptor.forClass(Map::class.java)
    verify(eventLogger).logEvent(eq("gameFinished"), capture(captor) as Map<String, String>?)
    assertThat(captor.value).containsExactly("type", "LOCAL", "size", "9", "handicap", "0", "resigned", "false", "wonBy", "3.0", "winner", "BLACK")
  }


}