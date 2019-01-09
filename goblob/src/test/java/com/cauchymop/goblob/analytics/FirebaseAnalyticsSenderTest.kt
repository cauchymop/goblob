package com.cauchymop.goblob.analytics

import com.cauchymop.goblob.logger.EventLogger
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class FirebaseAnalyticsSenderTest {

  @Mock
  private lateinit var eventLogger: EventLogger

  private lateinit var analyticsSender: FirebaseAnalyticsSender

  @Before
  @Throws(Exception::class)
  fun setUp() {
    analyticsSender = FirebaseAnalyticsSender(eventLogger)
  }

  @After
  @Throws(Exception::class)
  fun tearDown() {
  }

  @Test
  fun undo() {
    analyticsSender.undo()

    verify(eventLogger).logEvent("undo", null)
  }

  @Test
  fun redo() {
    analyticsSender.redo()

    verify(eventLogger).logEvent("redo", null)
  }
}