package com.example.appreview;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingsActivity extends AppCompatActivity {
    private static final String DEFAULT_REMINDER_TIME = "18:00";
    private static final String DEFAULT_APP_USAGE_LENGTH = "00:15";
    private static final String DEFAULT_SNOOZE_LENGTH = "01:00";

    private SharedPreferences preferences;
    private String reminderTime;
    private String appUsageLength;
    private String notificationSnoozeLength;
    private String name;
    private boolean dailyReminderEnabled;
    private boolean appUsageLengthEnabled;
    private boolean notificationSnoozeLengthEnabled;
    private boolean recurringNotificationsEnabled;
    private boolean firstSetup;
    private static final String REGEX_STRING_WITH_COLON = "^(0?\\d|1\\d|2[0-3])?:([0-5]?\\d)$";
    private static final String REGEX_STRING_WITHOUT_COLON = "^(0\\d|1\\d|2[0-3])?([0-5]\\d|^\\d)$";
    private static final String REGEX_STRING_NON_ZERO = ".*[1-9].*";
    private static final String REGEX_STRING_CONTAINS_COLON = ".*:.*";
    private static final Pattern REGEX_PATTERN_WITH_COLON = Pattern.compile(REGEX_STRING_WITH_COLON);
    private static final Pattern REGEX_PATTERN_WITHOUT_COLON = Pattern.compile(REGEX_STRING_WITHOUT_COLON);
    private static final Pattern REGEX_PATTERN_NON_ZERO = Pattern.compile(REGEX_STRING_NON_ZERO);
    private static final Pattern REGEX_PATTERN_CONTAINS_COLON = Pattern.compile(REGEX_STRING_CONTAINS_COLON);
    private static final int ENABLED_COLOR = Color.BLACK;
    private static final int DISABLED_COLOR = Color.GRAY;
    private static final int ERROR_COLOR = Color.RED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getApplicationContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();

        EditText cur;

        cur = (EditText) findViewById(R.id.nameInput);
        cur.setOnEditorActionListener(new EditorActionListener());
        cur.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                name = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        cur = (EditText) findViewById(R.id.NotificationSnoozeLengthTimeInput);
        cur.addTextChangedListener(new TextChangeListener(cur, false, this));
        cur.setOnFocusChangeListener(new FocusChangeListener(false));
        cur.setOnEditorActionListener(new EditorActionListener());

        cur = (EditText) findViewById(R.id.DailyReminderTimeInput);
        cur.addTextChangedListener(new TextChangeListener(cur, true, this));
        cur.setOnFocusChangeListener(new FocusChangeListener(true));
        cur.setOnEditorActionListener(new EditorActionListener());

        cur = (EditText) findViewById(R.id.AppUsageLengthTimeInput);
        cur.addTextChangedListener(new TextChangeListener(cur, false, this));
        cur.setOnFocusChangeListener(new FocusChangeListener(false));
        cur.setOnEditorActionListener(new EditorActionListener());


        getSavedValues();
        updateView();
//        SharedPreferences.Editor editor = preferences.edit();
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
        if(REGEX_PATTERN_CONTAINS_COLON.matcher(time).matches()) {
            boolean matches = REGEX_PATTERN_WITH_COLON.matcher(time).matches();
            boolean nonZero = REGEX_PATTERN_NON_ZERO.matcher(time).matches();
            return  matches && (allowsZero || nonZero);
        } else {
            return REGEX_PATTERN_WITHOUT_COLON.matcher(time).matches() && (allowsZero || REGEX_PATTERN_NON_ZERO.matcher(time).matches());
        }
    }

    private String normalize(CharSequence time){
        if(time.length() == 5){
            return time.toString();
        }

        Matcher matcher;

        if (REGEX_PATTERN_CONTAINS_COLON.matcher(time).matches()){
            matcher = REGEX_PATTERN_WITH_COLON.matcher(time);
        } else {
            matcher = REGEX_PATTERN_WITHOUT_COLON.matcher(time);
        }

        if(!matcher.matches()){
            throw new IllegalArgumentException("Time does not match regex");
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
        SharedPreferences.Editor editor = preferences.edit();
        if(allInputsValid()) {
            if (firstSetup) {
                editor.putBoolean("firstSetup", false);
            }

            if(dailyReminderEnabled) {
                editor.putString("reminderTime", reminderTime);
            }

            if(appUsageLengthEnabled) {
                editor.putString("appUsageLength", appUsageLength);
            }

            if(notificationSnoozeLengthEnabled) {
                editor.putString("notificationSnoozeLength", notificationSnoozeLength);
            }

            editor.putString("name", name);
            editor.putBoolean("dailyReminderEnabled", dailyReminderEnabled);
            editor.putBoolean("appUsageLengthEnabled", appUsageLengthEnabled);
            editor.putBoolean("notificationSnoozeEnabled", notificationSnoozeLengthEnabled);
            editor.putBoolean("recurringNotificationsEnabled", recurringNotificationsEnabled);
            editor.apply();
            Toast.makeText(MainActivity.getAppContext(), "Settings updated", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast error = Toast.makeText(this, "Error saving settings, some are not set correctly", Toast.LENGTH_LONG);
//            error.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0 ,0);
            error.show();
        }
    }

    private synchronized void putValuesToSave(){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("reminderTime", DEFAULT_REMINDER_TIME);
        editor.putString("appUsageLength", DEFAULT_APP_USAGE_LENGTH);
        editor.putString("notificationSnoozeLength", DEFAULT_SNOOZE_LENGTH);
        editor.putString("name", "ho");
        editor.putBoolean("dailyReminderEnabled", dailyReminderEnabled);
        editor.putBoolean("appUsageLengthEnabled", appUsageLengthEnabled);
        editor.putBoolean("notificationSnoozeEnabled", notificationSnoozeLengthEnabled);
        editor.putBoolean("recurringNotificationsEnabled", recurringNotificationsEnabled);
        editor.putBoolean("firstSetup", false);
        editor.apply();
    }

    private void getInputtedValues(){
        name = ((EditText) findViewById(R.id.nameInput)).getText().toString();
        reminderTime = ((EditText) findViewById(R.id.DailyReminderTimeInput)).getText().toString();
        appUsageLength = ((EditText) findViewById(R.id.AppUsageLengthTimeInput)).getText().toString();
        notificationSnoozeLength = ((EditText) findViewById(R.id.NotificationSnoozeLengthTimeInput)).getText().toString();
    }

    private void getSavedValues(){
        reminderTime = preferences.getString("reminderTime", DEFAULT_REMINDER_TIME);
        appUsageLength = preferences.getString("appUsageLength", DEFAULT_APP_USAGE_LENGTH);
        notificationSnoozeLength = preferences.getString("notificationSnoozeLength", DEFAULT_SNOOZE_LENGTH);
        name = preferences.getString("name", "huh");
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
            setAllSiblingsTextColor(reminderTimeInput, DISABLED_COLOR);
            reminderTimeInput.setTextColor(DISABLED_COLOR);
        }

        EditText appUsageLengthInput = findViewById(R.id.AppUsageLengthTimeInput);
        TextInputLayout appUsageLengthTooltip = findViewById(R.id.usageLengthTooltip);
        appUsageLengthInput.setText(appUsageLength);
        appUsageLengthInput.setEnabled(appUsageLengthEnabled);
        appUsageLengthTooltip.setEnabled(appUsageLengthEnabled);
        if(!appUsageLengthEnabled){
            setAllSiblingsTextColor(appUsageLengthInput, DISABLED_COLOR);
            appUsageLengthInput.setTextColor(DISABLED_COLOR);
        }

        EditText snoozeLengthInput = findViewById(R.id.NotificationSnoozeLengthTimeInput);
        TextInputLayout snoozeLengthTooltip = findViewById(R.id.notificationSnoozeLengthTooltip);
        snoozeLengthInput.setText(notificationSnoozeLength);
        snoozeLengthInput.setEnabled(notificationSnoozeLengthEnabled);
        snoozeLengthTooltip.setEnabled(notificationSnoozeLengthEnabled);
        if(!notificationSnoozeLengthEnabled){
            setAllSiblingsTextColor(snoozeLengthInput, DISABLED_COLOR);
            snoozeLengthInput.setTextColor(DISABLED_COLOR);
        }

        Switch dailyReminderSwitch = findViewById(R.id.dailyReminderSwitch);
        dailyReminderSwitch.setChecked(dailyReminderEnabled);

        Switch appUsageLengthSwitch = findViewById(R.id.appUsageLengthSwitch);
        appUsageLengthSwitch.setChecked(appUsageLengthEnabled);

        Switch notificationSnoozeLengthSwitch = findViewById(R.id.notificationSnoozeSwitch);
        notificationSnoozeLengthSwitch.setChecked(notificationSnoozeLengthEnabled);

        Switch recurringNotificationsSwitch = findViewById(R.id.AutomaticRecurrenceSwitch);
        recurringNotificationsSwitch.setChecked(recurringNotificationsEnabled);
        int recurringNotificationsColor = recurringNotificationsEnabled ? ENABLED_COLOR : DISABLED_COLOR;
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
//        if(firstSetup){
//            Button cancel = findViewById(R.id.cancelButton);
//            ((ViewGroup) cancel.getParent()).removeView(cancel);
//        }
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
                    CharSequence normalizedText = normalize(textView.getText());
                    textView.setText(normalizedText);
                    setAllSiblingsTextColor(v, ENABLED_COLOR);
                } else if(hasFocus) {
                    textView.setTextColor(ERROR_COLOR);
                } else {
                    setAllSiblingsTextColor(v, ERROR_COLOR);
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
        private SettingsActivity settings;

        protected TextChangeListener(EditText caller, boolean allowsZeroTime, SettingsActivity settingsActivity){
            this.caller = caller;
            this.allowsZeroTime = allowsZeroTime;
            this.settings = settingsActivity;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(isValidTime(s.toString(), allowsZeroTime)) {
                setAllSiblingsTextColor(caller, ENABLED_COLOR);
                caller.setTextColor(ENABLED_COLOR);
                settings.getInputtedValues();
            } else {
                caller.setTextColor(ERROR_COLOR);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }
}
