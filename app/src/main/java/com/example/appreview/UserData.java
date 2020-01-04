package com.example.appreview;

import java.util.ArrayList;
import java.util.Calendar;

public class UserData {
    private static final UserData ourInstance = new UserData();
    int curDay;

    ArrayList<DailyData> data;
    DailyData curData;

    public static UserData getInstance() {
        return ourInstance;
    }

    private UserData() {
        data = new ArrayList<>();
    }

    public int getCurDay(){
        return curDay;
    }

    /***
     *
     * @return The data that represents today, could be null
     */
    public DailyData getCurrentData(){
        return curData;
    }

    public DailyData nextDay(){
        curDay++;
        DailyData todaysData = new DailyData(Calendar.getInstance().getTime(), curDay);
        data.add(todaysData);
        curData = todaysData;
        return todaysData;
    }

    public void SkipDay(){
        curDay++;
        data.add(null);
        curData = null;
    }
}
