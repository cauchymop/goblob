package com.cauchymop.goblob.viewmodel

class InGameViewModel(
    val boardViewModel: BoardViewModel,
    val currentPlayerViewModel: PlayerViewModel,
    val message: String,
    val isPassActionAvailable: Boolean,
    val isDoneActionAvailable: Boolean,
    val isResignActionAvailable: Boolean,
    val isUndoActionAvailable: Boolean,
    val isRedoActionAvailable: Boolean)
