package com.cauchymop.goblob.logger

interface EventLogger {
  fun logEvent(name:String, params:Map<String, String>? = null)
}
