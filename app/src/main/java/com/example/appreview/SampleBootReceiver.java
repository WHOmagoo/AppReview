package com.example.appreview;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Calendar;

import static com.example.appreview.MainActivity.CHANNEL_ID;
import static com.example.appreview.MainActivity.NOTIFICATION_ID;

public class SampleBootReceiver extends BroadcastReceiver {

    private void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Daily Reminder";
            String description = "This channel will go off once daily";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void sendPushNotification(Context context){
        createNotificationChannel(context);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        builder.setContentTitle("Don't forget to review the app today!");
        builder.setSmallIcon(R.drawable.review_icon_foreground);
        builder.setPriority(Notification.PRIORITY_DEFAULT);
        Intent notifyIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 2, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //to be able to launch your activity from the notification
        builder.setContentIntent(pendingIntent);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
        managerCompat.notify(NOTIFICATION_ID, builder.build());
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("Boot Recieve", "Recieved command from alarm");
        sendPushNotification(context);
//        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
//            AlarmManager alarmMgr;
//            PendingIntent alarmIntent;
//            alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
//            intent = new Intent(context, MyReceiver.class);
//            alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
//
//
//            // Set the alarm to start at approximately 8:00 p.m.
//            Calendar calendar = Calendar.getInstance();
//            calendar.setTimeInMillis(System.currentTimeMillis());
//            calendar.set(Calendar.HOUR_OF_DAY, 20);
//
//            // With setInexactRepeating(), you have to use one of the AlarmManager interval
//            // constants--in this case, AlarmManager.INTERVAL_DAY.
//            alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
//                    AlarmManager.INTERVAL_DAY, alarmIntent);
//
//        }
    }
}
