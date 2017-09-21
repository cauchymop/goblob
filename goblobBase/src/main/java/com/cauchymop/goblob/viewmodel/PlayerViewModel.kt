package com.cauchymop.goblob.viewmodel

import com.cauchymop.goblob.proto.PlayGameData

data class PlayerViewModel(val playerName: String,
                           val playerColor: PlayGameData.Color)
