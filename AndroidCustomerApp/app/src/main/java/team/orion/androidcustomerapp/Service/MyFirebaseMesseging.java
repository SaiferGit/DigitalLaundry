package team.orion.androidcustomerapp.Service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;
import android.app.Notification;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import team.orion.androidcustomerapp.R;


public class MyFirebaseMesseging extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {

        if(remoteMessage.getNotification().getTitle().equals("Cancel")){
            //cuz this is customer app, we haven't coding for this app yet
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MyFirebaseMesseging.this, ""+remoteMessage.getNotification().getBody(), Toast.LENGTH_LONG).show();
                }
            });

        }

        else if(remoteMessage.getNotification().getTitle().equals("Arrived")) {

            showArrivedNotification(remoteMessage.getNotification().getBody());


        }
    }

    private void showArrivedNotification(String body) {
        //will work for API 25<=
        PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext()
        ,0, new Intent(), PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext());

        builder.setAutoCancel(true)
                .setDefaults(android.app.Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Arrived")
                .setContentText(body)
                .setContentIntent(contentIntent);

        NotificationManager manager = (NotificationManager)getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1,builder.build());
    }
}
