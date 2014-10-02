package com.cauchymop.goblob.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;

/**
 * An ActionBar navigation spinner menu entry for a match.
 */
public interface MatchMenuItem {
  public String getDisplayName(Context context);
  public Drawable getIcon(Context context);
  public void start(GameStarter gameStarter);
  public String getMatchId();
}
