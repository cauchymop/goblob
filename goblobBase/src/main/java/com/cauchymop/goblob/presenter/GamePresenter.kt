package com.cauchymop.goblob.presenter

import com.cauchymop.goblob.model.*
import com.cauchymop.goblob.proto.PlayGameData
import com.cauchymop.goblob.view.GameView
import javax.inject.Inject
import kotlin.properties.Delegates

class GamePresenter @Inject constructor(private val gameDatas: GameDatas,
                                        private val analytics: Analytics,
                                        private val gameRepository: GameRepository,
                                        private val achievementManager: AchievementManager,
                                        private val updater: GameViewUpdater,
                                        private val feedbackSender: FeedbackSender,
                                        private val goGameController: GoGameController) : GameSelectionListener {


  private var singleGamePresenter:SingleGamePresenter? = null

  private var view_: GameView? = null
  var view: GameView
    get() = view_!!
    set(value) {
      view_ = value
      updater.view = value
      gameRepository.addGameSelectionListener(this)
      view.setConfigurationViewListener(ConfigurationViewEventProcessor(goGameController, updater, gameRepository))
      view.setInGameActionListener(InGameViewEventProcessor(gameDatas, feedbackSender, analytics, goGameController, updater, gameRepository))

    }

  override fun gameSelected(gameData: PlayGameData.GameData?) {
    if (gameData != null) {
      goGameController.setGameData(gameData)
      if (singleGamePresenter == null) {
        singleGamePresenter = SingleGamePresenter(gameRepository, achievementManager, goGameController, updater)
      }
    }
  }

  fun clear() {
    gameRepository.removeGameSelectionListener(this)
    singleGamePresenter?.clear()
    view.setConfigurationViewListener(null)
    view.setInGameActionListener(null)
  }
}
