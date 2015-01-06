package com.cauchymop.goblob.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.cauchymop.goblob.R;
import com.cauchymop.goblob.model.GoGameController;
import com.cauchymop.goblob.proto.PlayGameData;

/**
 * {@link MatchMenuItem} representing a Local Game.
 */
public class LocalMatchMenuItem extends MatchMenuItem {
  public static final String LOCAL_MATCH_ID = "local";
  private final GoGameController gameController;

  public LocalMatchMenuItem(GoGameController gameController) {
    super(LOCAL_MATCH_ID);
    this.gameController = gameController;
  }

  @Override
  public String getFirstLine(Context context) {
    return context.getString(R.string.local_match_item_label);
  }

  @Override
  public String getSecondLine(Context context) {
    PlayGameData.GameConfiguration conf = gameController.getGameConfiguration();
    return context.getString(R.string.match_label_local_second_line_format, conf.getBoardSize());
  }

  @Override
  public Drawable getIcon(Context context) {
    return null;
  }

  @Override
  public void start(GameStarter gameStarter) {
    gameStarter.startLocalGame(gameController);
  }

  @Override
  public String getMatchId() {
    return LOCAL_MATCH_ID;
  }
}
