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
    public static String QQFormatTime(long time) {
        Date date = new Date();
        date.setTime(time);
        //同一年 显示MM-dd HH:mm
        if (isSameYear(date)) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
            //同一天 显示HH:mm
            if (isSameDay(date)) {
                int minute = minutesAgo(time);
                //1小时之内 显示n分钟前
                if (minute < 60) {
                    //一分钟之内，显示刚刚
                    if (minute <= 1) {
                        return "刚刚";
                    } else {
                        return minute + "分钟前";
                    }
                } else {
                    return simpleDateFormat.format(date);
                }
            } else {
                //昨天，显示昨天+HH:mm
                if (isYesterday(date)) {
                    return "昨天 " + simpleDateFormat.format(date);
                }
                //本周,显示周几+HH:mm
                else if (isSameWeek(date)) {
                    String weekday = null;
                    if (date.getDay() == 1) {
                        weekday = "周一";
                    }
                    if (date.getDay() == 2) {
                        weekday = "周二";
                    }
                    if (date.getDay() == 3) {
                        weekday = "周三";
                    }
                    if (date.getDay() == 4) {
                        weekday = "周四";
                    }
                    if (date.getDay() == 5) {
                        weekday = "周五";
                    }
                    if (date.getDay() == 6) {
                        weekday = "周六";
                    }
                    if (date.getDay() == 0) {
                        weekday = "周日";
                    }
                    return weekday + " " + simpleDateFormat.format(date);
                } else {//同一年 显示MM-dd HH:mm
                    SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日", Locale.CHINA);
                    return sdf.format(date);
                }
            }
        } else {//不是同一年 显示完整日期yyyy-MM-dd HH:mm
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);
            return sdf.format(date);
        }
    }

    /**
     * 几分钟前
     *
     * @param time
     * @return
     */
    public static int minutesAgo(long time) {
        return (int) ((System.currentTimeMillis() - time) / (1000 * 60));
    }

    /**
     * 与当前时间是否在同一周
     * 先判断是否在同一年，然后根据Calendar.DAY_OF_YEAR判断所得的周数是否一致
     *
     * @param date
     * @return
     */
    public static boolean isSameWeek(Date date) {
        if (isSameYear(date)) {
            Calendar ca = Calendar.getInstance();
            //西方周日为一周的第一天，咱得将周一设为一周第一天
            ca.setFirstDayOfWeek(Calendar.MONDAY);
            ca.setTime(date);
            Calendar caNow = Calendar.getInstance();
            caNow.setFirstDayOfWeek(Calendar.MONDAY);
            caNow.setTime(new Date());
            if (ca.get(Calendar.WEEK_OF_YEAR) == caNow.get(Calendar.WEEK_OF_YEAR)) {
                return true;
            }
        } else {
            return false;
        }
        return false;
    }

    /**
     * 是否是当前时间的昨天
     * 获取指定时间的后一天的日期，判断与当前日期是否是同一天
     *
     * @param date
     * @return
     */
    public static boolean isYesterday(Date date) {
        Date yesterday = getNextDay(date, 1);
        return isSameDay(yesterday);
    }

    /**
     * 判断与当前日期是否是同一天
     *
     * @param date
     * @return
     */
    public static boolean isSameDay(Date date) {
        return isEquals(date, "yyyy-MM-dd");
    }

    /**
     * 判断与当前日期是否是同一月
     *
     * @param date
     * @return
     */
    public static boolean isSameMonth(Date date) {
        return isEquals(date, "yyyy-MM");
    }

    /**
     * 判断与当前日期是否是同一年
     *
     * @param date
     * @return
     */
    public static boolean isSameYear(Date date) {
        return isEquals(date, "yyyy");
    }


    /**
     * 格式化Date，判断是否相等
     *
     * @param date
     * @return 是返回true，不是返回false
     */
    private static boolean isEquals(Date date, String format) {
        //当前时间
        Date now = new Date();
        SimpleDateFormat sf = new SimpleDateFormat(format);
        //获取今天的日期
        String nowDay = sf.format(now);
        //对比的时间
        String day = sf.format(date);
        return day.equals(nowDay);
    }

    /**
     * 获取某个date第n天的日期
     * n<0 表示前n天
     * n=0 表示当天
     * n>1 表示后n天
     *
     * @param date
     * @param n
     * @return
     */
    public static Date getNextDay(Date date, int n) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, n);
        date = calendar.getTime();
        return date;
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
