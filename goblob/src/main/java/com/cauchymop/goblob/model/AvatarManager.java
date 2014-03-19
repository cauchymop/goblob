package com.cauchymop.goblob.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.google.android.gms.common.images.ImageManager;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Class storing avatars for {@link Player}s.
 */
public class AvatarManager {
  private Map<Player, Bitmap> avatars = Maps.newHashMap();

  public Bitmap getAvatar(Player player) {
    return avatars.get(player);
  }

  public void setAvatar(Player player, Bitmap avatar) {
    avatars.put(player, avatar);
  }

  public void setAvatar(Player player, Drawable avatar) {
    if (avatar == null) {
      setAvatar(player, (Bitmap)null);
      return;
    }

    final int w = avatar.getIntrinsicWidth();
    final int h = avatar.getIntrinsicHeight();
    Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    avatar.setBounds(0, 0, w, h);
    avatar.draw(canvas);
    setAvatar(player, bitmap);
  }

  public void setAvatarUri(Context context, Player player, Uri avatarUri) {
    fetchAvatarFromUri(context, player, avatarUri);
  }

  private void fetchAvatarFromUri(Context context, final Player player, Uri avatarUri) {
    ImageManager.create(context).loadImage(new ImageManager.OnImageLoadedListener() {

      @Override
      public void onImageLoaded(Uri uri, Drawable drawable, boolean isRequestedDrawable) {
        setAvatar(player, drawable);
      }
    }, avatarUri);
  }
}
