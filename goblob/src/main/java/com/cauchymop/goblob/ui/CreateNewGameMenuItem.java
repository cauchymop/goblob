package com.cauchymop.goblob.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;

/**
 * {@link MatchMenuItem} which creates a new game.
 */
public class CreateNewGameMenuItem implements MatchMenuItem {

  public static final String NEW_GAME_MATCH_ID = "new game";

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

  @Override
  public String getMatchId() {
    return NEW_GAME_MATCH_ID;
  }
}
