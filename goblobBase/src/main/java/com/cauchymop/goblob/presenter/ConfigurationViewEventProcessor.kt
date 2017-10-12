package com.cauchymop.goblob.presenter

import com.cauchymop.goblob.model.GoGameController
import javax.inject.Inject

class ConfigurationViewEventProcessor @Inject constructor() : ConfigurationEventListener {

    lateinit var helper: GamePresenterHelper
    lateinit var goGameControllerProvider: () -> GoGameController?

    override fun onBlackPlayerNameChanged(blackPlayerName: String) =
        goGameControllerProvider.invoke()!!.setBlackPlayerName(blackPlayerName)

    override fun onWhitePlayerNameChanged(whitePlayerName: String) =
        goGameControllerProvider.invoke()!!.setWhitePlayerName(whitePlayerName)

    override fun onHandicapChanged(handicap: Int) =
        goGameControllerProvider.invoke()!!.setHandicap(handicap)

    override fun onKomiChanged(komi: Float) =
        goGameControllerProvider.invoke()!!.setKomi(komi)

    override fun onBoardSizeChanged(boardSize: Int) =
        goGameControllerProvider.invoke()!!.setBoardSize(boardSize)

    override fun onSwapEvent() {
        goGameControllerProvider.invoke()!!.swapPlayers()
        helper.updateView()
    }

    override fun onConfigurationValidationEvent() {
        goGameControllerProvider.invoke()!!.validateConfiguration()
        helper.commitGameChanges()
    }

}