package com.cauchymop.goblob.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;

/**
 * Created by olivierbonal on 05/05/14.
 */
public class CreateNewGameMenuItem implements MatchMenuItem {
  private final String label;

  public CreateNewGameMenuItem(String label) {
    this.label = label;
  }

  @Override
  public String getDisplayName(Context context) {
    return label;
  }

  @Override
  public Drawable getIcon(Context context) {
    return null;
  }

  @Override
  public void start(GameStarter gameStarter) {
    gameStarter.startNewGame();
  }
}
