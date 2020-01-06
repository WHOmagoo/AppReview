package com.example.appreview;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Observable;
import java.util.Observer;

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
