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
import javax.inject.Singleton
import kotlin.properties.Delegates

/**
 * Class to handle interactions between the players and the [GoGame].
 */
@Singleton
class GoGameController @Inject constructor(
    @field:Transient private var gameDatas: GameDatas,
    private val analytics: Analytics) : Serializable {

  var game: GoGame? = null
    private set

  private var gameDataBuilder: GameData.Builder by Delegates.notNull()

  private var initialGameData: GameData by Delegates.notNull()

  var gameData: GameData
    get() = gameDataBuilder.apply { sequenceNumber++ }.build()
    set(value) {
      this.initialGameData = value
      this.gameDataBuilder = Preconditions.checkNotNull(value).toBuilder()
      // The GoGame settings can change during the configuration, so we postpone its creation.
      if (value.phase != CONFIGURATION) {
        createGoGame()
        for (move in this.gameDataBuilder.moveList) {
          game!!.play(getPos(move))
        }
      }
    }

  val score: Score
    get() = matchEndStatus.score

  val currentPlayer: GoPlayer
    get() = gameDatas.getCurrentPlayer(gameDataBuilder)

  val opponent: GoPlayer
    get() = gameDatas.getGoPlayer(gameDataBuilder, opponentColor)

  val currentColor: Color
    get() = gameDatas.getCurrentColor(gameDataBuilder)

  val gameConfiguration: GameConfiguration
    get() = gameDataBuilder.gameConfiguration

  val isLocalTurn: Boolean
    get() = gameDatas.isLocalTurn(gameDataBuilder)

  val deadStones: List<PlayGameData.Position>
    get() = matchEndStatus.deadStoneList

  val isLocalGame: Boolean
    get() = gameDatas.isLocalGame(gameDataBuilder)

  private val opponentColor: Color
    get() = GoBoard.getOpponent(currentColor)

  private val matchEndStatus: MatchEndStatus
    get() = gameDataBuilder.matchEndStatus

  private val whitePlayer: GoPlayer
    get() = gameConfiguration.white

  private val blackPlayer: GoPlayer
    get() = gameConfiguration.black

  val isGameFinished: Boolean
    get() = gameDataBuilder.phase == FINISHED

  val phase: Phase
    get() = gameDataBuilder.phase

  val winner: GoPlayer
    get() = gameDatas.getGoPlayer(gameDataBuilder, gameDataBuilder.matchEndStatus.score.winner)

  val matchId: String
    get() = gameDataBuilder.matchId

  fun undo(): Boolean {
    if (canUndo()) {
      gameDataBuilder.addRedo(0, removeLastMove())
      game!!.undo()
      return true
    }
    return false
  }

  fun redo(): Boolean {
    if (canRedo()) {
      playMove(gameDataBuilder.getRedo(0))
      return true
    }
    return false
  }

  override fun toString(): String {
    return String.format("GoGameController(GoGame=%s, black=%s, white=%s, end=%s)",
        game, blackPlayer, whitePlayer, matchEndStatus)
  }

  fun markingTurnDone() {
    if (isLocalGame || matchEndStatus.lastModifier != currentColor) {
      gameDataBuilder.phase = FINISHED
      analytics.gameFinished(gameConfiguration, score)
    }
    gameDataBuilder.turn = opponentColor
  }

  fun canUndo(): Boolean {
    return isLocalGame && gameDataBuilder.phase == IN_GAME && !gameDataBuilder.moveList.isEmpty()
  }

  fun canRedo(): Boolean {
    return isLocalGame && gameDataBuilder.phase == IN_GAME && !gameDataBuilder.redoList.isEmpty()
  }

  fun resign() {
    gameDataBuilder.phase = FINISHED
    gameDataBuilder.matchEndStatusBuilder.scoreBuilder.apply {
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
      gameDataBuilder.phase = IN_GAME
      createGoGame()
    } else {
      gameDataBuilder.phase = CONFIGURATION
    }
    gameDataBuilder.turn = computeConfigurationNextTurn()
    analytics.configurationChanged(gameDataBuilder)
  }

  fun getPlayerForColor(player: Color): GoPlayer {
    return gameDatas.getGoPlayer(gameDataBuilder, player)
  }

  fun setBlackPlayerName(blackPlayerName: String) {
    gameDataBuilder.gameConfigurationBuilder.blackBuilder.name = blackPlayerName
  }

  fun setWhitePlayerName(whitePlayerName: String) {
    gameDataBuilder.gameConfigurationBuilder.whiteBuilder.name = whitePlayerName
  }

  fun setBoardSize(boardSize: Int) {
    gameDataBuilder.gameConfigurationBuilder.boardSize = boardSize
  }

  fun setKomi(komi: Float) {
    gameDataBuilder.gameConfigurationBuilder.komi = komi
  }

  fun setHandicap(handicap: Int) {
    gameDataBuilder.gameConfigurationBuilder.handicap = handicap
  }

  fun swapPlayers() {
    val gameConfiguration = gameConfiguration
    val black = gameConfiguration.black
    val white = gameConfiguration.white
    gameDataBuilder.gameConfigurationBuilder.black = white
    gameDataBuilder.gameConfigurationBuilder.white = black
    gameDataBuilder.turn = opponentColor
  }

  fun pass(): Boolean {
    return playMove(gameDatas.createPassMove())
  }

  private fun createGoGame() {
    game = GoGame(gameConfiguration.boardSize, gameConfiguration.handicap)
  }

  private fun playMove(move: Move): Boolean {
    if (gameDataBuilder.phase == IN_GAME && game!!.play(getPos(move))) {
      updateRedoForMove(move)
      gameDataBuilder.addMove(move)
      gameDataBuilder.turn = opponentColor
      checkForMatchEnd()
      analytics.movePlayed(gameConfiguration, move)
      return true
    } else {
      analytics.invalidMovePlayed(gameConfiguration)
      return false
    }
  }

  private fun removeLastMove(): Move {
    val lastIndex = gameDataBuilder.moveCount - 1
    val lastMove = gameDataBuilder.getMove(lastIndex)
    gameDataBuilder.removeMove(lastIndex)
    return lastMove
  }

  private fun toggleDeadStone(position: Position): Boolean {
    if (game!!.getColor(position.x, position.y) == null) {
      return false
    }
    val index = matchEndStatus.deadStoneList.indexOf(position)
    val matchEndStatus = gameDataBuilder.matchEndStatusBuilder
    if (index == -1) {
      matchEndStatus.addDeadStone(position)
    } else {
      matchEndStatus.removeDeadStone(index)
    }
    matchEndStatus.lastModifier = gameDataBuilder.turn
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
    if (gameDataBuilder.redoCount == 0) {
      return
    }
    if (move == gameDataBuilder.getRedo(0)) {
      gameDataBuilder.removeRedo(0)
    } else {
      gameDataBuilder.clearRedo()
    }
  }

  private fun checkForMatchEnd() {
    if (game!!.isGameEnd) {
      gameDataBuilder.phase = DEAD_STONE_MARKING
      val lastModifier = GoBoard.getOpponent(game!!.currentColor)
      gameDataBuilder.matchEndStatusBuilder
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
