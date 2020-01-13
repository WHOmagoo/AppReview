package com.example.appreview;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;

import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

public class ReviewActivity extends AppCompatActivity {

    boolean anxietyUpdated = false;
    boolean happinessUpdated = false;
    int anxietyLevel = 0;
    int happinessLevel = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!UserData.getInstance().hasAppUsed()) {
            setTitle(getString(R.string.title_activity_review_first));
        } else {
            setTitle(getString(R.string.title_activity_review_second));
        }

        setContentView(R.layout.activity_review);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        Button b = findViewById(R.id.submit_ratings);
        b.setEnabled(false);

        RatingBar.OnRatingBarChangeListener change = new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                switch (ratingBar.getId()){
                    case R.id.anxiety_slider:
                        anxietyUpdated = true;
                        anxietyLevel = (int) (v *2);
                        break;
                    case R.id.happiness_slider:
                        happinessUpdated = true;
                        happinessLevel = (int) (v *2);
                        break;
                }

                if(anxietyUpdated && happinessUpdated){
                    enableButton();
                }
            }
        };

        TextView instructions = (TextView) findViewById(R.id.review_instructions);

        if(!UserData.getInstance().hasAppUsed()){
            instructions.setText(getString(R.string.review_description_pre));
            b.setText(getString(R.string.review_submit_launch));
        } else {
            instructions.setText(getString(R.string.review_description_post));
            b.setText(getString(R.string.submit));
        }


        RatingBar anxietyRatingBar = findViewById(R.id.anxiety_slider);
        anxietyRatingBar.setOnRatingBarChangeListener(change);
        RatingBar happinessRatingBar = findViewById(R.id.happiness_slider);
        happinessRatingBar.setOnRatingBarChangeListener(change);

        UserData.getInstance().updateDay();
    }

    @Override
    public void onStart(){
        super.onStart();
        UserData.getInstance().updateDay();
    }


    private void enableButton(){
        Button b = findViewById(R.id.submit_ratings);
        b.setEnabled(true);
    }

    public void onSubmitData(View view) {
//        Toast.makeText(this, "Launch app", Toast.LENGTH_SHORT).show();
//        TextView status = (TextView) findViewById(R.id.dailyStatus);
//        status.setText(R.string.hasAppUsed);
//        incrementDayCounter();
        Context ctx = this;

        DailyData currentData = UserData.getInstance().getCurrentData();

        if(!UserData.getInstance().hasAppUsed()){
            currentData.setAnxietyLevelBefore(anxietyLevel);
            currentData.setHappinessLevelBefore(happinessLevel);

            SharedPreferences preferences = getSharedPreferences("settings", MODE_PRIVATE);
            if(preferences.getBoolean("appUsageLengthEnabled", true)){
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

                Intent notification = new Intent(getApplicationContext(), SendNotificationReceiver.class);
                notification.putExtra("type", Defaults.APP_USAGE_REMINDER);
                notification.putExtra("title", "Don't forget your second review");
                notification.putExtra("id", 55);

                PendingIntent intent = PendingIntent.getBroadcast(this, Defaults.APP_USAGE_REMINDER, notification, PendingIntent.FLAG_UPDATE_CURRENT);

                String sTimeToWait = preferences.getString("appUsageLength", Defaults.DEFAULT_APP_USAGE_LENGTH);
                long timeToWait = Defaults.convertTimeToLong(sTimeToWait);

                if (alarmManager != null) {
                    alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 0, intent);
                } else {
                    Log.d("Review Activity", "Could not queue app usage reminder");
                }
            }


            try {
                final String app1 = "com.CedVR.aVRPlaceX";
                final String app2 = "com.CedVR.aVRPlaceY";

                Intent i = ctx.getPackageManager().getLaunchIntentForPackage(app1);
                if(i == null) {
                    i = ctx.getPackageManager().getLaunchIntentForPackage(app2);
                }

                if(i == null){
                    Toast.makeText(this, "Error: Could not find app " + app1 + " or " + app2, Toast.LENGTH_LONG).show();
                } else {
                    ctx.startActivity(i);
                }
            } catch (Exception e){
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            currentData.setAnxietyLevelAfter(anxietyLevel);
            currentData.setHappinessLevelAfter(happinessLevel);
        }

        finish();
    }
}
