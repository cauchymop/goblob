package com.cauchymop.goblob.lobby

import net.yura.lobby.model.Game

fun Game.isOnGoingGameFromOtherPlayers(myPlayerName:String) = players.size == 2 &&
        !players.map { it.toString() }.contains(myPlayerName)

fun Game.isMyGameWaitingForOpponent(myPlayerName:String) = players.size == 1 &&
        players.map { it.toString() }.contains(myPlayerName)

fun Game.isMyGameReadyToPlay(myPlayerName:String) = players.size == 2 &&
        players.map { it.toString() }.contains(myPlayerName)