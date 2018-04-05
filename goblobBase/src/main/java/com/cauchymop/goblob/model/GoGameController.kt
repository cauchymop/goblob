package com.cauchymop.goblob.model

import com.cauchymop.goblob.proto.PlayGameData
import com.cauchymop.goblob.proto.PlayGameData.*
import com.cauchymop.goblob.proto.PlayGameData.GameData.Phase
import com.cauchymop.goblob.proto.PlayGameData.GameData.Phase.*
import com.cauchymop.goblob.proto.PlayGameData.GameType.LOCAL
import com.google.common.base.Preconditions
import com.google.common.collect.Sets
import java.io.Serializable
import javax.inject.Inject
import kotlin.properties.Delegates

/**
 * Class to handle interactions between the players and the [GoGame].
 */
class GoGameController @Inject constructor(@field:Transient internal var gameDatas: GameDatas, private val analytics: Analytics) : Serializable {

  var game: GoGame? = null
    private set

  var gameData: GameData.Builder by Delegates.notNull()
   private set

  private var initialGameData: GameData by Delegates.notNull()

  val score: Score
    get() = matchEndStatus.score

  val currentPlayer: GoPlayer
    get() = gameDatas.getCurrentPlayer(gameData)

  val opponent: GoPlayer
    get() = gameDatas.getGoPlayer(gameData, opponentColor)

  val currentColor: Color
    get() = gameDatas.getCurrentColor(gameData)

  val gameConfiguration: GameConfiguration
    get() = gameData.gameConfiguration

  val isLocalTurn: Boolean
    get() = gameDatas.isLocalTurn(gameData)

  val deadStones: List<PlayGameData.Position>
    get() = matchEndStatus.deadStoneList

  val isLocalGame: Boolean
    get() = gameDatas.isLocalGame(gameData)

  private val opponentColor: Color
    get() = GoBoard.getOpponent(currentColor)

  private val matchEndStatus: MatchEndStatus
    get() = gameData.matchEndStatus

  private val whitePlayer: GoPlayer
    get() = gameConfiguration.white

  private val blackPlayer: GoPlayer
    get() = gameConfiguration.black

  val isGameFinished: Boolean
    get() = gameData.phase == FINISHED

  val phase: Phase
    get() = gameData.phase

  val winner: GoPlayer
    get() = gameDatas.getGoPlayer(gameData, gameData.matchEndStatus.score.winner)

  val matchId: String
    get() = gameData.matchId


  fun setGameData(gameData: GameData) {
    this.initialGameData = gameData
    this.gameData = Preconditions.checkNotNull(gameData).toBuilder()
    // The GoGame settings can change during the configuration, so we postpone its creation.
    if (gameData.phase != CONFIGURATION) {
      createGoGame()
      for (move in this.gameData.moveList) {
        game!!.play(getPos(move))
      }
    }
  }

  fun undo(): Boolean {
    if (canUndo()) {
      gameData.addRedo(0, removeLastMove())
      game!!.undo()
      return true
    }
    return false
  }

  fun redo(): Boolean {
    if (canRedo()) {
      playMove(gameData.getRedo(0))
      return true
    }
    return false
  }

  override fun toString(): String {
    return String.format("GoGameController(GoGame=%s, black=%s, white=%s, end=%s)",
        game, blackPlayer, whitePlayer, matchEndStatus)
  }

  fun buildGameData(): GameData {
    gameData.sequenceNumber = gameData.sequenceNumber + 1
    return gameData.build()
  }

  fun markingTurnDone() {
    if (isLocalGame || matchEndStatus.lastModifier != currentColor) {
      gameData.phase = FINISHED
      analytics.gameFinished(gameConfiguration, score)
    }
    gameData.turn = opponentColor
  }

  fun canUndo(): Boolean {
    return isLocalGame && gameData.phase == IN_GAME && !gameData.moveList.isEmpty()
  }

  fun canRedo(): Boolean {
    return isLocalGame && gameData.phase == IN_GAME && !gameData.redoList.isEmpty()
  }

  fun resign() {
    gameData.phase = FINISHED
    gameData.matchEndStatusBuilder.scoreBuilder.apply {
      winner = opponentColor
      resigned = true
    }
    analytics.gameFinished(gameConfiguration, score)
  }

  fun playMoveOrToggleDeadStone(move: Move): Boolean {
    when (phase) {
      IN_GAME -> return playMove(move)
      DEAD_STONE_MARKING -> return toggleDeadStone(move.position)
      else -> throw RuntimeException("Invalid mode")
    }
  }

