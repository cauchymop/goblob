package com.cauchymop.goblob.service

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.cauchymop.goblob.R
import com.cauchymop.goblob.model.GameRepository
import com.cauchymop.goblob.ui.GoApplication
import com.cauchymop.goblob.ui.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import net.yura.lobby.client.PushLobbyClient
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
        gameRepository.getLobbyClient().sendPushToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = getString(R.string.app_name)
        val message = remoteMessage.data[PushLobbyClient.MESSAGE] ?: ""
        val gameId = remoteMessage.data[PushLobbyClient.GAME_ID] ?: ""
        val options = remoteMessage.data[PushLobbyClient.OPTIONS] ?: ""
        Log.d("Push Received", "onMessageReceived: $title $message $gameId $options")

        // TODO: Use the payload to pass to the pendingIntent and open the correct game when
        //  handling it
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
            if (ActivityCompat.checkSelfPermission(this@GoFirebaseMessagingService, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }
}
