package com.cauchymop.goblob.ui

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.cauchymop.goblob.R
import com.cauchymop.goblob.lobby.LobbyClient
import net.yura.lobby.model.Game

/**
 * [MatchMenuItem] for a Lobby turn based match.
 */
class LobbyGameMatchMenuItem(private val lobbyClient: LobbyClient, private val game: Game) :
    MatchMenuItem(game.id.toString()) {
    override fun getFirstLine(context: Context): String? {
        return game.name
    }

    override fun getSecondLine(context: Context): String {
        val gameStatus =
            if (game.inGame != 0) {
                context.getString(R.string.waiting_for_opponent)
            } else {
                context.getString(R.string.playing)
            }
        val players: List<String> = game.players.map { "$it" }
        if (players.size == 2) {
            return context.getString(
                R.string.match_label_remote_first_line_format, players[0], players[1]
            )
        } else {
            return gameStatus
        }
    }


    override fun getIcon(context: Context): Drawable? {
        val whosTurn = game.whosTurn
        val isMyTurn = (whosTurn == lobbyClient.myPlayerName())
        println("whosTurn = $whosTurn, myPlayerName = ${lobbyClient.myPlayerName()},isMyTurn = $isMyTurn")
        val iconResId =
            if (isMyTurn) R.drawable.ic_match_your_turn else R.drawable.ic_match_their_turn
        return ContextCompat.getDrawable(context, iconResId)
    }

    override fun isValid(): Boolean {
        return true
    }
}
