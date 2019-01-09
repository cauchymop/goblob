package com.cauchymop.goblob.logger

import android.os.Bundle

interface EventLogger {
  fun logEvent(name:String, params:Bundle?)
}
