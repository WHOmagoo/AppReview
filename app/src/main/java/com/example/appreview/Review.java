package com.example.appreview;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Toast;

public class Review extends AppCompatActivity {

    boolean anxietyUpdated = false;
    boolean happinessUpdated = false;
    int anxietyLevel = 0;
    int happinessLevel = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        RatingBar anxietyRatingBar = findViewById(R.id.anxiety_slider);
        anxietyRatingBar.setOnRatingBarChangeListener(change);
        RatingBar happinessRatingBar = findViewById(R.id.happiness_slider);
        happinessRatingBar.setOnRatingBarChangeListener(change);
    }

    private void enableButton(){
        Button b = findViewById(R.id.submit_ratings);
        b.setEnabled(true);
    }

    public void onSubmitData(View view) {
//        Toast.makeText(this, "Launch app", Toast.LENGTH_SHORT).show();
//        TextView status = (TextView) findViewById(R.id.dailyStatus);
//        status.setText(R.string.appUsed);
//        incrementDayCounter();
        Context ctx = this;

        try {
            final String app = "com.android.chrome";
            Intent i = ctx.getPackageManager().getLaunchIntentForPackage(app);
            ctx.startActivity(i);
        } catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
