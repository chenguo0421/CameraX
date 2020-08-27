package com.cg.cgcamerax.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * @ProjectName: NVMS_3.0
 * @CreateDate: 2020/7/7 15:44
 * @Author: ChenGuo
 * @Description: java类作用描述
 * @Version: 1.0
 */
public class DataUtils {
    public static final String TAG = "DataUtils";


    public final static String getCurrentDate(String strLink) {
        TimeZone defaultZone = TimeZone.getDefault();
        Calendar calendar = Calendar.getInstance(defaultZone);
        calendar.setTime(new Date());
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DATE);
        StringBuffer sb = new StringBuffer();
        sb.append(year).append(strLink);
        sb.append(month < 10 ? "0" : "").append(month).append(strLink);
        sb.append(day < 10 ? "0" : "").append(day);
        return sb.toString();
    }

    public final static String getCurrentTime(String strLink) {
        TimeZone defaultZone = TimeZone.getDefault();
        Calendar calendar = Calendar.getInstance(defaultZone);
        calendar.setTime(new Date());
        int hour = calendar.get(Calendar.HOUR);
        int min = calendar.get(Calendar.MINUTE);
        int sec = calendar.get(Calendar.SECOND);
        int pm = calendar.get(Calendar.AM_PM);
        hour = pm == 1 ? hour + 12 : hour;
        StringBuffer sb = new StringBuffer();
        sb.append(hour < 10 ? "0" : "").append(hour).append(strLink);
        sb.append(min < 10 ? "0" : "").append(min).append(strLink);
        sb.append(sec < 10 ? "0" : "").append(sec);
        return sb.toString();
    }

    public static String getTimestamp(String strLink){
        return getCurrentDate(strLink) + strLink + getCurrentTime(strLink);
    }
}
