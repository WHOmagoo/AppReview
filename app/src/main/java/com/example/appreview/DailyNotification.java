package com.example.appreview;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import java.util.Calendar;

import static com.example.appreview.Defaults.convertTimeToLong;

public class DailyNotification {
    private static void updateDailyNotification(Context context, long reminderTime){
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, SendNotificationReceiver.class);
        intent.putExtra("type", Defaults.DAILY_NOTIFICATION_ID);
        intent.putExtra("title", "Don't forget to review the app today!");
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, Defaults.DAILY_NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (reminderTime != -1) {
            assert alarmMgr != null;
            // Use if you wish to schedule alarm to start tomorrow vs today.
            Calendar cur = Calendar.getInstance();
//
            int remindHour = (int) (reminderTime / (1000*60*60));
            cur.set(Calendar.HOUR_OF_DAY, remindHour);
            cur.set(Calendar.MINUTE, (int) ((reminderTime - remindHour) / (1000*60)));

            alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, cur.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);
        }
    }

    public static void updateDailyNotification(Context context){
        SharedPreferences preferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        String reminderTime = preferences.getString("reminderTime", Defaults.DEFAULT_REMINDER_TIME);
        boolean dailyReminderEnabled = preferences.getBoolean("dailyReminderEnabled", true);
        long lReminderTime = dailyReminderEnabled ? convertTimeToLong(reminderTime) : -1;
        DailyNotification.updateDailyNotification(context, lReminderTime);
    }
}
