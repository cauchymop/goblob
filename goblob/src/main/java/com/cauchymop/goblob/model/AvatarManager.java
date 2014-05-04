package com.cauchymop.goblob.model;

import android.net.Uri;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Class storing avatars Uris for {@link GoPlayer}s.
 */
public class AvatarManager {
  private final Map<String, Uri> avatarUrisByPlayerIds = Maps.newHashMap();

  public Uri getAvatarUri(String playerId) {
    return avatarUrisByPlayerIds.get(playerId);
  }

  public void setAvatarUri(String playerId, Uri avatarUri) {
    if (getAvatarUri(playerId) == null) {
      avatarUrisByPlayerIds.put(playerId, avatarUri);
    }
  }
}
