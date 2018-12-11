package com.cauchymop.goblob.analytics

import android.os.Bundle
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
      val bundle = getGameConfigurationBundle(game.gameConfiguration)
      bundle.putBoolean("agreed", game.phase == Phase.IN_GAME)
      eventLogger.logEvent("configurationChanged", bundle)
    }
  }

  override fun undo() {
    eventLogger.logEvent("undo", Bundle.EMPTY)
  }

  override fun redo() {
    eventLogger.logEvent("redo", Bundle.EMPTY)
  }

  override fun resign() {
    eventLogger.logEvent("resign", Bundle.EMPTY)
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
    val gameConfigurationBundle = getGameConfigurationBundle(gameConfiguration)
    gameConfigurationBundle.putBoolean("resigned", score.resigned)
    gameConfigurationBundle.putFloat("wonBy", score.wonBy)
    gameConfigurationBundle.putString("winner", score.winner.toString())
    eventLogger.logEvent("gameFinished", gameConfigurationBundle)
  }

  @NonNull
  private fun getGameConfigurationBundle(gameConfiguration: GameConfiguration): Bundle {
    val bundle = Bundle()
    bundle.putString("type", gameConfiguration.gameType.name)
    bundle.putInt("size", gameConfiguration.boardSize)
    bundle.putInt("handicap", gameConfiguration.handicap)
    return bundle
  }
}
