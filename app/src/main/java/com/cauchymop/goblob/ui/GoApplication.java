package com.cauchymop.goblob.ui;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.cauchymop.goblob.R;
import com.cauchymop.goblob.injection.DaggerGoApplicationComponent;
import com.cauchymop.goblob.injection.GoApplicationComponent;
import com.cauchymop.goblob.injection.GoApplicationModule;

/**
 * Top level application for Game of Go.
 */
public class GoApplication extends Application {

  public static final String CHANNEL_ID = "GO_BLOB_NOTIFICATIONS";
  private GoApplicationComponent component;

  @Override
  public void onCreate() {
    super.onCreate();
    component = DaggerGoApplicationComponent.builder()
        .goApplicationModule(new GoApplicationModule(this))
        .build();
    createNotificationChannel();
  }

  private void createNotificationChannel() {
    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is not in the Support Library.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      CharSequence name = getString(R.string.notification_channel_name);
      String description = getString(R.string.notification_channel_description);
      int importance = NotificationManager.IMPORTANCE_DEFAULT;
      NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
      channel.setDescription(description);
      // Register the channel with the system; you can't change the importance
      // or other notification behaviors after this.
      NotificationManager notificationManager = getSystemService(NotificationManager.class);
      notificationManager.createNotificationChannel(channel);
    }
  }

  public GoApplicationComponent getComponent() {
    return component;
  }

}
