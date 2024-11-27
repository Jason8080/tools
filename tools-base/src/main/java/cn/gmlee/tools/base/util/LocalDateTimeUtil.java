package cn.gmlee.tools.base.util;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjuster;
import java.util.Date;
import java.util.Objects;

/**
 * The type Local date time util.
 *
 * @author Jas °
 * @date 2020 /11/13 (周五)
 */
public class LocalDateTimeUtil {
    /**
     * 默认时区
     */
    private static final ZoneId zoneId = ZoneId.systemDefault();
    /**
     * 默认时区
     */
    private static final ZoneOffset utc = ZoneOffset.UTC;

    /**
     * Between duration.
     *
     * @param start the start
     * @param end   the end
     * @return the duration
     */
    public static Duration between(String start, String end) {
        if (BoolUtil.allNotNull(start, end)) {
            return between(TimeUtil.parseTime(start), TimeUtil.parseTime(end));
        }
        return Duration.ZERO;
    }

    /**
     * 时间差.
     *
     * @param start the d 1
     * @param end   the d 2
     * @return the duration
     */
    public static Duration between(LocalDateTime start, LocalDateTime end) {
        if (BoolUtil.allNotNull(start, end)) {
            return Duration.between(start, end);
        }
        return Duration.ZERO;
    }

    /**
     * 时间差.
     *
     * @param start the d 1
     * @param end   the d 2
     * @return the duration
     */
    public static Duration between(Date start, Date end) {
        LocalDateTime date1 = LocalDateTimeUtil.toLocalDateTime(start);
        LocalDateTime date2 = LocalDateTimeUtil.toLocalDateTime(end);
        if (BoolUtil.allNotNull(date1, date2)) {
            return Duration.between(date1, date2);
        }
        return Duration.ZERO;
    }

    /**
     * Date转换成LocalDateTime.
     *
     * @param date the date
     * @return the local date time
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        if (Objects.isNull(date)) {
            return null;
        }
        Instant instant = date.toInstant();
        return instant.atZone(zoneId).toLocalDateTime();
    }

    /**
     * LocalDateTime转换成Date.
     *
     * @param localDateTime the local date time
     * @return the date
     */
    public static Date toDate(LocalDateTime localDateTime) {
        if (Objects.isNull(localDateTime)) {
            return null;
        }
        ZonedDateTime zdt = localDateTime.atZone(zoneId);
        return Date.from(zdt.toInstant());
    }


    /**
     * Offset current long.
     *
     * @param millis the millis
     * @return the long
     */
    public static long offsetCurrent(long millis) {
        return System.currentTimeMillis() + millis;
    }

    /**
     * Offset date.
     *
     * @param now    the now
     * @param offset the offset
     * @param unit   the unit
     * @return the date
     */
    public static Date offset(LocalDateTime now, long offset, ChronoUnit unit) {
        if (now != null) {
            return toDate(now.plus(offset, unit));
        }
        return null;
    }

    /**
     * Plus local date time.
     *
     * @param now    the now
     * @param offset the offset
     * @param unit   the unit
     * @return the local date time
     */
    private static LocalDateTime plus(LocalDateTime now, long offset, ChronoUnit unit) {
        return now.plus(offset, unit);
    }

    /**
     * Offset date.
     *
     * @param date   the date
     * @param offset the offset
     * @param unit   the unit
     * @return the date
     */
    public static Date offset(Date date, long offset, ChronoUnit unit) {
        LocalDateTime now = toLocalDateTime(date);
        return offset(now, offset, unit);
    }

    /**
     * Offset current date.
     *
     * @param offset the offset
     * @param unit   the unit
     * @return the date
     */
    public static Date offsetCurrent(long offset, ChronoUnit unit) {
        LocalDateTime now = LocalDateTime.now();
        return offset(now, offset, unit);
    }

    /**
     * 获取指定日期的某一时刻.
     *
     * @param date      the date
     * @param localTime the local time
     * @return the date
     */
    public static Date moment(Date date, LocalTime localTime) {
        return moment(toLocalDateTime(date), localTime);
    }

    /**
     * 获取指定日期的某一时刻.
     *
     * @param date      指定日期
     * @param offset    位移指定天数
     * @param localTime the local time
     * @return the date
     */
    public static Date moment(Date date, long offset, LocalTime localTime) {
        return moment(toLocalDateTime(date), offset, localTime);
    }

    /**
     * 获取指定日期的某一时刻.
     *
     * @param date      the date
     * @param localTime the local date
     * @return the date
     */
    public static Date moment(LocalDateTime date, LocalTime localTime) {
        return toDate(LocalDateTime.of(date.toLocalDate(), localTime));
    }

    /**
     * 获取指定日期的某一时刻.
     *
     * @param date      指定时间
     * @param offset    位移指定天数
     * @param localTime the local time
     * @return the date
     */
    public static Date moment(LocalDateTime date, long offset, LocalTime localTime) {
        LocalDateTime plus = plus(date, offset, ChronoUnit.DAYS);
        return toDate(LocalDateTime.of(plus.toLocalDate(), localTime));
    }

    /**
     * 获取某天的某一时刻.
     * <p>
     * 昨天: offset = -1
     * 明天: offset = 1
     * </p>
     *
     * @param offset    位移指定天数
     * @param localTime 指定时刻
     * @return 时间 date
     */
    public static Date momentCurrent(int offset, LocalTime localTime) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime plus = plus(now, offset, ChronoUnit.DAYS);
        LocalDateTime date = LocalDateTime.of(plus.toLocalDate(), localTime);
        return toDate(date);
    }

    /**
     * 获取当天的某一时刻.
     *
     * @param localTime the local time
     * @return the date
     */
    public static Date momentCurrent(LocalTime localTime) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime date = LocalDateTime.of(now.toLocalDate(), localTime);
        return toDate(date);
    }

    /**
     * 获取某个高光时刻.
     *
     * @param temporalAdjuster the temporal adjuster
     * @return local date time
     */
    public static LocalDateTime momentCurrent(TemporalAdjuster temporalAdjuster) {
        return LocalDateTime.now().with(temporalAdjuster);
    }

    /**
     * 获取某个高光时刻.
     *
     * @param temporalAdjuster the temporal adjuster
     * @param localTime        the local time
     * @return local date time
     */
    public static LocalDateTime momentCurrent(TemporalAdjuster temporalAdjuster, LocalTime localTime) {
        LocalDateTime localDateTime = LocalDateTime.now().with(temporalAdjuster);
        return localDateTime.of(localDateTime.toLocalDate(), localTime);
    }

}
