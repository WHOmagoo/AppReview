package com.example.appreview;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import static com.example.appreview.Defaults.convertTimeToLong;

public class OnBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        DailyNotification.updateDailyNotification(context);
        NotificationSnoozer.ScheduleSecondReviewPushNotification(context);
    }
}
