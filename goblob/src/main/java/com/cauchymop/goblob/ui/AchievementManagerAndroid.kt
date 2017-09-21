package com.cauchymop.goblob.ui


import android.content.Context
import android.content.res.Resources

import com.cauchymop.goblob.R
import com.cauchymop.goblob.presenter.AchievementManager
import com.google.android.gms.common.api.GoogleApiClient

import com.google.android.gms.games.Games.Achievements
import javax.inject.Inject

internal class AchievementManagerAndroid @Inject constructor(context: Context, private val googleApiClient: GoogleApiClient) : AchievementManager {
    private val resources: Resources = context.resources

    override fun unlockAchievement9x9() {
        unlockAchievement(resources.getString(R.string.achievements_9x9))
    }

    override fun unlockAchievement13x13() {
        unlockAchievement(resources.getString(R.string.achievements_13x13))
    }

    override fun unlockAchievement19x19() {
        unlockAchievement(resources.getString(R.string.achievements_19x19))
    }

    override fun unlockAchievementLocal() {
        unlockAchievement(resources.getString(R.string.achievements_local))
    }

    override fun unlockAchievementRemote() {
        unlockAchievement(resources.getString(R.string.achievements_remote))
    }

    override fun unlockAchievementWinner() {
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
