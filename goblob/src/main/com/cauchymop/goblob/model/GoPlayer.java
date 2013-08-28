package com.cauchymop.goblob.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A {@link Player} for the Game of Go.
 */
public class GoPlayer extends Player {

  public static final Parcelable.Creator<GoPlayer> CREATOR = new Parcelable.Creator<GoPlayer>() {
    public GoPlayer createFromParcel(Parcel in) {
      return new GoPlayer(in);
    }

    public GoPlayer[] newArray(int size) {
      return new GoPlayer[size];
    }
  };

  private StoneColor stoneColor = StoneColor.Black;

  public GoPlayer(PlayerType type, String name) {
    super(type, name);
  }

  protected GoPlayer(Parcel in) {
    super(in);
    setStoneColor(StoneColor.valueOf(in.readString()));
  }

  public StoneColor getStoneColor() {
    return stoneColor;
  }

  public void setStoneColor(StoneColor stoneColor) {
    this.stoneColor = stoneColor;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    super.writeToParcel(dest, flags);
    dest.writeString(stoneColor.name());
  }
}
