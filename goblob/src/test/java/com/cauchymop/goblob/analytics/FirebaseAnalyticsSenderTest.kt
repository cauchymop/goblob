package com.cauchymop.goblob.analytics

import com.cauchymop.goblob.logger.EventLogger
import com.cauchymop.goblob.model.GameDatas
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class FirebaseAnalyticsSenderTest {

  @Mock
  private lateinit var eventLogger: EventLogger

  private lateinit var analyticsSender: FirebaseAnalyticsSender

  private val gameDatas:GameDatas =  GameDatas()

  @Before
  @Throws(Exception::class)
  fun setUp() {
    analyticsSender = FirebaseAnalyticsSender(eventLogger)
  }

  @After
  @Throws(Exception::class)
  fun tearDown() {
  }

//  @Test
//  fun gameCreated() {
//    val black = gameDatas.createGamePlayer("pipo", "player1", true)
//    val white = gameDatas.createGamePlayer("bimbo", "player2", true)
//    val localGame = gameDatas.createNewGameData("pizza", PlayGameData.GameType.LOCAL, black, white)
//
//    analyticsSender.gameCreated(localGame)
//
//    val captor = ArgumentCaptor.forClass(Bundle::class.java)
//    verify(eventLogger).logEvent("undo", captor.capture())
//    assertThat(captor.value.getString("type")).isEqualTo("local")
//  }

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
}