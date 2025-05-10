package com.cauchymop.goblob.ui


import android.content.Context
import android.content.res.Resources
import com.cauchymop.goblob.R
import com.cauchymop.goblob.model.GoGameController
import com.cauchymop.goblob.model.GoogleAccountManager
import com.cauchymop.goblob.presenter.AchievementManager
import com.google.android.gms.games.AchievementsClient
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
internal class AchievementManagerAndroid @Inject constructor(context: Context,
                                                             private val googleAccountManager: GoogleAccountManager,
                                                             private val achievementClientProvider: Provider<AchievementsClient>) : AchievementManager {
  private val resources: Resources = context.resources

  override fun updateAchievements(goGameController: GoGameController) = with(goGameController) {
    if (!isGameFinished) {
      return
    }
    when (boardSize) {
      9 -> unlockAchievement9x9()
      13 -> unlockAchievement13x13()
      19 -> unlockAchievement19x19()
    }
    if (isLocalGame) {
      unlockAchievementLocal()
    } else {
      unlockAchievementRemote()
      if (winner.isLocal) {
        unlockAchievementWinner()
      }
    }
  }

  private fun unlockAchievement9x9() {
    unlockAchievement(resources.getString(R.string.achievements_9x9))
  }

  private fun unlockAchievement13x13() {
    unlockAchievement(resources.getString(R.string.achievements_13x13))
  }

  private fun unlockAchievement19x19() {
    unlockAchievement(resources.getString(R.string.achievements_19x19))
  }

  private fun unlockAchievementLocal() {
    unlockAchievement(resources.getString(R.string.achievements_local))
  }

  private fun unlockAchievementRemote() {
    unlockAchievement(resources.getString(R.string.achievements_remote))
  }

  private fun unlockAchievementWinner() {
    unlockAchievement(resources.getString(R.string.achievements_winner))
  }

  private fun unlockAchievement(achievementId: String) {
    if (googleAccountManager.signInComplete) {
      achievementClientProvider.get().unlock(achievementId)
    }
  }
}
