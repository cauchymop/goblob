package com.cauchymop.goblob.analytics

import android.os.Bundle
import com.cauchymop.goblob.logger.EventLogger
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject

class AnalyticsEventLogger @Inject constructor(private val firebaseAnalytics: FirebaseAnalytics) : EventLogger {
  override fun logEvent(name: String, params: Bundle?) {
    firebaseAnalytics.logEvent(name, params)
  }
}