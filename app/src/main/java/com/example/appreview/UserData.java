package com.example.appreview;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class UserData extends Observable implements Observer {
    private static final UserData ourInstance = new UserData(MainActivity.getAppContext());
    private int curDay;

    private ArrayList<DailyData> data;
    private DailyData curData;
    private String identifier;

    SharedPreferences userDataSaved;

    public static UserData getInstance() {
        return ourInstance;
    }

    private UserData(Context context) {
        data = new ArrayList<DailyData>();

        boolean created = false;

        userDataSaved = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        int count = userDataSaved.getInt("count", 0);

        identifier = userDataSaved.getString("id", "J Doe");


        for(int i = 1; i <= count; i++){
            String curDayData = userDataSaved.getString("day" + i, "20/01/01,"+ i + ",,,,");
            curData = DailyData.makeDailyDataFromCSV(curDayData);
            data.add(curData);
        }

        curDay = count;

        if(count == 0){
            nextDay();
        } else {
            curData.addObserver(this);
        }
    }

//    /****
//     * If ourInstance has already been initialized, will not make new instance
//     * @param context the android context
//     * @return ourInstance
//     */
//    public static UserData makeInstance(Context context){
//        return ourInstance;
//    }

    public void setIdentifier(String id){
        identifier = id;
    }

    public int getCurDay(){
        return curDay;
    }

    public String getIdentifier(){
        return identifier;
    }

    /***
     *
     * @return The data that represents today, never is null
     */
    public DailyData getCurrentData(){
        return curData;
    }

    public DailyData nextDay(){
        if(curData != null){
            curData.deleteObserver(this);
        }
        curDay++;
        Date curDate = Calendar.getInstance().getTime();
        DailyData todaysData = new DailyData(curDate, curDay);
        data.add(todaysData);
        curData = todaysData;

        curData.addObserver(this);

        setChanged();
        notifyObservers();

        writeDay();

        return todaysData;
    }

    public static String makeCSVHeading(){
        return "Identifier," + DailyData.getCSVHeadings();
    }

    public String makeCSV(){
        StringBuilder sb = new StringBuilder();

        sb.append(makeCSVHeading());
        sb.append("\n");

        for (int i = 0; i < data.size(); i++) {
            sb.append(identifier);
            sb.append(",");
            sb.append(data.get(i).makeCSV());
            sb.append("\n");
        }

        return new String(sb);
    }

    private void makeUserDataFromCSV(String csv){
        Scanner scanner = new Scanner(csv);
        makeUserDataFromCSV(scanner);
    }

    private void makeUserDataFromCSV(Scanner scanner){
        scanner.useDelimiter(",");
        scanner.nextLine();
        while(scanner.hasNext()){
            identifier = scanner.next();
            data.add(DailyData.makeDailyDataFromCSV(scanner));
        }

        if(data.size() > 0) {
            DailyData lastEntry = data.get(data.size() - 1);
            Date startingDate = lastEntry.getDate();
            int lastDayNum = lastEntry.getDayNum();

            Date currentDate = Calendar.getInstance().getTime();

            float daysBetween = TimeUnit.DAYS.convert(currentDate.getTime() - startingDate.getTime(), TimeUnit.MILLISECONDS);
            for(; lastDayNum <= daysBetween; lastDayNum++){
                nextDay();
            }
        } else {
            nextDay();
        }
    }

    public boolean hasAppUsed() {
        return curData.appUsed();
    }

    public boolean hasTaskFinished(){
        return curData.taskFinished();
    }

    private void writeDay(){
        SharedPreferences.Editor editor = userDataSaved.edit();

        editor.putString("day" + curDay, curData.makeCSV());
        editor.putInt("count", curDay);
        editor.apply();
    }

    @Override
    public void update(Observable observable, Object o) {
        if(observable instanceof DailyData){
            writeDay();

            setChanged();
            notifyObservers();
        }
    }
}