  fun validateConfiguration() {

    if (isConfigurationAgreed()) {
      gameData.phase = IN_GAME
      createGoGame()
    } else {
      gameData.phase = CONFIGURATION
    }
    gameData.turn = computeConfigurationNextTurn()
    analytics.configurationChanged(gameData)
  }

  fun getPlayerForColor(player: Color): GoPlayer {
    return gameDatas.getGoPlayer(gameData, player)
  }

  fun setBlackPlayerName(blackPlayerName: String) {
    gameData.gameConfigurationBuilder.blackBuilder.name = blackPlayerName
  }

  fun setWhitePlayerName(whitePlayerName: String) {
    gameData.gameConfigurationBuilder.whiteBuilder.name = whitePlayerName
  }

  fun setBoardSize(boardSize: Int) {
    gameData.gameConfigurationBuilder.boardSize = boardSize
  }

  fun setKomi(komi: Float) {
    gameData.gameConfigurationBuilder.komi = komi
  }

  fun setHandicap(handicap: Int) {
    gameData.gameConfigurationBuilder.handicap = handicap
  }

  fun swapPlayers() {
    val gameConfiguration = gameConfiguration
    val black = gameConfiguration.black
    val white = gameConfiguration.white
    gameData.gameConfigurationBuilder.black = white
    gameData.gameConfigurationBuilder.white = black
    gameData.turn = opponentColor
  }

  fun pass(): Boolean {
    return playMove(gameDatas.createPassMove())
  }

  private fun createGoGame() {
    game = GoGame(gameConfiguration.boardSize, gameConfiguration.handicap)
  }

  private fun playMove(move: Move): Boolean {
    if (gameData.phase == IN_GAME && game!!.play(getPos(move))) {
      updateRedoForMove(move)
      gameData.addMove(move)
      gameData.turn = opponentColor
      checkForMatchEnd()
      analytics.movePlayed(gameConfiguration, move)
      return true
    } else {
      analytics.invalidMovePlayed(gameConfiguration)
      return false
    }
  }

  private fun removeLastMove(): Move {
    val lastIndex = gameData.moveCount - 1
    val lastMove = gameData.getMove(lastIndex)
    gameData.removeMove(lastIndex)
    return lastMove
  }

  private fun toggleDeadStone(position: Position): Boolean {
    if (game!!.getColor(position.x, position.y) == null) {
      return false
    }
    val index = matchEndStatus.deadStoneList.indexOf(position)
    val matchEndStatus = gameData.matchEndStatusBuilder
    if (index == -1) {
      matchEndStatus.addDeadStone(position)
    } else {
      matchEndStatus.removeDeadStone(index)
    }
    matchEndStatus.lastModifier = gameData.turn
    matchEndStatus.score = calculateScore()
    analytics.deadStoneToggled(gameConfiguration)
    return true
  }

  private fun isConfigurationAgreed(): Boolean =
      initialGameData.gameConfiguration.gameType == LOCAL ||
          (initialGameData.phase == CONFIGURATION && initialGameData.gameConfiguration == gameConfiguration)

  private fun computeConfigurationNextTurn(): PlayGameData.Color = when (phase) {
    CONFIGURATION -> gameDatas.getOpponentColor(gameConfiguration)
    IN_GAME -> gameDatas.computeInGameTurn(gameConfiguration, 0)
    else -> throw IllegalArgumentException("Invalid phase: $phase")
  }

  private fun calculateScore(): Score {
    val scoreGenerator = ScoreGenerator(game!!.board,
        Sets.newHashSet(deadStones), gameConfiguration.komi)
    return scoreGenerator.score
  }

  private fun updateRedoForMove(move: Move) {
    if (gameData.redoCount == 0) {
      return
    }
    if (move == gameData.getRedo(0)) {
      gameData.removeRedo(0)
    } else {
      gameData.clearRedo()
    }
  }

  private fun checkForMatchEnd() {
    if (game!!.isGameEnd) {
      gameData.phase = DEAD_STONE_MARKING
      val lastModifier = GoBoard.getOpponent(game!!.currentColor)
      gameData.matchEndStatusBuilder
          .setLastModifier(lastModifier).score = calculateScore()
    }
  }

  private fun getPos(move: Move): Int {
    when (move.type) {
      PlayGameData.Move.MoveType.MOVE -> {
        val position = move.position
        return game!!.getPos(position.x, position.y)
      }
      PlayGameData.Move.MoveType.PASS -> return game!!.passValue
      else -> throw RuntimeException("Invalid Move")
    }
  }
}
