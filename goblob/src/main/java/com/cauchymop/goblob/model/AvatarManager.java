package com.cauchymop.goblob.model;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import com.google.android.gms.common.images.ImageManager;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Utility class storing avatars Uris for {@link GoPlayer}s and using them to load Avatar images in
 * {@link ImageView}s on demand.
 */
public class AvatarManager {
  private final Map<String, Uri> avatarUrisByPlayerIds = Maps.newHashMap();
  final ImageManager imageManager;

  public AvatarManager(Context context) {
    imageManager = ImageManager.create(context);
  }

  public void setAvatarUri(String playerId, Uri avatarUri) {
    if (getAvatarUri(playerId) == null) {
      avatarUrisByPlayerIds.put(playerId, avatarUri);
    }
  }

  public void loadImage(ImageView avatarImage, String playerId) {
    imageManager.loadImage(avatarImage, getAvatarUri(playerId));
  }

  private  Uri getAvatarUri(String playerId) {
    return avatarUrisByPlayerIds.get(playerId);
  }

}
