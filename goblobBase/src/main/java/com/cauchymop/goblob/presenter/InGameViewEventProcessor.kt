package com.cauchymop.goblob.presenter

import com.cauchymop.goblob.model.GameDatas
import com.cauchymop.goblob.view.InGameView
import javax.inject.Inject

class InGameViewEventProcessor @Inject constructor(private val gameDatas: GameDatas,
                                                   private val feedbackSender: FeedbackSender) : GameEventProcessor(), InGameView.InGameEventListener {

    override fun onIntersectionSelected(x: Int, y: Int) {
        goGameController.run {
            if (isLocalTurn) {
                val played = playMoveOrToggleDeadStone(gameDatas.createMove(x, y))

                if (played) {
                    helper.commitGameChanges()
                } else {
                    feedbackSender.invalidMove()
                }
            }
        }
    }

    override fun onPass() {
        goGameController.pass()
        helper.commitGameChanges()
    }

    override fun onDone() {
        goGameController.markingTurnDone()
        helper.commitGameChanges()
    }
}