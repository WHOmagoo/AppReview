package com.example.appreview;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

public class DailyNotification {
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    public DailyNotification(Context context, long reminderTime){
        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, SendNotificationReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        setReminderTime(reminderTime);
//        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
//                SystemClock.elapsedRealtime() +
//                        30 * 1000,30 * 1000, alarmIntent);
    }

    public void setReminderTime(long reminderTime){
        //TODO set interval to 24 hours and not on minute
//        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, reminderTime, 60000, alarmIntent);
        alarmMgr.cancel(alarmIntent);
        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() +
                        30 * 1000,30 * 1000, alarmIntent);
    }
}
