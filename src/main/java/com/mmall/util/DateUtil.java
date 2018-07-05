package com.mmall.util;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

/**
 * Created by 10353 on 2018/1/5.
 */
public class DateUtil {

    private static final String STANDARD_FOMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 自定义日期转字符串
     * @param date
     * @param fomatStr
     * @return
     */
    public static String dateToString(Date date, String fomatStr){
         if(date == null)
             return StringUtils.EMPTY;
        DateTime dateTime = new DateTime(date);
        return dateTime.toString(fomatStr);
    }

    /**
     * 标准型日期转字符串
     * @param data
     * @return
     */
    public static String dateToString(Date data){
        return dateToString(data, STANDARD_FOMAT);
    }

    /**
     * 自定义字符串转日期
     * @param dateStr
     * @param fomatStr
     * @return
     */
    public static Date stringToDate(String dateStr, String fomatStr){
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(fomatStr);
        DateTime dateTime = dateTimeFormatter.parseDateTime(dateStr);
        return dateTime.toDate();
    }

    /**
     * 标准字符串转日期
     * @param dateStr
     * @return
     */
    public static Date stringToDate(String dateStr){
        return stringToDate(dateStr, STANDARD_FOMAT);
    }

    public static void main(String[] args) {
        System.out.println(DateUtil.dateToString(new Date(),"yyyy-MM-dd HH:mm:ss"));
        System.out.println(DateUtil.stringToDate("2010-01-01 11:11:11","yyyy-MM-dd HH:mm:ss"));

    }
}
