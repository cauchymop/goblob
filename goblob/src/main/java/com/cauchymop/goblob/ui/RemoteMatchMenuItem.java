package com.cauchymop.goblob.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.cauchymop.goblob.R;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by olivierbonal on 05/05/14.
 */
public class RemoteMatchMenuItem implements MatchMenuItem {
  private final TurnBasedMatch match;

  public RemoteMatchMenuItem(TurnBasedMatch match) {
    this.match = match;
  }

  @Override
  public String getDisplayName(Context context) {
    Date date = new Date(match.getCreationTimestamp());
    DateFormat dateFormat = DateFormat.getDateInstance();

    return context.getString(R.string.match_label_format, dateFormat.format(date), match.getVariant());
//    StringBuilder builder = new StringBuilder();
//    String opponentId = getOpponentId(turnBasedMatch, myId);
//    Participant participant = turnBasedMatch.getParticipant(opponentId);
//    builder.append(participant.getDisplayName());
//
//    return builder.toString();
  }

  @Override
  public Drawable getIcon(Context context) {
    int iconResId =  match.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN?R.drawable.ic_match_your_turn:R.drawable.ic_match_their_turn;
    return context.getResources().getDrawable(iconResId);
  }

  @Override
  public void start(GameStarter gameStarter) {
    gameStarter.startRemoteGame(match);
  }

}
