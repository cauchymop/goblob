package com.cauchymop.goblob.presenter

import com.cauchymop.goblob.model.GameChangeListener
import com.cauchymop.goblob.model.GameRepository
import com.cauchymop.goblob.model.GoGameController
import com.cauchymop.goblob.proto.PlayGameData

class SingleGamePresenter(private val gameRepository: GameRepository,
                          private val achievementManager: AchievementManager,
                          goGameController: GoGameController,
                          updater: GameViewUpdater) : GameEventProcessor(goGameController, updater, gameRepository), GameChangeListener {

    init {
        gameRepository.addGameChangeListener(this)
        gameUpdated()
    }

    override fun gameChanged(gameData: PlayGameData.GameData) {
        goGameController.run {
            if (gameData.matchId == matchId) {
                gameUpdated()
            }
        }
    }

    private fun gameUpdated() {
        achievementManager.updateAchievements(goGameController)
        updateView()
    }

    fun clear() {
        gameRepository.removeGameChangeListener(this)
    }


//    private fun playMonteCarloMove() = with(goGameController!!) {
//        val bestMove = MonteCarlo.getBestMove(game, 1000)
//        val boardSize = gameConfiguration.boardSize
//        val x = bestMove % boardSize
//        val y = bestMove / boardSize
//        playMoveOrToggleDeadStone(gameDatas.createMove(x, y))
//    }

}

