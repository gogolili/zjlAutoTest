/*
 * @项目名称: Moirai-service
 * @文件名称: DateTimeUtils.java
 * @Date: 17-9-22 下午12:24
 * @author Lance cui
 *
 */

package com.baiwang.moirai.utils;

import com.baiwang.cloud.common.enumutil.ErrorType;
import com.baiwang.cloud.common.model.ErrorMessage;
import com.baiwang.moirai.common.WebContext;
import com.baiwang.moirai.enumutil.MoiraiErrorEnum;
import com.baiwang.moirai.es.ElasticSearchConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class DateTimeUtils {

    private static final Logger logger = LoggerFactory.getLogger(ElasticSearchConfig.class);

    /**
     * yyyyMMddHHmmssSSS 格式
     */
    public static final String DEFAULT_DATE_MILLISECOND_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss SSS";

    public static final String DEFAULT_DATE_MILLISECOND_PATTERN = "yyyyMMddHHmmssSSS";

    public static final String DEFAULT_DATE_MINUTE_FORMAT_PATTERN = "yyyyMMddHHmm";

    public static final String DEFAULT_DATE_SECOND_FORMAT_PATTERN = "yyyyMMddHHmmss";

    public static final String DEFAULT_DATE_DATE_FORMAT_PATTERN = "yyyyMMdd";

    public static final String DEFAULT_DATE_HOUR_FORMAT_PATTERN = "yyyyMMddHH";

    /**
     * 获取当前系统时间
     *
     * @return DateTime 当前系统时间
     */
    public static String nowTimeString() {
        Calendar calendar = Calendar.getInstance();
        String nowTimeString = toDateTimeString(DEFAULT_DATE_MILLISECOND_PATTERN,calendar);
        return nowTimeString;
    }

    /**
     * 获取当前系统时间 返回long类型
     *
     * @return DateTime 当前系统时间
     */
    public static Long nowTimeLong() {
        Calendar calendar = Calendar.getInstance();
        String nowTimeString = toDateTimeString(DEFAULT_DATE_MILLISECOND_PATTERN,calendar);
        return Long.parseLong(nowTimeString);
    }



    /**
     * 获取当前系统时间
     *
     * @return DateTime 当前系统时间
     */
    public static String nowTimeFormatString() {
        Calendar calendar = Calendar.getInstance();
        String nowTimeString = toDateTimeString(DEFAULT_DATE_MILLISECOND_FORMAT_PATTERN,calendar);
        return nowTimeString;
    }

    /**
     * 获取当前系统时间
     *
     * @return DateTime 当前系统时间
     */
    public static String nowMinuteString() {
        Calendar calendar = Calendar.getInstance();
        String nowTimeString = toDateTimeString(DEFAULT_DATE_MINUTE_FORMAT_PATTERN,calendar);
        return nowTimeString;
    }

    /**
     * 获取当前系统时间
     *
     * @return DateTime 当前系统时间
     */
    public static String nowDateString() {
        Calendar calendar = Calendar.getInstance();
        String nowTimeString = toDateTimeString(DEFAULT_DATE_DATE_FORMAT_PATTERN,calendar);
        return nowTimeString;
    }

    /**
     * 获取当前系统时间
     *
     * @return DateTime 当前系统时间
     */
    public static String nowIntegerHourString() {
        Calendar calendar = Calendar.getInstance();
        String nowHourString = toDateTimeString(DEFAULT_DATE_HOUR_FORMAT_PATTERN,calendar);
        nowHourString = nowHourString + "0000000";
        return nowHourString;
    }

    /**
     * 获取当前系统时间
     *
     * @return DateTime 当前系统时间
     */
    public static String nowSecondString() {
        Calendar calendar = Calendar.getInstance();
        String nowTimeString = toDateTimeString(DEFAULT_DATE_SECOND_FORMAT_PATTERN,calendar);
        return nowTimeString;
    }

    public static boolean isInTime(String endTime) {
        SimpleDateFormat fmt = (SimpleDateFormat) DateFormat.getDateInstance();
        fmt.applyPattern(DEFAULT_DATE_MILLISECOND_PATTERN);
        Date endTimeFmt = null;
        boolean isOver = false;
        try {
            endTimeFmt = fmt.parse(endTime);
            Date nowTime = new Date();
            isOver = endTimeFmt.getTime() < nowTime.getTime();
        } catch (ParseException e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_DATE_GENERATE_ERROR;
            logger.error(new ErrorMessage(requestURI,errorEnum.getCode(),errorEnum.getMsg() + ":isInTime", ErrorType.CustomerError).toString(),e);

        }
        return isOver;
    }

    /**n
     * 获取当前系统时间
     *
     * @return DateTime 当前系统时间
     */
    public static String getNext20Minutes(String nowTime) {

        String nextTimeString = null;
        try {
            SimpleDateFormat fmt = (SimpleDateFormat) DateFormat.getDateInstance();
            fmt.applyPattern(DEFAULT_DATE_MILLISECOND_PATTERN);
            Date nowTimeFmt = fmt.parse(nowTime);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(nowTimeFmt);
            calendar.add(Calendar.MINUTE,20);

            nextTimeString = fmt.format(calendar.getTime());
        } catch (ParseException e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_DATE_GENERATE_ERROR;
            logger.error(new ErrorMessage(requestURI,errorEnum.getCode(),errorEnum.getMsg() + ":getNext20Minutes", ErrorType.CustomerError).toString(),e);

        }

        return nextTimeString;
    }
    /**n
     * 获取当前系统时间
     *
     * @return DateTime 当前系统时间
     */

    public static String getBefore20Minutes(String nowTime) {

        String nextTimeString = null;
        try {
            SimpleDateFormat fmt = (SimpleDateFormat) DateFormat.getDateInstance();
            fmt.applyPattern(DEFAULT_DATE_MILLISECOND_PATTERN);
            Date nowTimeFmt = fmt.parse(nowTime);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(nowTimeFmt);
            calendar.add(Calendar.MINUTE,-20);

            nextTimeString = fmt.format(calendar.getTime());
        } catch (ParseException e) {
            String requestURI = WebContext.getRequest().getRequestURI();
            MoiraiErrorEnum errorEnum = MoiraiErrorEnum.MOIRAI_DATE_GENERATE_ERROR;
            logger.error(new ErrorMessage(requestURI,errorEnum.getCode(),errorEnum.getMsg() + ":getBefore20Minutes", ErrorType.CustomerError).toString(),e);
        }
        return nextTimeString;
    }

    /**
     * 使用日期转换pattern <p>pattern的用法参见java.text.SimpleDateFormat</p>
     *
     * @param pattern 日期解析规则
     * @return 按规则转换后的日期时间字符串
     */
    public static String toDateTimeString(String pattern,Calendar calendar) {
        SimpleDateFormat fmt = (SimpleDateFormat) DateFormat.getDateInstance();
        fmt.applyPattern(pattern);
        return fmt.format(calendar.getTime());
    }

    /**
     * 使用日期转换pattern <p>pattern的用法参见java.text.SimpleDateFormat</p>
     *
     * @param pattern 日期解析规则
     * @return 按规则转换后的日期时间字符串
     */

    public static String toDateTimeString(String pattern,String calendar) {
        SimpleDateFormat fmt =
                (SimpleDateFormat) DateFormat.getDateInstance(Integer.parseInt(calendar));
        fmt.applyPattern(pattern);
        return fmt.toString();
    }
}
