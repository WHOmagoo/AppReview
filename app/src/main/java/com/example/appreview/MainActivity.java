package com.example.appreview;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import android.content.SharedPreferences;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;

import static androidx.core.content.FileProvider.getUriForFile;

public class MainActivity extends AppCompatActivity implements Observer {
    private static Context context;

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

            Toast.makeText(super.getApplicationContext(), "Settings have not been implemented", Toast.LENGTH_SHORT).show();

            return true;
        }

        if(id == R.id.action_increment_day){
            UserData.getInstance().nextDay();
            return true;
        }


        if(id == R.id.action_spoof_review_first){
            DailyData d = UserData.getInstance().getCurrentData();
            d.setAnxietyLevelBefore((int) (Math.random() * 11));
            d.setHappinessLevelBefore((int) (Math.random() * 11));
        }

        if(id == R.id.action_spoof_review_second){
            DailyData d = UserData.getInstance().getCurrentData();
            d.setAnxietyLevelAfter((int) (Math.random() * 11));
            d.setHappinessLevelAfter((int) (Math.random() * 11));
        }

        if(id == R.id.action_export_csv){
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

//                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
//                intent.addCategory(Intent.CATEGORY_OPENABLE);
//                intent.setType("application/txt");
//                intent.putExtra(Intent.EXTRA_TITLE, "userData.csv");
//
//                // Optionally, specify a URI for the directory that should be opened in
//                // the system file picker when your app creates the document.
//                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, contentUri);
//
//                startActivityForResult(intent, 0);


//                Uri uri = Uri.fromFile(context.getFileStreamPath("userData.csv"));

                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setDataAndType(contentUri, "text/plain");
                sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                sharingIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
//                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "CSV Data!");
//                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Extra text");
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
            }catch (IOException e) {
                    e.printStackTrace();
                }
        }

        //noinspection SimplifiableIfStatement
//
//        if (id == R.id.action_settings) {
//            return true;
//        }
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

    private void updateView(){
        updateDayCounter();
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
