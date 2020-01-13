package com.example.appreview;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.regex.Matcher;

import static com.example.appreview.Defaults.convertTimeToLong;

public class SettingsActivity extends AppCompatActivity {
    private String reminderTime;
    private String appUsageLength;
    private String notificationSnoozeLength;
    private String name;
    private boolean dailyReminderEnabled;
    private boolean appUsageLengthEnabled;
    private boolean notificationSnoozeLengthEnabled;
    private boolean recurringNotificationsEnabled;
    private boolean firstSetup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        EditText cur;

        cur = (EditText) findViewById(R.id.nameInput);
        cur.setOnEditorActionListener(new EditorActionListener());

        cur = (EditText) findViewById(R.id.NotificationSnoozeLengthTimeInput);
        cur.addTextChangedListener(new TextChangeListener(cur, false));
        cur.setOnFocusChangeListener(new FocusChangeListener(false));
        cur.setOnEditorActionListener(new EditorActionListener());

        cur = (EditText) findViewById(R.id.DailyReminderTimeInput);
        cur.addTextChangedListener(new TextChangeListener(cur, true));
        cur.setOnFocusChangeListener(new FocusChangeListener(true));
        cur.setOnEditorActionListener(new EditorActionListener());

        cur = (EditText) findViewById(R.id.AppUsageLengthTimeInput);
        cur.addTextChangedListener(new TextChangeListener(cur, false));
        cur.setOnFocusChangeListener(new FocusChangeListener(false));
        cur.setOnEditorActionListener(new EditorActionListener());


