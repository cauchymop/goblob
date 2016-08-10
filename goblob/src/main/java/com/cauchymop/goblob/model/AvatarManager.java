package com.cauchymop.goblob.model;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.common.images.ImageManager;
import com.google.common.collect.Maps;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Utility class storing avatars Uris for players and using them to load Avatar images in
 * {@link ImageView}s on demand.
 */
@Singleton
public class AvatarManager {
  private static final String TAG = "AvatarManager";

  private final Map<String, Uri> avatarUrisByPlayerDisplayName = Maps.newHashMap();
  final ImageManager imageManager;

  @Inject
  public AvatarManager(Context context) {
    imageManager = ImageManager.create(context);
  }

  public void setAvatarUri(String playerDisplayName, Uri avatarUri) {
    Log.d(TAG, String.format("setAvatarUri(%s, %s)", playerDisplayName, avatarUri));
    if (getAvatarUri(playerDisplayName) == null) {
      Log.d(TAG, String.format("    ==> setting new Avatar for %s", playerDisplayName));
      avatarUrisByPlayerDisplayName.put(playerDisplayName, avatarUri);
    }
  }

  public void loadImage(ImageView avatarImage, String playerDisplayName) {
//    Log.d(TAG, String.format("loadImage(%s)", playerDisplayName));
    imageManager.loadImage(avatarImage, getAvatarUri(playerDisplayName));
  }

  private Uri getAvatarUri(String playerDisplayName) {
//    Log.d(TAG, String.format("getAvatarUri(%s)", playerDisplayName));
    return avatarUrisByPlayerDisplayName.get(playerDisplayName);
  }
}
