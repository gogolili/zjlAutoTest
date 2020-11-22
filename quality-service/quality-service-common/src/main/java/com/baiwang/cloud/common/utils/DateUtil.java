package com.baiwang.cloud.common.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {
    public static String  currentDate(){
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(System.currentTimeMillis());
        System.out.println(formatter.format(date));
        return formatter.format(date);
    }
    public static String  currentYear(){
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy");
        Date date = new Date(System.currentTimeMillis());
        System.out.println(formatter.format(date));
        return formatter.format(date);
    }

    public static String  currentMonth(){
        SimpleDateFormat formatter= new SimpleDateFormat("MM");
        Date date = new Date(System.currentTimeMillis());
        System.out.println(formatter.format(date));
        return formatter.format(date);
    }

    public static String  currentTime(){
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        System.out.println(formatter.format(date));
        return formatter.format(date);
    }




    public static void main(String[] args) {
        DateUtil.currentDate();
        DateUtil.currentYear();
        DateUtil.currentMonth();
        DateUtil.currentTime();
}
}
