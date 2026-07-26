package com.cauchymop.goblob.lobby

import net.yura.lobby.model.Game

fun Game.isJoinable(myPlayerName: String): Boolean {
    return getState(myPlayerName) == Game.STATE_CAN_JOIN
}

fun Game.isMyGame(myPlayerName: String): Boolean {
    return hasPlayer(myPlayerName)
}
