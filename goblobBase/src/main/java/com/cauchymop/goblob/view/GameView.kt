package com.cauchymop.goblob.view


import com.cauchymop.goblob.presenter.ConfigurationEventListener
import com.cauchymop.goblob.viewmodel.ConfigurationViewModel
import com.cauchymop.goblob.viewmodel.InGameViewModel

interface GameView {
    fun setConfigurationViewModel(configurationViewModel: ConfigurationViewModel?)
    fun setConfigurationViewListener(configurationEventListener: ConfigurationEventListener?)

    fun setInGameViewModel(inGameViewModel: InGameViewModel)
    fun setInGameActionListener(inGameEventListener: InGameView.InGameEventListener?)
}
