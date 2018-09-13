package com.cauchymop.goblob.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;

import static com.cauchymop.goblob.model.GameRepositoryKt.NO_MATCH_ID;

/**
 * {@link MatchMenuItem} which creates a new game.
 */
public class CreateNewGameMenuItem extends MatchMenuItem {

  private final String label;

  public CreateNewGameMenuItem(String label) {
    super(NO_MATCH_ID);
    this.label = label;
  }

  @Override
  public String getFirstLine(Context context) {
    return label;
  }

  @Override
  public String getSecondLine(Context context) {
    return null;
  }

  @Override
  public Drawable getIcon(Context context) {
    return null;
  }

}
