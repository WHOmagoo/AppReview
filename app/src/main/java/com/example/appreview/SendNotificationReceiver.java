package com.example.appreview;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import static com.example.appreview.MainActivity.CHANNEL_ID;
import static com.example.appreview.MainActivity.NOTIFICATION_ID;

public class SendNotificationReceiver extends BroadcastReceiver {

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
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void sendPushNotification(Context context, Intent intent){
        sendPushNotification(context, intent.getStringExtra("title"), intent.getStringExtra("description"), intent.getIntExtra("id", 0), intent.getIntExtra("type", -1));
    }

    public void sendPushNotification(Context context, String title, String description, int id, int type){
        createNotificationChannel(context);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        builder.setContentTitle(title);
        builder.setContentText(description);
        builder.setSmallIcon(R.drawable.review_icon_foreground);
        builder.setPriority(Notification.PRIORITY_DEFAULT);
        Intent notifyIntent = new Intent(context, ReviewActivity.class);
        notifyIntent.putExtra("id", id);

        SharedPreferences settings = context.getSharedPreferences("settings", Context.MODE_PRIVATE);

        Intent snoozeIntent = new Intent(context, NotificationSnoozer.class);
        snoozeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        snoozeIntent.putExtra("title", title);
        snoozeIntent.putExtra("description", description);
        snoozeIntent.putExtra("id", id);
        snoozeIntent.putExtra("type", type);
        PendingIntent snoozeAlarmIntent = PendingIntent.getBroadcast(context,Defaults.SNOOZED_NOTIFICATION_ID, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if(settings.getBoolean("notificationSnoozeEnabled", true)) {
            builder.addAction(R.drawable.snooze_button_foreground, context.getString(R.string.snooze), snoozeAlarmIntent);
        }

        if(settings.getBoolean("recurringNotificationsEnabled", true)){
            NotificationSnoozer.snoozeNotification(context, snoozeIntent, false);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(context, Defaults.CLICKED_NOTIFICATION_ID, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //to be able to launch your activity from the notification
        builder.setContentIntent(pendingIntent);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
        managerCompat.notify(id, builder.build());
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences data = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        UserData.getInstance().updateDay();
        switch (intent.getIntExtra("type", -1)){
            case Defaults.DAILY_NOTIFICATION_ID:
                if(!UserData.dayValid(context) || !data.getBoolean("ReviewedToday", false)){
                    sendPushNotification(context, intent);
                }
                break;
            case Defaults.APP_USAGE_REMINDER:
                if(UserData.dayValid(context) && data.getBoolean("ReviewedToday", false) && !data.getBoolean("DayFinished", false)){
                    sendPushNotification(context, intent);
                }
                break;
            default:
                sendPushNotification(context, intent);
        }
    }
}
