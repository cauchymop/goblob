package com.cauchymop.goblob.presenter

import com.cauchymop.goblob.model.Analytics
import com.cauchymop.goblob.model.GameDatas
import com.cauchymop.goblob.model.GameRepository
import com.cauchymop.goblob.model.GameSelectionListener
import com.cauchymop.goblob.proto.PlayGameData
import com.cauchymop.goblob.view.GameView
import javax.inject.Inject

class GamePresenter @Inject constructor(private val gameDatas: GameDatas,
                                        private val analytics: Analytics,
                                        private val gameRepository: GameRepository,
                                        private val achievementManager: AchievementManager,
                                        private val updater: GameViewUpdater,
                                        private val feedbackSender: FeedbackSender,
                                        private val goGameControllerFactory: GoGameControllerFactory) : GameSelectionListener {

    private var singleGamePresenter: SingleGamePresenter? = null;

    private var view_: GameView? = null
    var view: GameView
        get() = view_!!
        set(value) {
            view_ = value
            gameRepository.addGameSelectionListener(this)
        }

    private fun updateFromGame(gameData: PlayGameData.GameData?) = gameData?.let {
        singleGamePresenter?.clear()
        val goGameController = goGameControllerFactory.createGameController(gameDatas, gameData, analytics)
        updater.view = view;
        updater.goGameController = goGameController
        view.setConfigurationViewListener(ConfigurationViewEventProcessor(goGameController, updater, gameRepository))
        view.setInGameActionListener(InGameViewEventProcessor(gameDatas, feedbackSender, analytics, goGameController, updater, gameRepository))

        singleGamePresenter = SingleGamePresenter(gameRepository, achievementManager, goGameController, updater)
    }

    override fun gameSelected(gameData: PlayGameData.GameData?) {
        updateFromGame(gameData)
    }

    fun clear() {
        gameRepository.removeGameSelectionListener(this)
        singleGamePresenter?.clear()
        view.setConfigurationViewListener(null)
        view.setInGameActionListener(null)
    }
}
