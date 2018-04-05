package com.cauchymop.goblob.ui


import android.content.Context
import android.content.res.Resources

import com.cauchymop.goblob.R
import com.cauchymop.goblob.model.GoGameController
import com.cauchymop.goblob.presenter.AchievementManager
import com.google.android.gms.common.api.GoogleApiClient

import com.google.android.gms.games.Games.Achievements
import javax.inject.Inject

internal class AchievementManagerAndroid @Inject constructor(context: Context, private val googleApiClient: GoogleApiClient) : AchievementManager {
    private val resources: Resources = context.resources

    override fun updateAchievements(goGameController: GoGameController) = with(goGameController) {
        if (!isGameFinished) {
            return
        }
        when (game!!.boardSize) {
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
        if (isSignedIn) {
            Achievements.unlock(googleApiClient, achievementId)
        }
    }

    private val isSignedIn: Boolean
        get() = googleApiClient.isConnected
}
