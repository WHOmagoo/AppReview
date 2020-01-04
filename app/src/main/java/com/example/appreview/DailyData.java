package com.example.appreview;

import java.util.Date;

public class DailyData {
    private int happinessLevelBefore;
    private int happinessLevelAfter;
    private int anxietyLevelBefore;
    private int anxietyLevelAfter;
    private Date date;
    private int dayNum;
    
    public DailyData(Date date, int dayNum){
        this.date = date;
        this.dayNum = dayNum;
    }
    
    public boolean setHappinessLevelBefore(int level){
        if(intOutOfBounds(level)){
            return false;
        }
        
        happinessLevelBefore = level;
        return true;
    }

    public boolean setHappinessLevelAfter(int level){
        if(intOutOfBounds(level)){
            return false;
        }

        happinessLevelAfter = level;
        return true;
    }

    public boolean setAnxietyLevelBefore(int level){
        if(intOutOfBounds(level)){
            return false;
        }

        anxietyLevelBefore = level;
        return true;
    }

    public boolean setAnxietyLevelAfter(int level){
        if(intOutOfBounds(level)){
            return false;
        }

        anxietyLevelAfter = level;
        return true;
    }
    
    private boolean intOutOfBounds(int level){
        return level < 0 | level > 10;   
    }

    public String makeCsv(){
        StringBuilder sb = new StringBuilder();
        sb.append(date);
        sb.append(",");
        sb.append(dayNum);
        sb.append(",");
        sb.append(happinessLevelBefore);
        sb.append(",");
        sb.append(happinessLevelAfter);
        sb.append(",");
        sb.append(anxietyLevelBefore);
        sb.append(",");
        sb.append(anxietyLevelAfter);
        sb.append("\n");

        return new String(sb);
    }
}
