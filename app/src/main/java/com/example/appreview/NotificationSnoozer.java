package com.example.appreview;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import static androidx.core.app.NotificationCompat.FLAG_ONGOING_EVENT;
import static com.example.appreview.Defaults.DAILY_NOTIFICATION_ID;
import static com.example.appreview.Defaults.SNOOZED_NOTIFICATION_ID;
import static com.example.appreview.Defaults.convertTimeToLong;

public class NotificationSnoozer extends BroadcastReceiver {

    public static void snoozeNotification(Context context, Intent intent, boolean cancelPrev){
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent newIntent = null;

        newIntent = new Intent(context, SendNotificationReceiver.class);
        newIntent.putExtra("title", intent.getStringExtra("title"));
        newIntent.putExtra("description", intent.getStringExtra("description"));
        newIntent.putExtra("type", intent.getIntExtra("type", -1));


        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, SNOOZED_NOTIFICATION_ID, newIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        SharedPreferences preferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        String snoozeLengthText = preferences.getString("notificationSnoozeLength", Defaults.DEFAULT_SNOOZE_LENGTH);
        long snoozeLength = convertTimeToLong(snoozeLengthText);

        alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + snoozeLength, alarmIntent);

        Toast.makeText(context, "Notification Snoozed", Toast.LENGTH_SHORT).show();

        if(cancelPrev) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            int id = intent.getIntExtra("id", -1);
            assert manager != null;
            manager.cancel(id);
        }
    }

    public static void ScheduleSecondReviewPushNotification(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        if(preferences.getBoolean("appUsageLengthEnabled", true)){
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Intent notification = new Intent(context, SendNotificationReceiver.class);
            notification.putExtra("type", Defaults.APP_USAGE_REMINDER);
            notification.putExtra("title", "Don't forget your second review");
            notification.putExtra("id", 55);

            PendingIntent intent = PendingIntent.getBroadcast(context, Defaults.APP_USAGE_REMINDER, notification, PendingIntent.FLAG_UPDATE_CURRENT);

            String sTimeToWait = preferences.getString("appUsageLength", Defaults.DEFAULT_APP_USAGE_LENGTH);
            long timeToWait = convertTimeToLong(sTimeToWait);

            if (alarmManager != null) {
                alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, timeToWait, intent);
            } else {
                Log.d("Review Activity", "Could not queue app usage reminder");
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        snoozeNotification(context, intent, true);
    }
}
