package com.cauchymop.goblob.presenter

import com.cauchymop.goblob.model.Analytics
import com.cauchymop.goblob.model.GameDatas
import com.cauchymop.goblob.model.GoGameController
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class InGameViewEventProcessorTest {

    @Mock private lateinit var analytics: Analytics
    @Mock private lateinit var feedbackSender: FeedbackSender
    @Mock private lateinit var gamePresenterHelper: GamePresenterHelper
    @Mock private lateinit var goGameController: GoGameController

    private lateinit var inGameViewEventProcessor: InGameViewEventProcessor
    private val gameDatas = GameDatas()

    @Before
    fun setUp() {
        inGameViewEventProcessor = InGameViewEventProcessor(gameDatas = gameDatas, feedbackSender = feedbackSender)
        inGameViewEventProcessor.helper = gamePresenterHelper
        inGameViewEventProcessor.goGameControllerProvider = { goGameController }
    }

    @After
    fun tearDown() {
        verifyNoMoreInteractions(analytics, feedbackSender, gamePresenterHelper, goGameController)
    }

    @Test
    fun onIntersectionSelected_localTurn() {
        val move = gameDatas.createMove(3, 4)
        given(goGameController.isLocalTurn).willReturn(true)
        given(goGameController.playMoveOrToggleDeadStone(move)).willReturn(true)
        inGameViewEventProcessor.onIntersectionSelected(3, 4)

        verify(goGameController).playMoveOrToggleDeadStone(move)
        verify(goGameController).isLocalTurn
        verify(gamePresenterHelper).commitGameChanges()
    }

    @Test
    fun onIntersectionSelected_localTurn_invalidMove() {
        val move = gameDatas.createMove(3, 4)
        given(goGameController.isLocalTurn).willReturn(true)
        given(goGameController.playMoveOrToggleDeadStone(move)).willReturn(false)
        inGameViewEventProcessor.onIntersectionSelected(3, 4)

        verify(goGameController).playMoveOrToggleDeadStone(move)
        verify(goGameController).isLocalTurn
        verify(feedbackSender).invalidMove()
    }

    @Test
    fun onIntersectionSelected_notLocalTurn() {
        given(goGameController.isLocalTurn).willReturn(false)

        inGameViewEventProcessor.onIntersectionSelected(3, 4)

        verify(goGameController).isLocalTurn
    }

    @Test
    fun onPass() {
        inGameViewEventProcessor.onPass()

        verify(goGameController).pass()
        verify(gamePresenterHelper).commitGameChanges()
    }

    @Test
    fun onDone() {
        inGameViewEventProcessor.onDone()

        verify(goGameController).markingTurnDone()
        verify(gamePresenterHelper).commitGameChanges()
    }

}