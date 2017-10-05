package com.cauchymop.goblob.ui

import android.content.Context
import android.media.RingtoneManager
import com.cauchymop.goblob.presenter.FeedbackSender
import javax.inject.Inject

class AndroidFeedbackSender @Inject constructor(val context: Context) :FeedbackSender {
    override fun invalidMove() {
        try {
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(context, notification)
            r.play()
        } catch (e: Exception) {
            System.err.println("Exception while buzzing")
            e.printStackTrace()
        }
    }
}