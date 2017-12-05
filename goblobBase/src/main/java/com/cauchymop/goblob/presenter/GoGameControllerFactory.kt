package com.cauchymop.goblob.presenter

import com.cauchymop.goblob.model.Analytics
import com.cauchymop.goblob.model.GameDatas
import com.cauchymop.goblob.model.GoGameController
import com.cauchymop.goblob.proto.PlayGameData
import javax.inject.Inject

class GoGameControllerFactory @Inject constructor() {
    fun createGameController(gameDatas: GameDatas, gameData: PlayGameData.GameData, analytics: Analytics):GoGameController = GoGameController(gameDatas, gameData, analytics)
}