package com.cauchymop.goblob.service

import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.cauchymop.goblob.R
import com.cauchymop.goblob.model.GameRepository
import com.cauchymop.goblob.ui.GoApplication
import com.cauchymop.goblob.ui.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import javax.inject.Inject

class GoFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var gameRepository: GameRepository

    override fun onCreate() {
        super.onCreate()
        (application as GoApplication).component.inject(this)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        gameRepository.getLobbyClient().setPushToken(token, "FCM")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title ?: getString(R.string.app_name)
        val message = remoteMessage.notification?.body ?: return

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, GoApplication.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }
}
