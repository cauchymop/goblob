package com.cauchymop.goblob.presenter

import com.cauchymop.goblob.model.GameDatas
import com.cauchymop.goblob.viewmodel.ConfigurationViewModels
import com.cauchymop.goblob.viewmodel.InGameViewModels
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class GameViewUpdaterTest {
    private val CONFIGURATION_INITIAL_MESSAGE = "configuration initial message"

    @Mock private lateinit var gameMessageGenerator: GameMessageGenerator

    private val GAME_DATAS = GameDatas()
    private var configurationViewModels: ConfigurationViewModels? = null
    private var inGameViewModels: InGameViewModels? = null

    @Before
    @Throws(Exception::class)
    fun setUp() {
        configurationViewModels = ConfigurationViewModels(gameMessageGenerator)
        inGameViewModels = InGameViewModels(GAME_DATAS, gameMessageGenerator)

//        Mockito.`when`(gameMessageGenerator.configurationMessageInitial).thenReturn(CONFIGURATION_INITIAL_MESSAGE)
    }

    // TODO: All Test cases
    @Test
    fun update() {
    }

}