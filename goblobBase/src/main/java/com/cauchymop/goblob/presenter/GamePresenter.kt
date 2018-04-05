package com.cauchymop.goblob.presenter

import com.cauchymop.goblob.model.*
import com.cauchymop.goblob.proto.PlayGameData
import com.cauchymop.goblob.view.GameView
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GamePresenter @Inject constructor(private val gameDatas: GameDatas,
                                        private val analytics: Analytics,
                                        private val gameRepository: GameRepository,
                                        private val achievementManager: AchievementManager,
                                        private val updater: GameViewUpdater,
                                        private val feedbackSender: FeedbackSender,
                                        goGameController: GoGameController) : GameEventProcessor(goGameController, updater, gameRepository), GameSelectionListener, GameChangeListener {

  private var view_: GameView? = null
  var view: GameView
    get() = view_!!
    set(value) {
      view_ = value
      updater.view = value
      gameRepository.addGameSelectionListener(this)
      gameRepository.addGameChangeListener(this)
      view.setConfigurationViewListener(ConfigurationViewEventProcessor(goGameController, updater, gameRepository))
      view.setInGameActionListener(InGameViewEventProcessor(gameDatas, feedbackSender, analytics, goGameController, updater, gameRepository))
      gameUpdated()

    }

  override fun gameSelected(gameData: PlayGameData.GameData?) {
    if (gameData != null) {
      goGameController.setGameData(gameData)
      updater.update()
    }
  }

  override fun gameChanged(gameData: PlayGameData.GameData) {
    goGameController.let {
      if (gameData.matchId == it.matchId) {
        if (gameData.gameConfiguration.gameType == PlayGameData.GameType.REMOTE) {
          it.setGameData(gameData)
        }
        gameUpdated()
      }
    }
  }

  private fun gameUpdated() {
    achievementManager.updateAchievements(goGameController)
    updateView()
  }

  fun clear() {
    gameRepository.removeGameSelectionListener(this)
    gameRepository.removeGameChangeListener(this)
    view.setConfigurationViewListener(null)
    view.setInGameActionListener(null)
  }

//  private fun playMonteCarloMove() = with(goGameController!!) {
//    val bestMove = MonteCarlo.getBestMove(game, 1000)
//    val boardSize = gameConfiguration.boardSize
//    val x = bestMove % boardSize
//    val y = bestMove / boardSize
//    playMoveOrToggleDeadStone(gameDatas.createMove(x, y))
//  }

}
