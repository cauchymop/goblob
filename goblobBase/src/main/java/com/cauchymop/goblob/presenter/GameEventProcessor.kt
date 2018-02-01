package com.cauchymop.goblob.presenter

import com.cauchymop.goblob.model.GoGameController
import kotlin.properties.Delegates

abstract class GameEventProcessor {

    var helper: GamePresenterHelper by Delegates.notNull<GamePresenterHelper>()
    var goGameControllerProvider: () -> GoGameController? by Delegates.notNull<() -> GoGameController?>()

    val goGameController
        get() = goGameControllerProvider.invoke() ?: throw NullPointerException()

}