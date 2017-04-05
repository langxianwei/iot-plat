package com.point.iot.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    
    public static String getDateStr(long nTime) {
        String str = "";
        Date date1 = new Date(nTime);
        Date date2 = new Date(System.currentTimeMillis());

        // Integer.parseInt(
        SimpleDateFormat f1 = new SimpleDateFormat("MM");
        SimpleDateFormat f2 = new SimpleDateFormat("dd");
        SimpleDateFormat sf_year = new SimpleDateFormat("yyyy");
        SimpleDateFormat sf_hour = new SimpleDateFormat("HH");
        SimpleDateFormat sf_minute = new SimpleDateFormat("mm");
        SimpleDateFormat sf_second = new SimpleDateFormat("ss");
        int year1 = Integer.parseInt(sf_year.format(date1));
        int month1 = Integer.parseInt(f1.format(date1));
        int day1 = Integer.parseInt(f2.format(date1));
        int hour1 = Integer.parseInt(sf_hour.format(date1));
        int min1 = Integer.parseInt(sf_minute.format(date1));
        int sec1 = Integer.parseInt(sf_second.format(date1));

        String year = sf_year.format(date1);
        String month = f1.format(date1);
        String day = f2.format(date1);
        String hour = sf_hour.format(date1);
        String minute = sf_minute.format(date1);
        String second = sf_second.format(date1);

        int year2 = Integer.parseInt(sf_year.format(date2));
        int month2 = Integer.parseInt(f1.format(date2));
        int day2 = Integer.parseInt(f2.format(date2));
        int hour2 = Integer.parseInt(sf_hour.format(date2));
        int min2 = Integer.parseInt(sf_minute.format(date2));
        int sec2 = Integer.parseInt(sf_second.format(date2));

        if (year1 != year2) {
            str += year;
            str += "年";
            str += month;
            str += "月";

        } else if (month1 != month2) {
            str += month;
            str += "月";
            str += day;
            str += "日";
        } else if (day1 != day2) {
            if ((day2 - day1) == 1) {
                str += "昨天";
                str += hour;
                str += ":";
                str += minute;
            } else {
                str += month;
                str += "月";
                str += day;
                str += "日";
            }
        } else {
            str += hour;
            str += ":";
            str += minute;
        }
        return str;
    }
    

}
