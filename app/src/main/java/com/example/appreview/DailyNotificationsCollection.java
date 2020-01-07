package com.example.appreview;

public class DailyNotificationsCollection {
    private static final DailyNotificationsCollection ourInstance = new DailyNotificationsCollection();

    public static DailyNotificationsCollection getInstance() {
        return ourInstance;
    }

    private DailyNotificationsCollection() {
    }
}
