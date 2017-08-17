package com.cauchymop.goblob.model

import com.cauchymop.goblob.proto.PlayGameData

fun createGameData(): PlayGameData.GameData.Builder {
    val gameDatas = GameDatas()
    val black = gameDatas.createGamePlayer("pipo", "player1", true)
    val white = gameDatas.createGamePlayer("bimbo", "player2", true)
    return gameDatas.createNewGameData("matchId", PlayGameData.GameType.LOCAL, black, white).toBuilder().setPhase(PlayGameData.GameData.Phase.IN_GAME)
}