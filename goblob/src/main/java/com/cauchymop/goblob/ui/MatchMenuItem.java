package com.cauchymop.goblob.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;

/**
 * Created by olivierbonal on 05/05/14.
 */
public interface MatchMenuItem {
  public String getDisplayName(Context context);
  public Drawable getIcon(Context context);
  public void start(GameStarter gameStarter);
}
