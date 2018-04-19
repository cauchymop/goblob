package com.cauchymop.goblob.presenter

import com.cauchymop.goblob.model.GameRepository
import com.cauchymop.goblob.model.GoGameController

abstract class GameEventProcessor(protected val goGameController: GoGameController,
                                  private val updater: GameViewUpdater,
                                  private val gameRepository: GameRepository) {

    protected fun updateView() {
        updater.update()
    }

    protected fun commitGameChanges() {
        with(goGameController) {
            gameRepository.commitGameChanges(gameData)
            updateView()
        }
    }
}