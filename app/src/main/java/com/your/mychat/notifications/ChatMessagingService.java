package com.your.mychat.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.mediarouter.media.RemotePlaybackClient;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.your.mychat.MainActivity;
import com.your.mychat.R;
import com.your.mychat.chats.MessagesAdapter;
import com.your.mychat.common.Constants;
import com.your.mychat.common.Util;

public class ChatMessagingService extends FirebaseMessagingService {

    private static final String TAG = "ChatMessagingService";
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);

        Util.updateDeviceToken(this, s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = remoteMessage.getData().get(Constants.NOTIFICATION_TITLE);
        String message = remoteMessage.getData().get(Constants.NOTIFICATION_MESSAGE);

        String whoHasSentID = remoteMessage.getData().get(Constants.NOTIFICATION_WHO_SENT);


        Log.d(TAG, "Notification title" + title+"; Notification Data"+message);

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,PendingIntent.FLAG_ONE_SHOT);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            Log.d(TAG, "New notification!");

            NotificationChannel channel = new NotificationChannel(Constants.CHANNEL_ID,
                            Constants.CHANEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(Constants.CHANNEL_DESC);

            notificationManager.createNotificationChannel(channel);
            notificationBuilder = new NotificationCompat.Builder(this, Constants.CHANNEL_ID);

        }
        else{
            Log.d(TAG, "Notification old notification!");
             notificationBuilder = new NotificationCompat.Builder(this);
        }

        notificationBuilder.setSmallIcon(R.drawable.ic_chat);
        notificationBuilder.setContentTitle(title);
        notificationBuilder.setColor(getResources().getColor(R.color.blue_gray));
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(message));
        notificationBuilder.setSound(defaultSoundUri);
        notificationBuilder.setContentIntent(pendingIntent);
        if(message.startsWith("https://firebasestorage.")){

            try {
                NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();
                Glide.with(this)
                        .asBitmap()
                        .load(message)
                        .into(new CustomTarget<Bitmap>(200,100) {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                bigPictureStyle.bigPicture(resource);
                                notificationBuilder.setStyle(bigPictureStyle);
                                notificationManager.notify(999, notificationBuilder.build());
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }
                        });
            }
            catch (Exception e) {
                notificationBuilder.setContentText("New File Received");
                e.printStackTrace();
            }

        }
        else {
            notificationBuilder.setContentText(message);
            notificationManager.notify(999, notificationBuilder.build());
           }

    }
}