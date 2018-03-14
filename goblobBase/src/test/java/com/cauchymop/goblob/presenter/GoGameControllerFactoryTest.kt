package com.cauchymop.goblob.presenter

import com.cauchymop.goblob.model.Analytics
import com.cauchymop.goblob.model.GameDatas
import com.cauchymop.goblob.model.GoGameController
import com.cauchymop.goblob.proto.PlayGameData.GameData
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class GoGameControllerFactoryTest {

    @Mock
    private lateinit var analytics: Analytics

    @Test
    fun createGameController() {
        val gameController: GoGameController = GoGameControllerFactory().createGameController(GameDatas(), GameData.getDefaultInstance(), analytics)

        assertNotNull(gameController)
    }

}