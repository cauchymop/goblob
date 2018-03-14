package com.cauchymop.goblob.presenter

import com.cauchymop.goblob.model.Analytics
import com.cauchymop.goblob.model.GameDatas
import com.cauchymop.goblob.model.GameRepository
import com.cauchymop.goblob.model.GoGameController
import com.cauchymop.goblob.proto.PlayGameData
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

    @Mock
    private lateinit var analytics: Analytics
    @Mock
    private lateinit var feedbackSender: FeedbackSender
    @Mock
    private lateinit var goGameController: GoGameController
    @Mock
    private lateinit var gameRepository: GameRepository
    @Mock
    private lateinit var gameViewUpdater: GameViewUpdater

    private lateinit var inGameViewEventProcessor: InGameViewEventProcessor

    private val gameDatas = GameDatas()
    private val gameData = PlayGameData.GameData.getDefaultInstance()

    @Before
    fun setUp() {
        inGameViewEventProcessor = InGameViewEventProcessor(gameDatas = gameDatas,
                feedbackSender = feedbackSender,
                analytics = analytics,
                goGameController = goGameController,
                updater = gameViewUpdater,
                gameRepository = gameRepository)


        given(goGameController.buildGameData()).willReturn(gameData)
    }

    @After
    fun tearDown() {
        verifyNoMoreInteractions(analytics, feedbackSender, goGameController)
    }

    @Test
    fun onIntersectionSelected_localTurn() {
        val move = gameDatas.createMove(3, 4)
        given(goGameController.isLocalTurn).willReturn(true)
        given(goGameController.playMoveOrToggleDeadStone(move)).willReturn(true)
        inGameViewEventProcessor.onIntersectionSelected(3, 4)

        verify(goGameController).playMoveOrToggleDeadStone(move)
        verify(goGameController).isLocalTurn
        verify(goGameController).buildGameData()
        verify(gameRepository).commitGameChanges(gameData)
        verify(gameViewUpdater).update()
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
        verify(goGameController).buildGameData()
        verify(gameRepository).commitGameChanges(gameData)
        verify(gameViewUpdater).update()
    }

    @Test
    fun onDone() {
        inGameViewEventProcessor.onDone()

        verify(goGameController).markingTurnDone()
        verify(goGameController).buildGameData()
        verify(gameRepository).commitGameChanges(gameData)
        verify(gameViewUpdater).update()
    }

    @Test
    fun onUndo() {
        given(goGameController.undo()).willReturn(true)

        inGameViewEventProcessor.onUndo()

        verify(analytics).undo()
        verify(goGameController).undo()
        verify(goGameController).buildGameData()
        verify(gameRepository).commitGameChanges(gameData)
        verify(gameViewUpdater).update()
    }

    @Test
    fun onRedo() {
        given(goGameController.redo()).willReturn(true)

        inGameViewEventProcessor.onRedo()

        verify(analytics).redo()
        verify(goGameController).redo()
        verify(goGameController).buildGameData()
        verify(gameRepository).commitGameChanges(gameData)
        verify(gameViewUpdater).update()
    }

    @Test
    fun onResign() {
        inGameViewEventProcessor.onResign()

        verify(analytics).resign()
        verify(goGameController).resign()
        verify(goGameController).buildGameData()
        verify(gameRepository).commitGameChanges(gameData)
        verify(gameViewUpdater).update()
    }

}