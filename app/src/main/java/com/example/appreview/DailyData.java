package com.example.appreview;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Observable;
import java.util.Scanner;

public class DailyData extends Observable {
    private int happinessLevelBefore;
    private int happinessLevelAfter;
    private int anxietyLevelBefore;
    private int anxietyLevelAfter;

    public Date getDate() {
        return date;
    }

    private Date date;
    private int dayNum;
    public static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

    public DailyData(Date date, int dayNum){
        this.date = date;
        this.dayNum = dayNum;
        happinessLevelBefore = -1;
        happinessLevelAfter = -1;
        anxietyLevelBefore = -1;
        anxietyLevelAfter = -1;
    }

    public static DailyData makeDailyDataFromCSV(String s){
        Scanner scanner = new Scanner(s);
        scanner.useDelimiter(",");
        return makeDailyDataFromCSV(scanner);
    }

    public static DailyData makeDailyDataFromCSV(Scanner scanner) {
        Date date = null;
        try {
            date = dateFormat.parse(scanner.next());
        } catch (ParseException e) {
            try {
                //This shouldn't fail unless the dateFormat changes and this doesn't get updated
                date = dateFormat.parse("20/01/01");
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
        }

        int dayNum = -1;
        try {
            dayNum = scanner.nextInt();
        } catch (InputMismatchException ignored){}

        DailyData result = new DailyData(date, dayNum);

        try {
            try {
                result.happinessLevelBefore = scanner.nextInt();
            } catch (InputMismatchException ignored) {
            }

            try {
                result.happinessLevelAfter = scanner.nextInt();
            } catch (InputMismatchException ignored) {
            }

            try {
                result.anxietyLevelBefore = scanner.nextInt();
            } catch (InputMismatchException ignored) {
            }

            try {
                result.anxietyLevelAfter = scanner.nextInt();
            } catch (InputMismatchException ignored) {
            }
        } catch (NoSuchElementException ignored){}

        return result;
    }

    public boolean setHappinessLevelBefore(int level){
        if(intOutOfBounds(level)){
            return false;
        }

        happinessLevelBefore = level;
        if(!intOutOfBounds(anxietyLevelBefore)){
            setChanged();
            notifyObservers(false);
        }
        return true;
    }

    public boolean setHappinessLevelAfter(int level){
        if(intOutOfBounds(level)){
            return false;
        }

        happinessLevelAfter = level;

        if(!intOutOfBounds(anxietyLevelAfter)){
            setChanged();
            notifyObservers(true);
        }
        return true;
    }

    public boolean setAnxietyLevelBefore(int level){
        if(intOutOfBounds(level)){
            return false;
        }

        anxietyLevelBefore = level;

        if(!intOutOfBounds(happinessLevelBefore)){
            setChanged();
            notifyObservers(false);
        }
        return true;
    }

    public boolean setAnxietyLevelAfter(int level){
        if(intOutOfBounds(level)){
            return false;
        }

        anxietyLevelAfter = level;

        if(!intOutOfBounds(happinessLevelAfter)){
            setChanged();
            notifyObservers(true);
        }
        return true;
    }

    private boolean intOutOfBounds(int level){
        return level < 0 | level > 10;
    }

    public static String getCSVHeadings(){
        StringBuilder sb = new StringBuilder();
        sb.append("Date,");
        sb.append("Day Number,");
        sb.append("Happiness Level Before,");
        sb.append("Happiness Level After,");
        sb.append("Anxiety Level Before,");
        sb.append("Anxiety Level After,");

        return new String(sb);
    }

    public String makeCSV(){
        StringBuilder sb = new StringBuilder();

        sb.append(dateFormat.format(date));
        sb.append(",");
        sb.append(dayNum);
        sb.append(",");
        if(!intOutOfBounds(happinessLevelBefore)) {
            sb.append(happinessLevelBefore);
        }
        sb.append(",");
        if(!intOutOfBounds(happinessLevelAfter)) {
            sb.append(happinessLevelAfter);
        }
        sb.append(",");

        if(!intOutOfBounds(anxietyLevelBefore)) {
            sb.append(anxietyLevelBefore);
        }
        sb.append(",");
        if(!intOutOfBounds(anxietyLevelAfter)) {
            sb.append(anxietyLevelAfter);
        }

        return new String(sb);
    }

    public int getHappinessLevelBefore() {
        return happinessLevelBefore;
    }

    public int getHappinessLevelAfter() {
        return happinessLevelAfter;
    }

    public int getAnxietyLevelBefore() {
        return anxietyLevelBefore;
    }

    public int getAnxietyLevelAfter() {
        return anxietyLevelAfter;
    }

    public int getDayNum() {
        return dayNum;
    }

    public boolean appUsed(){
        return !intOutOfBounds(happinessLevelBefore) && !intOutOfBounds(anxietyLevelBefore);
    }

    public boolean taskFinished(){
        return !intOutOfBounds(happinessLevelAfter) && !intOutOfBounds(anxietyLevelAfter);
    }
}
