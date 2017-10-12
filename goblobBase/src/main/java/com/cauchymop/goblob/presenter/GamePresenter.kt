package com.cauchymop.goblob.presenter

import com.cauchymop.goblob.model.Analytics
import com.cauchymop.goblob.model.GameDatas
import com.cauchymop.goblob.model.GameRepository
import com.cauchymop.goblob.model.GoGameController
import com.cauchymop.goblob.proto.PlayGameData
import com.cauchymop.goblob.view.GameView
import javax.inject.Inject

class GamePresenter @Inject constructor(private val gameDatas: GameDatas,
                                        private val analytics: Analytics,
                                        private val gameRepository: GameRepository,
                                        private val achievementManager: AchievementManager,
                                        private val updater: GameViewUpdater,
                                        private val configurationViewEventProcessor: ConfigurationViewEventProcessor,
                                        private val inGameViewEventProcessor: InGameViewEventProcessor) : GameRepository.GameRepositoryListener {

    inner class Helper : GamePresenterHelper {
        override fun updateView() = updater.update(goGameController, view)

        override fun commitGameChanges() = with(goGameController!!) {
            val gameData = buildGameData()
            gameRepository.commitGameChanges(gameData)
            helper.updateView()
        }
    }

    private var goGameController: GoGameController? = null
    private val helper: Helper by lazy { Helper() }

    var view: GameView? = null
        set(value) {
            value?.let {
                field = value
                it.setInGameActionListener(inGameViewEventProcessor)
                it.setConfigurationViewListener(configurationViewEventProcessor)
                helper.updateView()
            }
        }

    init {
        gameRepository.addGameRepositoryListener(this)
        inGameViewEventProcessor.helper = helper
        inGameViewEventProcessor.goGameControllerProvider = { goGameController }
        configurationViewEventProcessor.helper = helper
        configurationViewEventProcessor.goGameControllerProvider = { goGameController }
    }

    private fun updateFromGame(gameData: PlayGameData.GameData?) = gameData?.let {
        goGameController = GoGameController(gameDatas, gameData, analytics)
        helper.updateView()

        updateAchievements()
    }

    private fun updateAchievements() = with(goGameController!!) {
        if (!isGameFinished) {
            return
        }
        with(achievementManager) {
            when (game.boardSize) {
                9 -> unlockAchievement9x9()
                13 -> unlockAchievement13x13()
                19 -> unlockAchievement19x19()
            }
            if (isLocalGame) {
                unlockAchievementLocal()
            } else {
                unlockAchievementRemote()
                if (winner.isLocal) {
                    unlockAchievementWinner()
                }
            }
        }
    }

    override fun gameListChanged() {
        // Nothing to do
    }

    override fun gameChanged(gameData: PlayGameData.GameData) {
        goGameController?.run {
            if (gameData.matchId == matchId) {
                updateFromGame(gameData)
            }
        }
    }

    override fun gameSelected(gameData: PlayGameData.GameData?) {
        updateFromGame(gameData)
    }

    fun clear() {
        gameRepository.removeGameRepositoryListener(this)
        view = null
    }

    fun onUndo() {
        if (goGameController!!.undo()) {
            helper.commitGameChanges()
            analytics.undo()
        }
    }

    fun onRedo() {
        if (goGameController!!.redo()) {
            analytics.redo()
            helper.commitGameChanges()
        }
    }

    fun onResign() {
        goGameController!!.resign()
        helper.commitGameChanges()
        analytics.resign()
    }


//    private fun playMonteCarloMove() = with(goGameController!!) {
//        val bestMove = MonteCarlo.getBestMove(game, 1000)
//        val boardSize = gameConfiguration.boardSize
//        val x = bestMove % boardSize
//        val y = bestMove / boardSize
//        playMoveOrToggleDeadStone(gameDatas.createMove(x, y))
//    }

}

