package com.example.appreview;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private int dayCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        incrementDayCounter();
    }

    private void incrementDayCounter(){
        TextView dayText = (TextView) findViewById(R.id.dayCounterText);
        dayCount++;
        String newText = String.format(getResources().getString(R.string.day), dayCount);
        dayText.setText(newText);
    }

    public void onClickBtn(View view) {
        TextView status = (TextView) findViewById(R.id.dailyStatus);
        status.setText(R.string.appUsed);
        incrementDayCounter();
    }
}
