package com.cauchymop.goblob.model

import com.cauchymop.goblob.proto.PlayGameData

fun createGameData(phase: PlayGameData.GameData.Phase): PlayGameData.GameData.Builder {
    val gameDatas = GameDatas()
    val black = gameDatas.createGamePlayer("pipo", "player1", true)
    val white = gameDatas.createGamePlayer("bimbo", "player2", true)
    return gameDatas.createNewGameData("matchId", PlayGameData.GameType.LOCAL, black, white).toBuilder().setPhase(phase)
}