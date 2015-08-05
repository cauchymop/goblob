package com.cauchymop.goblob.model;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.common.images.ImageManager;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Utility class storing avatars Uris for players and using them to load Avatar images in
 * {@link ImageView}s on demand.
 */
public class AvatarManager {
  private static final String TAG = "AvatarManager";

  private final Map<String, Uri> avatarUrisByPlayerIds = Maps.newHashMap();
  final ImageManager imageManager;

  public AvatarManager(Context context) {
    imageManager = ImageManager.create(context);
  }

  public void setAvatarUri(String playerId, Uri avatarUri) {
    Log.d(TAG, String.format("setAvatarUri(%s, %s)", playerId, avatarUri));
    if (getAvatarUri(playerId) == null) {
      avatarUrisByPlayerIds.put(playerId, avatarUri);
    }
  }

  public void loadImage(ImageView avatarImage, String playerId) {
    Log.d(TAG, String.format("loadImage(%s)", playerId));
    imageManager.loadImage(avatarImage, getAvatarUri(playerId));
  }

  private  Uri getAvatarUri(String playerId) {
    Log.d(TAG, String.format("getAvatarUri(%s)", playerId));
    Log.d(TAG, String.format("avatarUrisByPlayerIds: %s", avatarUrisByPlayerIds));
    return avatarUrisByPlayerIds.get(playerId);
  }
}
