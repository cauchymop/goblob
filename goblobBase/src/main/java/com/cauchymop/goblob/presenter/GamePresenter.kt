package com.cauchymop.goblob.presenter

import com.cauchymop.goblob.model.Analytics
import com.cauchymop.goblob.model.GameDatas
import com.cauchymop.goblob.model.GameRepository
import com.cauchymop.goblob.model.GoGameController
import com.cauchymop.goblob.proto.PlayGameData
import com.cauchymop.goblob.view.GameView
import com.cauchymop.goblob.view.InGameView
import javax.inject.Inject

class GamePresenter @Inject constructor(private val gameDatas: GameDatas,
                                        private val analytics: Analytics,
                                        private val gameRepository: GameRepository,
                                        private val achievementManager: AchievementManager,
                                        private val updater: GameViewUpdater) : GameRepository.GameRepositoryListener, ConfigurationEventListener, InGameView.InGameEventListener {

    private var goGameController: GoGameController? = null

    var view: GameView? = null
        set(value) {
            value?.let {
                field = value
                it.setInGameActionListener(this)
                it.setConfigurationViewListener(this)
                updateView()
            }
        }

    init {
        gameRepository.addGameRepositoryListener(this)
    }

    private fun updateFromGame(gameData: PlayGameData.GameData?) = gameData?.let {
        goGameController = GoGameController(gameDatas, gameData, analytics)
        updateView()

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

    override fun gameChanged(gameData: PlayGameData.GameData) = with(goGameController) {
        if (gameData.matchId == this?.matchId) {
            updateFromGame(gameData)
        }
    }

    override fun gameSelected(gameData: PlayGameData.GameData?) {
        updateFromGame(gameData)
    }

    fun clear() {
        gameRepository.removeGameRepositoryListener(this)
        view = null
    }

    override fun onBlackPlayerNameChanged(blackPlayerName: String) = with(goGameController!!) {
        if (gameConfiguration.black.name != blackPlayerName) {
            setBlackPlayerName(blackPlayerName)
        }
    }

    override fun onWhitePlayerNameChanged(whitePlayerName: String) = with(goGameController!!) {
        if (gameConfiguration.white.name != whitePlayerName) {
            setWhitePlayerName(whitePlayerName)
        }
    }

    override fun onHandicapChanged(handicap: Int) = with(goGameController!!) {
        if (gameConfiguration.handicap != handicap) {
            setHandicap(handicap)
        }
    }

    override fun onKomiChanged(komi: Float) = with(goGameController!!) {
        if (gameConfiguration.komi != komi) {
            setKomi(komi)
        }
    }

    override fun onBoardSizeChanged(boardSize: Int) = with(goGameController!!) {
        if (gameConfiguration.boardSize != boardSize) {
            setBoardSize(boardSize)
        }
    }

    override fun onSwapEvent() {
        goGameController!!.swapPlayers()
        updateView()
    }

    private fun updateView() = updater.update(goGameController, view)

    override fun onConfigurationValidationEvent() {
        goGameController!!.validateConfiguration()
        commitGameChanges()
    }

    override fun onIntersectionSelected(x: Int, y: Int) = with(goGameController!!) {
        if (isLocalTurn) {
            val played = playMoveOrToggleDeadStone(gameDatas.createMove(x, y))

            if (played) {
                commitGameChanges()
            } else {
                view?.buzz()
                analytics.invalidMovePlayed(gameConfiguration)
            }
        }
    }

    override fun onPass() {
        goGameController!!.pass()
        commitGameChanges()
    }

    override fun onDone() {
        goGameController!!.markingTurnDone()
        commitGameChanges()
    }

    fun onUndo() {
        if (goGameController!!.undo()) {
            commitGameChanges()
            analytics.undo()
        }
    }

    fun onRedo() {
        if (goGameController!!.redo()) {
            analytics.redo()
            commitGameChanges()
        }
    }

    fun onResign() {
        goGameController!!.resign()
        commitGameChanges()
        analytics.resign()
    }

    private fun commitGameChanges() = with(goGameController!!) {
        val gameData = buildGameData()
        gameRepository.commitGameChanges(gameData)
        updateView()
    }

//    private fun playMonteCarloMove() = with(goGameController!!) {
//        val bestMove = MonteCarlo.getBestMove(game, 1000)
//        val boardSize = gameConfiguration.boardSize
//        val x = bestMove % boardSize
//        val y = bestMove / boardSize
//        playMoveOrToggleDeadStone(gameDatas.createMove(x, y))
//    }

}

