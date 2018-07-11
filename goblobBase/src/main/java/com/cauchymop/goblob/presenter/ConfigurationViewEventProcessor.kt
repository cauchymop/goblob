package com.cauchymop.goblob.presenter

import com.cauchymop.goblob.model.GameRepository
import com.cauchymop.goblob.model.GoGameController
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigurationViewEventProcessor @Inject constructor(goGameController: GoGameController,
                                                          updater: GameViewUpdater,
                                                          gameRepository: GameRepository) : GameEventProcessor(goGameController, updater, gameRepository), ConfigurationEventListener {

    override fun onBlackPlayerNameChanged(blackPlayerName: String) =
        goGameController.setBlackPlayerName(blackPlayerName)

    override fun onWhitePlayerNameChanged(whitePlayerName: String) =
        goGameController.setWhitePlayerName(whitePlayerName)

    override fun onHandicapChanged(handicap: Int) =
        goGameController.setHandicap(handicap)

    override fun onKomiChanged(komi: Float) =
        goGameController.setKomi(komi)

    override fun onBoardSizeChanged(boardSize: Int) =
        goGameController.setBoardSize(boardSize)

    override fun onSwapEvent() {
        goGameController.swapPlayers()
        updateView()
    }

    override fun onConfigurationValidationEvent() {
        goGameController.commitConfiguration()
        commitGameChanges()
    }

}