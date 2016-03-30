package com.cauchymop.goblob.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.cauchymop.goblob.R;
import com.cauchymop.goblob.model.GameDatas;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;

import java.text.DateFormat;
import java.util.Date;

/**
 * {@link MatchMenuItem} for a Google Play games turn based match.
 */
public class RemoteMatchMenuItem extends MatchMenuItem {
  private final MainActivity.MatchDescription matchDescription;

  public RemoteMatchMenuItem(MainActivity.MatchDescription matchDescription) {
    super(matchDescription.getMatchId());
    this.matchDescription = matchDescription;
  }

  @Override
  public String getFirstLine(Context context) {
    return context.getString(R.string.match_label_remote_first_line_format,
        matchDescription.getBlackPlayer().getName(),
        matchDescription.getWhitePlayer().getName());
  }

  @Override
  public String getSecondLine(Context context) {
    Date lastUpdate = new Date(matchDescription.getLastUpdateTimestamp());
    DateFormat dateFormat = DateFormat.getDateInstance();
    return context.getString(R.string.match_label_remote_second_line_format,
        matchDescription.getGameData().getGameConfiguration().getBoardSize(), dateFormat.format(lastUpdate));
  }

  @Override
  public Drawable getIcon(Context context) {
    int iconResId =  matchDescription.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN?R.drawable.ic_match_your_turn:R.drawable.ic_match_their_turn;
    return context.getResources().getDrawable(iconResId);
  }

  @Override
  public void start(GameStarter gameStarter) {
    if (needsApplicationUpdate()) {
      gameStarter.showUpdateScreen();
    } else {
      gameStarter.selectGame(getMatchId());
    }
  }

  @Override
  public boolean isValid() {
    return !needsApplicationUpdate();
  }

  private boolean needsApplicationUpdate() {
    return matchDescription.getGameData().getVersion() > GameDatas.VERSION;
  }
}
