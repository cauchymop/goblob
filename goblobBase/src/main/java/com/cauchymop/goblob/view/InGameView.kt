package com.cauchymop.goblob.view


import com.cauchymop.goblob.viewmodel.InGameViewModel

interface InGameView {
    fun setInGameModel(inGameViewModel: InGameViewModel)
    fun setInGameEventListener(inGameEventListener: InGameEventListener?)

    interface InGameEventListener : GoBoardView.BoardEventListener {
        fun onPass()
        fun onDone()
        fun onUndo()
        fun onRedo()
        fun onResign()
    }
}
