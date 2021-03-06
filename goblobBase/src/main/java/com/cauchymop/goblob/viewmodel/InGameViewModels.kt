package com.cauchymop.goblob.viewmodel

import com.cauchymop.goblob.model.GameDatas
import com.cauchymop.goblob.model.GoGameController
import com.cauchymop.goblob.presenter.GameMessageGenerator
import com.cauchymop.goblob.proto.PlayGameData
import com.cauchymop.goblob.proto.PlayGameData.Color
import com.cauchymop.goblob.proto.PlayGameData.Color.BLACK
import com.cauchymop.goblob.proto.PlayGameData.Color.WHITE
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InGameViewModels @Inject constructor(val gameDatas: GameDatas, val gameMessageGenerator: GameMessageGenerator) {

  fun from(goGameController: GoGameController) = with(goGameController) {
    require(phase >= PlayGameData.GameData.Phase.IN_GAME) { "Trying to create InGameViewModel with Invalid Phase ($phase)" }
    InGameViewModel(
        boardViewModel = getBoardViewModel(goGameController),
        currentPlayerViewModel = PlayerViewModel(currentPlayer.name, currentColor),
        message = getInGameMessage(goGameController),
        isPassActionAvailable = isLocalTurn && phase == PlayGameData.GameData.Phase.IN_GAME,
        isDoneActionAvailable = isLocalTurn && goGameController.phase == PlayGameData.GameData.Phase.DEAD_STONE_MARKING,
        isResignActionAvailable = isLocalTurn,
        isUndoActionAvailable = canUndo(),
        isRedoActionAvailable = canRedo())
  }

  private fun getBoardViewModel(goGameController: GoGameController): BoardViewModel {
    val boardSize = goGameController.boardSize
    val (lastMoveX, lastMoveY) = goGameController.lastMove
    val stones = squareArray(boardSize) { x, y -> goGameController.getColor(x, y) }

    val territories = Array(boardSize) { arrayOfNulls<Color>(boardSize) }
    goGameController.score.blackTerritoryList.forEach { territories[it.y][it.x] = BLACK }
    goGameController.score.whiteTerritoryList.forEach { territories[it.y][it.x] = WHITE }

    for (position in goGameController.deadStones) {
      val x = position.x
      val y = position.y
      territories[y][x] = gameDatas.getOppositeColor(stones[y][x])
    }

    return BoardViewModel(
        boardSize = boardSize,
        stones = stones,
        territories = territories,
        lastMoveX = lastMoveX,
        lastMoveY = lastMoveY,
        isInteractive = goGameController.isLocalTurn)
  }

  private fun getInGameMessage(goGameController: GoGameController) = with(goGameController) {
    when {
      isGameFinished -> getWinnerMessage(this, getPlayerForColor(score.winner).name)
      phase == PlayGameData.GameData.Phase.DEAD_STONE_MARKING -> gameMessageGenerator.stoneMarkingMessage
      isLastMovePass -> gameMessageGenerator.getOpponentPassedMessage(opponent.name)
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

private inline fun <reified T> squareArray(size: Int, f: (Int, Int) -> T?):Array<Array<T?>> {
  return Array(size) { y -> Array(size) { x -> f(x, y) } }
}

