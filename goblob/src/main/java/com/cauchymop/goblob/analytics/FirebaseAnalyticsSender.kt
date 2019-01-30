package com.cauchymop.goblob.analytics

import androidx.annotation.NonNull
import com.cauchymop.goblob.logger.EventLogger
import com.cauchymop.goblob.model.Analytics
import com.cauchymop.goblob.proto.PlayGameData
import com.cauchymop.goblob.proto.PlayGameData.*
import com.cauchymop.goblob.proto.PlayGameData.GameData.Phase
import javax.inject.Inject

class FirebaseAnalyticsSender @Inject
constructor(private val eventLogger: EventLogger) : Analytics {

  override fun gameCreated(game: GameData) {
    val bundle = getGameConfigurationBundle(game.gameConfiguration)
    eventLogger.logEvent("gameCreated", bundle)
  }

  override fun configurationChanged(game: PlayGameData.GameData) {
    val gameType = game.gameConfiguration.gameType
    if (gameType == PlayGameData.GameType.REMOTE) {
      eventLogger.logEvent("configurationChanged", getGameConfigurationBundle(game.gameConfiguration).apply {
        put("agreed", "${game.phase == Phase.IN_GAME}")
      })
    }
  }

  override fun undo() {
    eventLogger.logEvent("undo")
  }

  override fun redo() {
    eventLogger.logEvent("redo")
  }

  override fun resign() {
    eventLogger.logEvent("resign")
  }

  override fun movePlayed(gameConfiguration: GameConfiguration, move: PlayGameData.Move) {
    if (move.type == PlayGameData.Move.MoveType.PASS) {
      eventLogger.logEvent("passed", getGameConfigurationBundle(gameConfiguration))
    } else {
      eventLogger.logEvent("movePlayed", getGameConfigurationBundle(gameConfiguration))
    }
  }

  override fun deadStoneToggled(gameConfiguration: GameConfiguration) {
    eventLogger.logEvent("deadStoneToggled", getGameConfigurationBundle(gameConfiguration))
  }

  override fun invalidMovePlayed(gameConfiguration: GameConfiguration) {
    eventLogger.logEvent("invalidMovePlayed", getGameConfigurationBundle(gameConfiguration))
  }

  override fun gameFinished(gameConfiguration: GameConfiguration, score: Score) {
    eventLogger.logEvent("gameFinished", getGameConfigurationBundle(gameConfiguration).apply {
      put("resigned", "${score.resigned}")
      put("wonBy", "${score.wonBy}")
      put("winner", "${score.winner}")
    })
  }

  @NonNull
  private fun getGameConfigurationBundle(gameConfiguration: GameConfiguration): MutableMap<String, String> =
      mutableMapOf("type" to gameConfiguration.gameType.name,
          "size" to gameConfiguration.boardSize.toString(),
          "handicap" to gameConfiguration.handicap.toString())

}
