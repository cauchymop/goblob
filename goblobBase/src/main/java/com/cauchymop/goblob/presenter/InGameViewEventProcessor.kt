package com.cauchymop.goblob.presenter

import com.cauchymop.goblob.model.GameDatas
import com.cauchymop.goblob.model.GoGameController
import com.cauchymop.goblob.view.InGameView
import javax.inject.Inject

class InGameViewEventProcessor @Inject constructor(private val gameDatas: GameDatas,
                                                   private val feedbackSender: FeedbackSender) : InGameView.InGameEventListener {

    lateinit var helper: GamePresenterHelper
    lateinit var goGameControllerProvider: () -> GoGameController?

    override fun onIntersectionSelected(x: Int, y: Int) {
        goGameControllerProvider.invoke()!!.run {
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
        goGameControllerProvider.invoke()!!.pass()
        helper.commitGameChanges()
    }

    override fun onDone() {
        goGameControllerProvider.invoke()!!.markingTurnDone()
        helper.commitGameChanges()
    }
}