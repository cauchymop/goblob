package com.cauchymop.goblob;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Interface to represent a player.
 */
public class Player implements Parcelable {

  private PlayerType type;
  private String name;

  public Player(PlayerType type, String name) {
    this.type = type;
    this.name = name;
  }

  private Player(Parcel in) {
    name = in.readString();
    type = PlayerType.valueOf(in.readString());
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(name);
    dest.writeString(type.name());
  }

  public static final Parcelable.Creator<Player> CREATOR = new Parcelable.Creator<Player>() {
    public Player createFromParcel(Parcel in) {
      return new Player(in);
    }

    public Player[] newArray(int size) {
      return new Player[size];
    }
  };

  public String getName() {
    return name;
  }

  public PlayerType getType() {
    return type;
  }

  public void setName(String name) {
    this.name = name;
  }

  public enum PlayerType {
    AI,
    HUMAN
  }
}
