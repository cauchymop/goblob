package com.cauchymop.goblob.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.cauchymop.goblob.R;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;

import java.text.DateFormat;
import java.util.Date;

/**
 * {@link MatchMenuItem} for a Google Play games turn based match.
 */
public class RemoteMatchMenuItem implements MatchMenuItem {
  private final String matchId;
  private final int variant;
  private final int turnStatus;
  private final long creationTimestamp;

  public RemoteMatchMenuItem(long creationTimestamp, int variant, int turnStatus, String matchId) {
    this.creationTimestamp = creationTimestamp;
    this.variant = variant;
    this.turnStatus = turnStatus;
    this.matchId = matchId;
  }

  @Override
  public String getDisplayName(Context context) {
    Date date = new Date(creationTimestamp);
    DateFormat dateFormat = DateFormat.getDateInstance();

    return context.getString(R.string.match_label_format, dateFormat.format(date), variant);
//    StringBuilder builder = new StringBuilder();
//    String opponentId = getOpponentId(turnBasedMatch, myId);
//    Participant participant = turnBasedMatch.getParticipant(opponentId);
//    builder.append(participant.getDisplayName());
//
//    return builder.toString();
  }

  @Override
  public Drawable getIcon(Context context) {
    int iconResId =  turnStatus == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN?R.drawable.ic_match_your_turn:R.drawable.ic_match_their_turn;
    return context.getResources().getDrawable(iconResId);
  }

  @Override
  public void start(GameStarter gameStarter) {
    gameStarter.startRemoteGame(matchId);
  }

  @Override
  public String getMatchId() {
    return matchId;
  }
}
