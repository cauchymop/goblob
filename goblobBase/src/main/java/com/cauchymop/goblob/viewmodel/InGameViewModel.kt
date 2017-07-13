package com.cauchymop.goblob.viewmodel

class InGameViewModel(
        val boardViewModel: BoardViewModel,
        val currentPlayerViewModel: PlayerViewModel,
        val isPassActionAvailable: Boolean,
        val isDoneActionAvailable: Boolean,
        val message: String,
        val isUndoActionAvailable: Boolean,
        val isRedoActionAvailable: Boolean,
        val isResignActionAvailable: Boolean)
