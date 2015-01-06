package com.cauchymop.goblob.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.common.base.Objects;

/**
 * An ActionBar navigation spinner menu entry for a match.
 */
public abstract class MatchMenuItem {
  private final String matchId;

  public abstract String getFirstLine(Context context);
  public abstract String getSecondLine(Context context);
  public abstract Drawable getIcon(Context context);
  public abstract void start(GameStarter gameStarter);

  public MatchMenuItem(String matchId) {
    this.matchId = matchId;
  }

  public String getMatchId() {
    return matchId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof MatchMenuItem)) return false;

    MatchMenuItem that = (MatchMenuItem) o;

    return Objects.equal(this.matchId, that.matchId);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(matchId);
  }
}
