package cn.gmlee.tools.base.util;

import cn.gmlee.tools.base.enums.Int;
import cn.gmlee.tools.base.enums.XTime;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TimeUtil
 *
 * @author: Jas °
 */
public class TimeUtil {

    private TimeUtil() {
        // no construct function
    }

    /**
     * String 转时间
     *
     * @param timeStr the time str
     * @return local date time
     */
    public static Date parseTime(String timeStr) {
        return parseTime(timeStr, true);
    }

    /**
     * Parse time date.
     *
     * @param timeStr the time str
     * @param allowEx the allow ex
     * @return the date
     */
    public static Date parseTime(String timeStr, boolean allowEx) {
        if (BoolUtil.isEmpty(timeStr)) {
            return null;
        }
        XTime[] values = XTime.values();
        // 记录匹配失败的时间格式
        List<String> fail = new ArrayList();
        // 最先匹配长度相符的格式
        List<XTime> lenAll = Arrays.stream(values).filter(x -> x.pattern.length() == timeStr.length()).collect(Collectors.toList());
        for (XTime value : lenAll) {
            try {
                return value.dateFormat.parse(timeStr);
            } catch (ParseException e) {
                fail.add(value.pattern);
                continue;
            }
        }
        // 优先匹配最详细的时间格式
        List<XTime> sortAll = Arrays.stream(values)
                // 过滤已经失败的时间格式
                .filter(x -> !fail.contains(x.pattern))
                .sorted(Comparator.comparing((XTime x) -> x.pattern.length()).reversed())
                .collect(Collectors.toList());
        for (XTime value : sortAll) {
            try {
                return value.dateFormat.parse(timeStr);
            } catch (ParseException e) {
                fail.add(value.pattern);
                continue;
            }
        }
        if (allowEx) {
            ExceptionUtil.cast(String.format("不支持的日期格式: %s", timeStr));
        }
        return null;
    }

    /**
     * String 转时间
     *
     * @param timeStr the time str
     * @param format  时间格式
     * @return local date time
     */
    public static Date parseTime(String timeStr, XTime format) {
        return parseTime(timeStr, format, true);
    }

    /**
     * Parse time date.
     *
     * @param timeStr the time str
     * @param format  the format
     * @param allowEx the allow ex
     * @return the date
     */
    public static Date parseTime(String timeStr, XTime format, boolean allowEx) {
        try {
            return format.dateFormat.parse(timeStr);
        } catch (Exception e) {
            if (allowEx) {
                return ExceptionUtil.cast(String.format("日期格式有误: %s", timeStr));
            }
        }
        return new Date();
    }

    /**
     * 时间转 String
     *
     * @param time the time
     * @return string string
     */
    public static String parseTime(LocalDateTime time) {
        return XTime.SECOND_MINUS_BLANK_COLON.timeFormat.format(time);
    }

    /**
     * 时间转 String
     *
     * @param time   the time
     * @param format 时间格式
     * @return string string
     */
    public static String parseTime(LocalDateTime time, XTime format) {
        return format.timeFormat.format(time);
    }

    /**
     * 获取当前时间
     *
     * @return current datetime
     */
    public static String getCurrentDatetime() {
        return XTime.SECOND_MINUS_BLANK_COLON.timeFormat.format(LocalDateTime.now());
    }

    /**
     * 获取当前时间
     *
     * @param XTime 时间格式
     * @return current datetime
     */
    public static String getCurrentDatetime(XTime XTime) {
        return XTime.timeFormat.format(LocalDateTime.now());
    }

    /**
     * Gets current date.
     *
     * @return the current date
     */
    public static Date getCurrentDate() {
        return new Date();
    }

    /**
     * Gets current timestamp.
     *
     * @return the current timestamp
     */
    public static Long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * Gets current timestamp ms.
     *
     * @return the current timestamp ms
     */
    public static String getCurrentMs() {
        return getCurrentTimestamp().toString();
    }

    /**
     * Gets current timestamp second.
     *
     * @return the current timestamp second
     */
    public static Long getCurrentTimestampSecond() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * Gets current timestamp year.
     *
     * @return the current timestamp year
     */
    public static Long getCurrentTimestampYear() {
        return BigDecimalUtil.divide(System.currentTimeMillis(), 1000, 3600, 24, 365).longValue();
    }

    /**
     * Gets current second.
     *
     * @return the current second
     */
    public static String getCurrentSecond() {
        String ms = getCurrentMs();
        return ms.substring(0, ms.length() - Int.THREE);
    }

    /**
     * Gets current second.
     *
     * @param date the date
     * @return the current second
     */
    public static Long getTimestampSecond(Date date) {
        if (date != null) {
            return date.getTime() / 1000;
        }
        return 0L;
    }

    /**
     * 格式化时间
     *
     * @param date  the date
     * @param XTime the x time
     * @return string string
     */
    public static String format(Date date, XTime XTime) {
        LocalDateTime localDateTime = LocalDateTimeUtil.toLocalDateTime(date);
        return XTime.timeFormat.format(localDateTime);
    }

    /**
     * 格式化时间.
     *
     * @param localDateTime the local date time
     * @param XTime         the x time
     * @return the string
     */
    public static String format(LocalDateTime localDateTime, XTime XTime) {
        return XTime.timeFormat.format(localDateTime);
    }
}
