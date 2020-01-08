package com.example.appreview;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;

import com.google.android.material.textfield.TextInputEditText;

public class SettingsActivity extends AppCompatActivity {
    private static final String DEFAULT_REMINDER_TIME = "18:00";
    private static final String DEFAULT_APP_USAGE_LENGTH = "00:15";
    private static final String DEFAULT_SNOOZE_LENGTH = "01:00";

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getApplicationContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
        updateView();
//        SharedPreferences.Editor editor = preferences.edit();
    }

    public void onSwitchClick(View view){
        if(view instanceof Switch) {
            SharedPreferences.Editor editor = preferences.edit();
            Switch curSwitch = (Switch) view;
            switch (curSwitch.getId()) {
                case R.id.dailyReminderSwitch:
                    editor.putBoolean("dailyReminderEnabled", curSwitch.isChecked());
                    break;
                case R.id.appUsageLengthSwitch:
                    editor.putBoolean("appUsageLengthEnabled", curSwitch.isChecked());
                    break;
                case R.id.notificationSnoozeSwitch:
                    editor.putBoolean("notificationSnoozeEnabled", curSwitch.isChecked());
                    break;
                case R.id.AutomaticRecurrenceSwitch:
                    editor.putBoolean("recurringNotificationsEnabled", curSwitch.isChecked());
                    break;
            }

            editor.apply();
        }
        updateView();
    }

    private void updateView(){
        String reminderTime = preferences.getString("reminderTime", DEFAULT_REMINDER_TIME);
        String appUsageLength = preferences.getString("appUsageLength", DEFAULT_APP_USAGE_LENGTH);
        String notificationSnoozeLength = preferences.getString("notificationSnoozeLength", DEFAULT_SNOOZE_LENGTH);
        String name = preferences.getString("name", "");
        boolean dailyReminderEnabled = preferences.getBoolean("dailyReminderEnabled", false);
        boolean appUsageLengthEnabled = preferences.getBoolean("appUsageLengthEnabled", true);
        boolean notificationSnoozeLengthEnabled = preferences.getBoolean("notificationSnoozeEnabled", true);
        boolean recurringNotificationsEnabled = preferences.getBoolean("recurringNotificationsEnabled", true);

        if(!dailyReminderEnabled && !appUsageLengthEnabled){
            notificationSnoozeLengthEnabled = false;
        }

        TextInputEditText nameInput = findViewById(R.id.nameInput);
        nameInput.setText(name);

        EditText reminderTimeInput = findViewById(R.id.DailyReminderTimeInput);
        reminderTimeInput.setText(reminderTime);
        reminderTimeInput.setEnabled(dailyReminderEnabled);
        reminderTimeInput.setFocusable(dailyReminderEnabled);

        EditText appUsageLengthInput = findViewById(R.id.AppUsageLengthTimeInput);
        appUsageLengthInput.setText(appUsageLength);
        appUsageLengthInput.setEnabled(appUsageLengthEnabled);
        appUsageLengthInput.setFocusable(appUsageLengthEnabled);

        EditText snoozeLengthInput = findViewById(R.id.NotificationSnoozeLengthTimeInput);
        snoozeLengthInput.setText(notificationSnoozeLength);
        snoozeLengthInput.setEnabled(notificationSnoozeLengthEnabled);
        snoozeLengthInput.setFocusable(appUsageLengthEnabled);

        Switch dailyReminderSwitch = findViewById(R.id.dailyReminderSwitch);
        dailyReminderSwitch.setChecked(dailyReminderEnabled);

        Switch appUsageLengthSwitch = findViewById(R.id.appUsageLengthSwitch);
        appUsageLengthSwitch.setChecked(appUsageLengthEnabled);

        Switch notificationSnoozeLengthSwitch = findViewById(R.id.notificationSnoozeSwitch);
        notificationSnoozeLengthSwitch.setChecked(notificationSnoozeLengthEnabled);

        Switch recurringNotificationsSwitch = findViewById(R.id.AutomaticRecurrenceSwitch);
        recurringNotificationsSwitch.setChecked(recurringNotificationsEnabled);
    }
}
