package com.workverse.app.utils;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.workverse.app.R;
import com.workverse.app.activities.LoginActivity;
public class WorkVerseMessagingService extends FirebaseMessagingService {
    private static final String CHANNEL_ID = "workverse_channel";
    @Override
    public void onMessageReceived(RemoteMessage msg){
        String title = msg.getNotification()!=null ? msg.getNotification().getTitle() : "WorkVerse";
        String body  = msg.getNotification()!=null ? msg.getNotification().getBody()  : "";
        showNotification(title, body);
    }
    private void showNotification(String title, String body){
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel ch = new NotificationChannel(CHANNEL_ID,"WorkVerse",NotificationManager.IMPORTANCE_HIGH);
            nm.createNotificationChannel(ch);
        }
        Intent i = new Intent(this, LoginActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this,0,i,PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder b = new NotificationCompat.Builder(this,CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title).setContentText(body)
            .setAutoCancel(true).setContentIntent(pi);
        nm.notify((int)System.currentTimeMillis(), b.build());
    }
    @Override public void onNewToken(String token){}
}