        getSavedValues();
        updateView();
    }

    @Override
    public void onBackPressed() {
        if (!firstSetup) {
            super.onBackPressed();
        }
    }

    public void onSwitchClick(View view){
        if(view instanceof Switch) {
            Switch curSwitch = (Switch) view;
            switch (curSwitch.getId()) {
                case R.id.dailyReminderSwitch:
                    dailyReminderEnabled = curSwitch.isChecked();
                    break;
                case R.id.appUsageLengthSwitch:
                    appUsageLengthEnabled = curSwitch.isChecked();
                    break;
                case R.id.notificationSnoozeSwitch:
                    notificationSnoozeLengthEnabled = curSwitch.isChecked();
                    break;
                case R.id.AutomaticRecurrenceSwitch:
                    recurringNotificationsEnabled = curSwitch.isChecked();
                    break;
            }

            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            assert imm != null;
            imm.hideSoftInputFromWindow(curSwitch.getWindowToken(), 0);
            View root = findViewById(R.id.root);
            root.requestFocus();
        }
        updateView();
    }

    private boolean isValidTime(CharSequence time, boolean allowsZero){
        if(Defaults.REGEX_PATTERN_CONTAINS_COLON.matcher(time).matches()) {
            boolean matches = Defaults.REGEX_PATTERN_WITH_COLON.matcher(time).matches();
            boolean nonZero = Defaults.REGEX_PATTERN_NON_ZERO.matcher(time).matches();
            return  matches && (allowsZero || nonZero);
        } else {
            return Defaults.REGEX_PATTERN_WITHOUT_COLON.matcher(time).matches() && (allowsZero || Defaults.REGEX_PATTERN_NON_ZERO.matcher(time).matches());
        }
    }

    private String normalize(String time){
        if(time.length() == 5){
            return time;
        }

        Matcher matcher;

        if (Defaults.REGEX_PATTERN_CONTAINS_COLON.matcher(time).matches()){
            matcher = Defaults.REGEX_PATTERN_WITH_COLON.matcher(time);
        } else {
            matcher = Defaults.REGEX_PATTERN_WITHOUT_COLON.matcher(time);
        }

        if(!matcher.matches()){
            throw new IllegalArgumentException("Time " + time + " does not match regex");
        }

        String hour = matcher.group(1);
        String minutes = matcher.group(2);

        //if no hour inputted, update hour to be all zeroes, otherwise stays the same
        hour = hour == null ? "00" : hour;

        if(hour.length() < 2){
            for(int i = hour.length(); i < 2; i++){
                hour = "0".concat(hour);
            }
        }

        //if no minute inputted, update minute to be all zeroes, otherwise stays the same
        minutes = minutes == null ? "00" : minutes;

        if(minutes.length() < 2){
            for(int i = minutes.length(); i < 2; i++){
                minutes = "0".concat(minutes);
            }
        }

        return hour + ":" + minutes;
    }

    private boolean allInputsValid(){

        if(dailyReminderEnabled && !isValidTime(reminderTime, true)){
            return false;
        }

        if(appUsageLengthEnabled && !isValidTime(appUsageLength, false)){
            return false;
        }

        if(notificationSnoozeLengthEnabled && !isValidTime(notificationSnoozeLength, false)){
            return false;
        }

        if(name.length() == 0){
            return false;
        }

        return true;
    }

    private String getTextFromInput(int id){
        EditText textInput = findViewById(id);
        return String.valueOf(textInput.getText());
    }

    private void setTextForInput(int id, String text){
        EditText textInput = findViewById(id);
        textInput.setText(text);
    }

    public void onCancel(View v){
        Toast.makeText(MainActivity.getAppContext(), "Canceled changes to settings", Toast.LENGTH_SHORT).show();
        finish();
    }

    public void onSave(View v){
        getInputtedValues();
        if(allInputsValid()) {
            firstSetup = false;
            putValuesToSave();
            DailyNotification.updateDailyNotification(this);
            Toast.makeText(MainActivity.getAppContext(), "Settings updated", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast error = Toast.makeText(this, "Error saving settings, some are not set correctly", Toast.LENGTH_LONG);
//            error.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0 ,0);
            error.show();
        }
    }

    private synchronized void putValuesToSave(){
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        if(dailyReminderEnabled) {
            editor.putString("reminderTime", normalize(reminderTime));
        }


        if(appUsageLengthEnabled) {
            editor.putString("appUsageLength", normalize(appUsageLength));
        }

        if(notificationSnoozeLengthEnabled) {
            editor.putString("notificationSnoozeLength", normalize(notificationSnoozeLength));
        }


        editor.putString("name", name);
        editor.putBoolean("dailyReminderEnabled", dailyReminderEnabled);
        editor.putBoolean("appUsageLengthEnabled", appUsageLengthEnabled);
        editor.putBoolean("notificationSnoozeEnabled", notificationSnoozeLengthEnabled);
        editor.putBoolean("recurringNotificationsEnabled", recurringNotificationsEnabled);
        editor.putBoolean("firstSetup", firstSetup);
        editor.apply();
    }

    private void getInputtedValues(){
        name = ((EditText) findViewById(R.id.nameInput)).getText().toString();
        reminderTime = ((EditText) findViewById(R.id.DailyReminderTimeInput)).getText().toString();
        appUsageLength = ((EditText) findViewById(R.id.AppUsageLengthTimeInput)).getText().toString();
        notificationSnoozeLength = ((EditText) findViewById(R.id.NotificationSnoozeLengthTimeInput)).getText().toString();
    }

    private void getSavedValues(){
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        reminderTime = preferences.getString("reminderTime", Defaults.DEFAULT_REMINDER_TIME);
        appUsageLength = preferences.getString("appUsageLength", Defaults.DEFAULT_APP_USAGE_LENGTH);
        notificationSnoozeLength = preferences.getString("notificationSnoozeLength", Defaults.DEFAULT_SNOOZE_LENGTH);
        name = preferences.getString("name", "");
        dailyReminderEnabled = preferences.getBoolean("dailyReminderEnabled", true);
        appUsageLengthEnabled = preferences.getBoolean("appUsageLengthEnabled", true);
        notificationSnoozeLengthEnabled = preferences.getBoolean("notificationSnoozeEnabled", true);
        recurringNotificationsEnabled = preferences.getBoolean("recurringNotificationsEnabled", true);
        firstSetup = preferences.getBoolean("firstSetup", true);
    }

    private void updateView(){
        if(!dailyReminderEnabled && !appUsageLengthEnabled){
            notificationSnoozeLengthEnabled = false;
        }

        TextInputEditText nameInput = findViewById(R.id.nameInput);
        nameInput.setText(name);

        EditText reminderTimeInput = findViewById(R.id.DailyReminderTimeInput);
        TextInputLayout reminderTimeInputLayout = findViewById(R.id.dailReminderTooltip);
        reminderTimeInput.setText(reminderTime);
        reminderTimeInput.setEnabled(dailyReminderEnabled);
        reminderTimeInputLayout.setEnabled(dailyReminderEnabled);
        if(!dailyReminderEnabled) {
            setAllSiblingsTextColor(reminderTimeInput, Defaults.DISABLED_COLOR);
            reminderTimeInput.setTextColor(Defaults.DISABLED_COLOR);
        }

        EditText appUsageLengthInput = findViewById(R.id.AppUsageLengthTimeInput);
        TextInputLayout appUsageLengthTooltip = findViewById(R.id.usageLengthTooltip);
        appUsageLengthInput.setText(appUsageLength);
        appUsageLengthInput.setEnabled(appUsageLengthEnabled);
        appUsageLengthTooltip.setEnabled(appUsageLengthEnabled);
        if(!appUsageLengthEnabled){
            setAllSiblingsTextColor(appUsageLengthInput, Defaults.DISABLED_COLOR);
            appUsageLengthInput.setTextColor(Defaults.DISABLED_COLOR);
        }

        EditText snoozeLengthInput = findViewById(R.id.NotificationSnoozeLengthTimeInput);
        TextInputLayout snoozeLengthTooltip = findViewById(R.id.notificationSnoozeLengthTooltip);
        snoozeLengthInput.setText(notificationSnoozeLength);
        snoozeLengthInput.setEnabled(notificationSnoozeLengthEnabled);
        snoozeLengthTooltip.setEnabled(notificationSnoozeLengthEnabled);
        if(!notificationSnoozeLengthEnabled){
            setAllSiblingsTextColor(snoozeLengthInput, Defaults.DISABLED_COLOR);
            snoozeLengthInput.setTextColor(Defaults.DISABLED_COLOR);
        }

        Switch dailyReminderSwitch = findViewById(R.id.dailyReminderSwitch);
        dailyReminderSwitch.setChecked(dailyReminderEnabled);

        Switch appUsageLengthSwitch = findViewById(R.id.appUsageLengthSwitch);
        appUsageLengthSwitch.setChecked(appUsageLengthEnabled);

        Switch notificationSnoozeLengthSwitch = findViewById(R.id.notificationSnoozeSwitch);
        notificationSnoozeLengthSwitch.setChecked(notificationSnoozeLengthEnabled);

        Switch recurringNotificationsSwitch = findViewById(R.id.AutomaticRecurrenceSwitch);
        recurringNotificationsSwitch.setChecked(recurringNotificationsEnabled);
        int recurringNotificationsColor = recurringNotificationsEnabled ? Defaults.ENABLED_COLOR : Defaults.DISABLED_COLOR;
        setAllSiblingsTextColor(recurringNotificationsSwitch, recurringNotificationsColor);


        int nameNextFocus = R.id.saveButton;
        int dailyReminderNextFocus = R.id.saveButton;
        int appUsageLengthNextFocus = R.id.saveButton;
        int snoozeLengthNextFocus = R.id.saveButton;

        if(dailyReminderEnabled){
            nameNextFocus = R.id.DailyReminderTimeInput;
        } else if(appUsageLengthEnabled){
            nameNextFocus = R.id.AppUsageLengthTimeInput;
        } else if(notificationSnoozeLengthEnabled) {
            nameNextFocus = R.id.NotificationSnoozeLengthTimeInput;
        }

        if(dailyReminderEnabled) {
            if (appUsageLengthEnabled) {
                dailyReminderNextFocus = R.id.AppUsageLengthTimeInput;
            } else if (notificationSnoozeLengthEnabled){
                dailyReminderNextFocus = R.id.NotificationSnoozeLengthTimeInput;
            }
        }

        if(appUsageLengthEnabled && notificationSnoozeLengthEnabled){
            appUsageLengthNextFocus = R.id.NotificationSnoozeLengthTimeInput;
        }

        setAllNext(nameInput, nameNextFocus);
        setAllNext(reminderTimeInput, dailyReminderNextFocus);
//        setAllNext(reminderTimeInputLayout, dailyReminderNextFocus);
        setAllNext(appUsageLengthInput, appUsageLengthNextFocus);
//        setAllNext(appUsageLengthTooltip, appUsageLengthNextFocus);
        setAllNext(snoozeLengthInput, snoozeLengthNextFocus);
//        reminderTimeInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
        if(firstSetup){
            Button cancel = findViewById(R.id.cancelButton);
            ((ViewGroup) cancel.getParent()).removeView(cancel);
            Toolbar toolbar = findViewById(R.id.toolbar);
            toolbar.setTitle("Settings - First time setup");
            setSupportActionBar(toolbar);
        }
    }

    private void setAllNext(EditText edit, int next){
        edit.setNextFocusLeftId(next);
        edit.setNextFocusRightId(next);
        edit.setNextFocusUpId(next);
        edit.setNextFocusDownId(next);
        edit.setNextFocusForwardId(next);

        if(next == R.id.saveButton){
            edit.setImeOptions(EditorInfo.IME_ACTION_DONE);
        } else {
            edit.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            edit.setNextClusterForwardId(next);
        }
    }

    private void setAllSiblingsTextColor(View cur, int color){
        ViewParent parent = cur.getParent();

        while(parent != null && !(parent instanceof TableRow)) {
            parent = parent.getParent();
        }

        if(parent != null) {
            ViewGroup parentContainer = (ViewGroup) parent;
            for (int i = 0; i < parentContainer.getChildCount(); i++) {
                View v = parentContainer.getChildAt(i);
                if (parentContainer.getChildAt(i) instanceof TextView) {
                    TextView textView = (TextView) parentContainer.getChildAt(i);
                    textView.setTextColor(color);
                }
            }
        }
    }


    private class FocusChangeListener implements View.OnFocusChangeListener{
        boolean allowsZeroTime;

        protected FocusChangeListener(boolean allowsZeroTime){
            this.allowsZeroTime = allowsZeroTime;
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if(v instanceof TextView) {
                TextView textView = (TextView) v;
                if (isValidTime(textView.getText(), allowsZeroTime)) {
                    if(!hasFocus) {
                        CharSequence normalizedText = normalize(textView.getText().toString());
                        textView.setText(normalizedText);
                    }
                    setAllSiblingsTextColor(v, Defaults.ENABLED_COLOR);
                } else if(hasFocus) {
                    textView.setTextColor(Defaults.ERROR_COLOR);
                } else {
                    setAllSiblingsTextColor(v, Defaults.ERROR_COLOR);
                }
            }
        }
    }

    private class EditorActionListener implements TextView.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                //Clear focus here from edittext
                View root = findViewById(R.id.root);
                root.requestFocus();
//                 Button b = findViewById(R.id.saveButton);
//                 b.requestFocus();
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                assert imm != null;
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return true;
            }
            return false;
        }
    }

    private class TextChangeListener implements TextWatcher {
        private EditText caller;
        private boolean allowsZeroTime;

        protected TextChangeListener(EditText caller, boolean allowsZeroTime){
            this.caller = caller;
            this.allowsZeroTime = allowsZeroTime;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if(isValidTime(s.toString(), allowsZeroTime)) {
                setAllSiblingsTextColor(caller, Defaults.ENABLED_COLOR);
                caller.setTextColor(Defaults.ENABLED_COLOR);
                updateModelText(caller);
            } else {
                caller.setTextColor(Defaults.ERROR_COLOR);
            }
        }
    }

    private void updateModelText(EditText caller) {
        switch (caller.getId()){
            case R.id.nameInput :
                name = caller.getText().toString();
                break;
            case R.id.DailyReminderTimeInput:
                reminderTime = caller.getText().toString();
                break;
            case R.id.AppUsageLengthTimeInput:
                appUsageLength = caller.getText().toString();
                break;
            case R.id.NotificationSnoozeLengthTimeInput:
                notificationSnoozeLength = caller.getText().toString();
                break;
        }
    }
}
