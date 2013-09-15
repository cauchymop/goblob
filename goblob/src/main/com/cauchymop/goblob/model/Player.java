package com.cauchymop.goblob.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.common.images.ImageManager;

/**
 * Interface to represent a player.
 */
public class Player implements Parcelable {

  public static final Parcelable.Creator<Player> CREATOR = new Parcelable.Creator<Player>() {
    public Player createFromParcel(Parcel in) {
      return new Player(in);
    }

    public Player[] newArray(int size) {
      return new Player[size];
    }
  };

  private PlayerType type;
  private String name;
  private Bitmap avatar;
  private Uri avatarUri;

  public Player(PlayerType type, String name) {
    this.type = type;
    this.name = name;
  }

  protected Player(Parcel in) {
    name = in.readString();
    type = PlayerType.valueOf(in.readString());
    avatar = (Bitmap) in.readValue(Bitmap.class.getClassLoader());
    avatarUri = (Uri) in.readValue(Uri.class.getClassLoader());
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(name);
    dest.writeString(type.name());
    dest.writeValue(avatar);
    dest.writeValue(avatarUri);
  }

  public PlayerType getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Bitmap getAvatar() {
    return avatar;
  }

  public void setAvatar(Bitmap avatar) {
    this.avatar = avatar;
  }

  public void setAvatar(Drawable avatar) {
    if (avatar == null) {
      setAvatar((Bitmap)null);
      return;
    }

    final int w = avatar.getIntrinsicWidth();
    final int h = avatar.getIntrinsicHeight();
    Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    avatar.setBounds(0, 0, w, h);
    avatar.draw(canvas);
    setAvatar(bitmap);
  }

  public void setAvatarUri(Context context, Uri avatarUri) {
    this.avatarUri = avatarUri;

    // Fetch Avatar drawable from Uri
    ImageManager.create(context).loadImage(new ImageManager.OnImageLoadedListener() {
      @Override
      public void onImageLoaded(Uri uri, Drawable drawable) {
        setAvatar(drawable);
      }
    }, avatarUri);
  }

  public enum PlayerType {
    AI(false),
    HUMAN_LOCAL(false),
    HUMAN_REMOTE_FRIEND(true),
    HUMAN_REMOTE_RANDOM(true);

    private final boolean remote;

    PlayerType(boolean remote) {
      this.remote = remote;
    }

    public boolean isRemote() {
      return remote;
    }
  }
}
