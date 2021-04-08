package com.ilesson.ppim.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Dateuitls {
    public static String getDiffTime(long start,long end){
        long diff = end - start;//这样得到的差值是毫秒级别
        long hours = diff / (1000 * 60 * 60 );
        long yuhours = diff%(1000 * 60 * 60 );
        long yuminutes = diff%(1000 * 60);
        long minutes = yuhours/(1000* 60);
        long sec = yuminutes/1000;
        StringBuilder stringBuilder = new StringBuilder();
        if(hours>0){
            stringBuilder.append(hours+"小时");
        }
        if(minutes>0){
            stringBuilder.append(minutes+"分");
        }
        if (sec>0){
            stringBuilder.append(sec+"秒");
        }
        return stringBuilder.toString();
    }

    public static String getFormatTime(long time){
        SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日 HH:mm");
        return sdf.format(time);
    }
    public static String getFormatOrderTime(long time){//"yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = sdf.format(new Date(time));
        return date;
    }
    public static String getFormatScoreDetailTime(String time){//"yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd   HH:mm");
        String date = sdf.format(Long.valueOf(time));
        return date;
    }
    public static String getOrderPayTime(String var){
        StringBuilder stringBuilder = new StringBuilder();
        for(int i=0;i<var.length();i++){
            stringBuilder.append(var.charAt(i));
            if(i==3||i==5){
                stringBuilder.append("-");
            }else if(i==7){
                stringBuilder.append(" ");
            }else if(i==9||i==11){
                stringBuilder.append(":");
            }
        }
        return stringBuilder.toString();
    }
    public static String formatSeconds(long seconds){
        String standardTime;
        if (seconds <= 0){
            standardTime = "00:00";
        } else if (seconds < 60) {
            standardTime = String.format(Locale.getDefault(), "00:%02d", seconds % 60);
        } else if (seconds < 3600) {
            standardTime = String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60);
        } else {
            standardTime = String.format(Locale.getDefault(), "%02d:%02d:%02d", seconds / 3600, seconds % 3600 / 60, seconds % 60);
        }
        return standardTime;
    }

    public static String getFormatSendTime(long timesamp) {
        String result = "";
        Calendar todayCalendar = Calendar.getInstance();
        Calendar otherCalendar = Calendar.getInstance();
        otherCalendar.setTimeInMillis(timesamp);

        SimpleDateFormat yearTimeFormat = new SimpleDateFormat("yyyy年M月d");
        int todayMonth=todayCalendar.get(Calendar.MONTH);
        int otherMonth=otherCalendar.get(Calendar.MONTH);
        if(todayMonth==otherMonth){
            int days=todayCalendar.get(Calendar.DATE)-otherCalendar.get(Calendar.DATE);
            switch (days) {
                case 0:
                    result = "今天";
                    break;
                case 1:
                    result = "昨天 ";
                    break;
                case 2:
                    result = "前天 ";
                    break;

                default:
                    result = days+"天前";
                    break;
            }
        }else{
            result = yearTimeFormat.format(new Date(timesamp));
        }
        return result;
    }
}
