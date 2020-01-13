package com.example.appreview;

import android.graphics.Color;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Defaults {
    static final String DEFAULT_REMINDER_TIME = "18:00";
    static final String DEFAULT_APP_USAGE_LENGTH = "00:15";
    static final String DEFAULT_SNOOZE_LENGTH = "00:02";
//    static final String DEFAULT_SNOOZE_LENGTH = "01:00";
    static final String REGEX_STRING_WITH_COLON = "^(0?\\d|1\\d|2[0-3])?:([0-5]?\\d)$";
    static final String REGEX_STRING_WITHOUT_COLON = "^(0\\d|1\\d|2[0-3])?([0-5]\\d|^\\d)$";
    static final String REGEX_STRING_NON_ZERO = ".*[1-9].*";
    static final String REGEX_STRING_CONTAINS_COLON = ".*:.*";
    static final Pattern REGEX_PATTERN_WITH_COLON = Pattern.compile(REGEX_STRING_WITH_COLON);
    static final Pattern REGEX_PATTERN_WITHOUT_COLON = Pattern.compile(REGEX_STRING_WITHOUT_COLON);
    static final Pattern REGEX_PATTERN_NON_ZERO = Pattern.compile(REGEX_STRING_NON_ZERO);
    static final Pattern REGEX_PATTERN_CONTAINS_COLON = Pattern.compile(REGEX_STRING_CONTAINS_COLON);
    static final int ENABLED_COLOR = Color.BLACK;
    static final int DISABLED_COLOR = Color.GRAY;
    static final int ERROR_COLOR = Color.RED;

    static final int CLICKED_NOTIFICATION_ID = 0;
    static final int DAILY_NOTIFICATION_ID = 1;
    static final int SNOOZED_NOTIFICATION_ID = 2;
    static final int APP_USAGE_REMINDER = 3;

    public static long convertTimeToLong(String time){

        Matcher matcher = Defaults.REGEX_PATTERN_WITH_COLON.matcher(time);
            try {
                if(matcher.matches()) {
                    String sHour = matcher.group(1);
                    String sMinute = matcher.group(2);
                    long lHour = Long.parseLong(sHour);
                    long lMinute = Long.parseLong(sMinute);
                    long result = lHour * 1000 * 60 * 60 + lMinute * 1000 * 60;
                    return result;
                } else {
                    throw new IllegalArgumentException(time + " did not match the regular expression");
                }
            } catch (Error e) {
                throw new IllegalArgumentException(time + " did not match the regular expression");
            }
    }
}