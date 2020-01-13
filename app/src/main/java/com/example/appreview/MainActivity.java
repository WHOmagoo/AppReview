package com.example.appreview;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;

import android.content.SharedPreferences;

import android.os.SystemClock;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity implements Observer {
    private static Context context;
    public static final String CHANNEL_ID = "All notifications";
    public static final int NOTIFICATION_ID = 23;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        context = getApplicationContext();

        updateView();
        UserData.getInstance().addObserver(this);
        SharedPreferences preferences = context.getSharedPreferences("settings", 0);
        if(!preferences.getBoolean("storageMade", false)){
            File directory = new File(context.getExternalCacheDir() + File.separator + "share");
            SharedPreferences.Editor editor = preferences.edit();
            boolean result = directory.mkdirs();
            editor.putBoolean("storageMade", result);
            editor.apply();
        }

        UserData.getInstance().updateDay();
        DailyNotification.updateDailyNotification(getApplicationContext());


    }

    @Override
    public void onStart(){
        super.onStart();
        SharedPreferences prefs = context.getSharedPreferences("settings", MODE_PRIVATE);
        if(prefs.getBoolean("firstSetup", true)) {
            Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(i);

        } else {
            updateView();
            UserData.getInstance().updateDay();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.action_settings){
            Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(i);

            return true;
//        } else if(id == R.id.action_increment_day){
//            UserData.getInstance().nextDay();
//            return true;
//        } else if(id == R.id.action_spoof_review_first){
//            DailyData d = UserData.getInstance().getCurrentData();
//            d.setAnxietyLevelBefore((int) (Math.random() * 11));
//            d.setHappinessLevelBefore((int) (Math.random() * 11));
//        } else if(id == R.id.action_spoof_review_second){
//            DailyData d = UserData.getInstance().getCurrentData();
//            d.setAnxietyLevelAfter((int) (Math.random() * 11));
//            d.setHappinessLevelAfter((int) (Math.random() * 11));
        } else if(id == R.id.action_share){
            String generatedCSVData = UserData.getInstance().makeCSV();

            FileOutputStream fOut = null;
            try {
                File csvPath = new File(context.getExternalCacheDir() + "/share", "userData.csv");
//                File file = new File(context.getCacheDir(), "userData.csv");
                fOut = new FileOutputStream(csvPath);



                OutputStreamWriter osw = new OutputStreamWriter(fOut);

                // Write the string to the file
                osw.write(generatedCSVData);

                /* ensure that everything is
                 * really written out and close */
                osw.flush();
                osw.close();

                Scanner reader = new Scanner(new FileReader(csvPath));

                while(reader.hasNextLine()){
                    System.out.println(reader.nextLine());
                }

                System.out.println(csvPath);
                Uri contentUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", csvPath);
//                Intent sharingIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                sharingIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.default_subject_line));
                sharingIntent.putExtra(Intent.ACTION_SENDTO, getString(R.string.default_email_recipient));
                sharingIntent.setType("text/plain");
                startActivity(Intent.createChooser(sharingIntent, "Share CSV file via"));
            }catch (IOException e) {
                e.printStackTrace();
            }
        } else if (id == R.id.copy_data){
            ClipboardManager myClipboard;
            myClipboard = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
            String csvString = UserData.getInstance().makeCSV();
            ClipData csvClipData = ClipData.newPlainText("text", csvString);
            assert myClipboard != null;
            myClipboard.setPrimaryClip(csvClipData);
//        } else if (id == R.id.action_send_push_notification){
////            sendPushNotification();
//            SendNotificationReceiver r = new SendNotificationReceiver();
//            r.sendPushNotification(this, "Don't forget to review the app today", null, id, 1);
        }

        return super.onOptionsItemSelected(item);
    }

    public static Context getAppContext() {
        return context;
    }

    private void setReviewedToday(boolean b){
        TextView reviewSplash = (TextView) findViewById(R.id.dailyStatus);
        if(b){
            reviewSplash.setText(getResources().getString(R.string.appUsed));
        } else {
            reviewSplash.setText(getResources().getString(R.string.appUnused));
        }
    }

    private void setDayFinished(boolean b){
        if(b){
            TextView reviewSplash = (TextView) findViewById(R.id.dailyStatus);
            reviewSplash.setText(getResources().getString(R.string.statusDone));
        } else {
            //Reset the text box if going from finished to non finished
        }
    }

    private void updateDayCounter(){
        TextView dayText = (TextView) findViewById(R.id.dayCounterText);
        String newText = String.format(getResources().getString(R.string.day), UserData.getInstance().getCurDay());
        dayText.setText(newText);
        setReviewedToday(false);
    }

    public void onClickBtn(View view) {
        Intent i = new Intent(getApplicationContext(), ReviewActivity.class);
        startActivity(i);
//        TextView status = (TextView) findViewById(R.id.dailyStatus);
//        status.setText(R.string.hasAppUsed);
//        updateDayCounter();
    }

    private void updateDailyProgress(){
        ImageView firstReviewStatus = findViewById(R.id.firstReviewStatus);
        ImageView appStatus = findViewById(R.id.appStatus);
        ImageView secondReviewStatus = findViewById(R.id.secondReviewStatus);


        if(UserData.getInstance().hasAppUsed()) {
            firstReviewStatus.setImageResource(R.mipmap.review_icon);

            appStatus.setImageResource(R.mipmap.video_game_icon_completed);
            if (UserData.getInstance().hasTaskFinished()){
                secondReviewStatus.setImageResource(R.mipmap.review_icon);
            }
        } else {
            firstReviewStatus.setImageResource(R.mipmap.review_pending_icon);
            secondReviewStatus.setImageResource(R.mipmap.review_pending_icon);
            appStatus.setImageResource(R.mipmap.video_game_pending_icon);
        }
    }

    private void updateView(){
        updateDayCounter();
        updateDailyProgress();
        setReviewedToday(UserData.getInstance().hasAppUsed());
        setDayFinished(UserData.getInstance().hasTaskFinished());

        Button submit = (Button) findViewById(R.id.start_review_button);

        if(UserData.getInstance().hasTaskFinished()){
            submit.setEnabled(false);
            submit.setText(getString(R.string.start_review_button_disabled_text));
        } else {
            submit.setEnabled(true);
            submit.setText(getString(R.string.start_review_button_enabled_text));
        }
    }

    @Override
    public void update(Observable observable, Object o) {
        updateView();
    }
}
