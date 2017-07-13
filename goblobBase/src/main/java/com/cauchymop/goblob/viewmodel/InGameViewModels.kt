package com.cauchymop.goblob.viewmodel

import com.cauchymop.goblob.model.GameDatas
import com.cauchymop.goblob.model.GoGameController
import com.cauchymop.goblob.presenter.GameMessageGenerator
import com.cauchymop.goblob.proto.PlayGameData
import com.cauchymop.goblob.proto.PlayGameData.Color
import com.cauchymop.goblob.proto.PlayGameData.Color.BLACK
import com.cauchymop.goblob.proto.PlayGameData.Color.WHITE
import javax.inject.Inject

open class InGameViewModels @Inject constructor(val gameDatas:GameDatas, val gameMessageGenerator: GameMessageGenerator) {

    fun from(goGameController: GoGameController) = with(goGameController) {
        InGameViewModel(
                boardViewModel = getBoardViewModel(goGameController),
                currentPlayerViewModel = PlayerViewModel(currentPlayer.name, currentColor),
                isPassActionAvailable = isLocalTurn && phase == PlayGameData.GameData.Phase.IN_GAME,
                isDoneActionAvailable = isLocalTurn && goGameController.phase == PlayGameData.GameData.Phase.DEAD_STONE_MARKING,
                message = getInGameMessage(goGameController),
                isUndoActionAvailable = canUndo(),
                isRedoActionAvailable = canRedo(),
                isResignActionAvailable = isLocalTurn)
    }

    private fun getBoardViewModel(goGameController: GoGameController) = with(goGameController) {
        var lastMoveX = -1
        var lastMoveY = -1
        val stones = Array<Array<Color?>>(game.boardSize) { arrayOfNulls<Color>(game.boardSize) }
        for (x in 0..game.boardSize - 1) {
            for (y in 0..game.boardSize - 1) {
                stones[y][x] = game.board.getColor(x, y)
                if (game.getPos(x, y) == game.lastMove) {
                    lastMoveX = x
                    lastMoveY = y
                }
            }
        }

        val territories = Array<Array<Color?>>(game.boardSize) { arrayOfNulls<Color>(game.boardSize) }
        goGameController.score.blackTerritoryList.forEach { territories[it.y][it.x] = BLACK }
        goGameController.score.whiteTerritoryList.forEach { territories[it.y][it.x] = WHITE }

        for (position in deadStones) {
            val x = position.x
            val y = position.y
            territories[y][x] = gameDatas.getOppositeColor(stones[y][x])
        }

        BoardViewModel(
                boardSize = game.boardSize,
                stones = stones,
                territories = territories,
                lastMoveX = lastMoveX,
                lastMoveY = lastMoveY,
                isInteractive = isLocalTurn)
    }

    private fun getInGameMessage(goGameController: GoGameController) = with(goGameController) {
        when {
            isGameFinished -> getWinnerMessage(this, getPlayerForColor(score.winner).name)
            phase == PlayGameData.GameData.Phase.DEAD_STONE_MARKING -> gameMessageGenerator.stoneMarkingMessage
            game.isLastMovePass -> gameMessageGenerator.getOpponentPassedMessage(opponent.name)
            else -> ""
        }
    }

    private fun getWinnerMessage(goGameController: GoGameController, winnerName: String) = with(gameMessageGenerator) {
        when {
            goGameController.score.resigned -> getGameResignedMessage(winnerName)
            else -> getEndOfGameMessage(winnerName, goGameController.score.wonBy)
        }
    }
}