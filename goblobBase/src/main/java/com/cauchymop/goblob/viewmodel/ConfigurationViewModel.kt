package com.cauchymop.goblob.viewmodel

data class ConfigurationViewModel(
        val komi: Float,
        val boardSize: Int,
        val handicap: Int,
        val blackPlayerName: String,
        val whitePlayerName: String,
        val message: String,
        val interactionsEnabled: Boolean)
