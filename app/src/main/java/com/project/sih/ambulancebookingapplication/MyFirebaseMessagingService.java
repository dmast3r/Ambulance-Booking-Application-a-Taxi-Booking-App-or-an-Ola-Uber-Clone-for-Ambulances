package com.project.sih.ambulancebookingapplication;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.core.Context;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import android.os.Handler;

import java.sql.Driver;


/**
 * Created by sourabh on 11/12/18.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {
        Log.d("refreshedToken", "Refreshed token: " + token);
    }

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {

        String []titleText = remoteMessage.getNotification().getTitle().split(",");
        String title = titleText[0], receiverCategory = titleText[1], senderRegToken = titleText[2];
        String sender_latitude = titleText[3], sender_longitude = titleText[4];
        String receiver_latitude = titleText[5], receiver_longitude = titleText[6];
        String senderPhoneNumber = titleText[7], receiverPhoneNumber = titleText[8];
        String body = remoteMessage.getNotification().getBody();

        PendingIntent pendingActivityIntent = null;

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        Notification notification;

        if(receiverCategory.equals("Driver")) {
            Intent activityIntent = new Intent(this, DriverDecision.class);
            activityIntent.setAction("actionString" + System.currentTimeMillis());
            activityIntent.putExtra("Mapping Details",
                    new String[]{receiver_latitude, receiver_longitude, sender_latitude, sender_longitude, body, senderRegToken,
                            receiverPhoneNumber, senderPhoneNumber});
            pendingActivityIntent = PendingIntent.getActivity(this, (int)System.currentTimeMillis(), activityIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            notification = new NotificationCompat.Builder(this, MainActivity.CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_stat_directions_car)
                    .setColor(getResources().getColor(R.color.colorPrimary))
                    .setContentTitle(title)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                    .setContentIntent(pendingActivityIntent)
                    .setLights(Color.RED, 3000, 3000)
                    .setAutoCancel(true)
                    .build();
        }


        else {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:"+senderPhoneNumber));

            PendingIntent pendingIntent = PendingIntent.getActivity(this, (int)System.currentTimeMillis(), intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            notification = new NotificationCompat.Builder(this, MainActivity.CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_stat_directions_car)
                    .setColor(getResources().getColor(R.color.colorPrimary))
                    .setContentTitle(title)
                    .addAction(R.mipmap.ic_launcher, "Call the driver!", pendingIntent)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                    .setLights(Color.RED, 3000, 3000)
                    .setAutoCancel(true)
                    .build();

        }

        notificationManagerCompat.notify(1, notification);
    }
}

/*
        if(remoteMessage != null && remoteMessage.getNotification() != null) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                public void run() {
                    Toast.makeText(getApplicationContext(), remoteMessage.getNotification().getBody() ,Toast.LENGTH_LONG).show();
                }
            });
        }
 */