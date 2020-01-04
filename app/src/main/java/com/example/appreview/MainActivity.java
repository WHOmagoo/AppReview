package com.example.appreview;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private int dayCount = 0;
    private boolean reviewedToday = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        incrementDayCounter();
        setReviewedToday(false);
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
            incrementDayCounter();
            return true;
        }

        if(id == R.id.action_increment_day){
            incrementDayCounter();
            return true;
        }


        if(id == R.id.action_spoof_review){
            setReviewedToday(true);
        }

        //noinspection SimplifiableIfStatement
//
//        if (id == R.id.action_settings) {
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }

    private void setReviewedToday(boolean b){
        if(b != reviewedToday){
            TextView reviewSplash = (TextView) findViewById(R.id.dailyStatus);
            if(b){
                reviewSplash.setText(getResources().getString(R.string.appUsed));
            } else {
                reviewSplash.setText(getResources().getString(R.string.appUnused));
            }

            reviewedToday = b;
        }
    }

    private void incrementDayCounter(){
        TextView dayText = (TextView) findViewById(R.id.dayCounterText);
        dayCount++;
        String newText = String.format(getResources().getString(R.string.day), dayCount);
        dayText.setText(newText);
        setReviewedToday(false);
    }

    public void onClickBtn(View view) {
        Toast.makeText(this, "Page to be implemented", Toast.LENGTH_LONG).show();
//        TextView status = (TextView) findViewById(R.id.dailyStatus);
//        status.setText(R.string.appUsed);
//        incrementDayCounter();
    }
}
