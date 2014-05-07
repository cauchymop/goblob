package com.cauchymop.goblob.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.cauchymop.goblob.R;
import com.cauchymop.goblob.model.GoGameController;
import com.cauchymop.goblob.proto.PlayGameData;

/**
 * MatchMenuItem representing a Local Game.
 */
public class LocalMatchMenuItem implements MatchMenuItem {
  private final GoGameController gameController;

  public LocalMatchMenuItem(GoGameController gameController) {
    this.gameController = gameController;
  }

  @Override
  public String getDisplayName(Context context) {
    PlayGameData.GameConfiguration conf = gameController.getGameConfiguration();
    return context.getString(R.string.match_label_format, context.getString(R.string.human_local_opponent_label), conf.getBoardSize());
  }

  @Override
  public Drawable getIcon(Context context) {
    return null;
  }

  @Override
  public void start(GameStarter gameStarter) {
    gameStarter.startLocalGame(gameController);
  }
}
