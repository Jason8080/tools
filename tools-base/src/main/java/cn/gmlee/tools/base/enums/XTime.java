package cn.gmlee.tools.base.enums;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;

/**
 * 时间格式
 *
 * @author Jas °
 */
public enum XTime {
    // -----------------------------------------------------------------------------------------------------------------
    YEAR_MINUS("yyyy"),
    MONTH_MINUS("yyyy-MM"),
    MONTH_SLASH("yyyy/MM"),
    MONTH_DOUBLE_SLASH("yyyy\\MM"),
    MONTH_NONE("yyyyMM"),
    DAY_MINUS("yyyy-MM-dd"),
    DAY_SLASH("yyyy/MM/dd"),
    DAY_DOUBLE_SLASH("yyyy\\MM\\dd"),
    DAY_NONE("yyyyMMdd"),
    HOUR_MINUTE_SECOND("HH:mm:ss"),
    HOUR_MINUS_BLANK("yyyy-MM-dd HH"),
    HOUR_SLASH_BLANK("yyyy/MM/dd HH"),
    HOUR_DOUBLE_SLASH_BLANK("yyyy\\MM\\dd HH"),
    HOUR_NONE("yyyyMMddHH"),
    HOUR_BLANK("yyyyMMdd HH"),
    MINUTE_MINUS_BLANK_COLON("yyyy-MM-dd HH:mm"),
    MINUTE_SLASH_BLANK_COLON("yyyy/MM/dd HH:mm"),
    MINUTE_DOUBLE_SLASH_BLANK_COLON("yyyy\\MM\\dd HH:mm"),
    MINUTE_NONE("yyyyMMddHHmm"),
    MINUTE_BLANK("yyyyMMdd HHmm"),
    SECOND_MINUS_BLANK_COLON("yyyy-MM-dd HH:mm:ss"),
    SECOND_SLASH_BLANK_COLON("yyyy/MM/dd HH:mm:ss"),
    SECOND_DOUBLE_SLASH_BLANK_COLON("yyyy\\MM\\dd HH:mm:ss"),
    SECOND_NONE("yyyyMMddHHmmss"),
    SECOND_BLANK("yyyyMMdd HHmmss"),
    MS_NONE("yyyyMMddHHmmssSSS"),
    MS_MINUS_BLANK_COLON_DOT("yyyy-MM-dd HH:mm:ss.SSS"),
    MS_SLASH_BLANK_COLON_DOT("yyyy/MM/dd HH:mm:ss.SSS"),
    MS_DOUBLE_SLASH_BLANK_COLON_DOT("yyyy\\MM\\dd HH:mm:ss.SSS"),
    MS_BLANK_COLON_DOT("yyyyMMdd HH:mm:ss.SSS"),
    // -----------------------------------------------------------------------------------------------------------------
    MS_TZ("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
    MS_T("yyyy-MM-dd'T'HH:mm:ss.SSS"),
    SECOND_TZ("yyyy-MM-dd'T'HH:mm:ss'Z'"),
    SECOND_T("yyyy-MM-dd'T'HH:mm:ss"),
    SECOND_EEE_Z("EEE MMM dd HH:mm:ss z yyyy"),
    MS_EEE_Z("EEE MMM dd HH:mm:ss.SSS z yyyy"),
    // -----------------------------------------------------------------------------------------------------------------
    ;

    public final transient DateTimeFormatter timeFormat;
    public final transient SimpleDateFormat dateFormat;
    public final transient String pattern;

    XTime(String pattern) {
        this.pattern = pattern;
        this.timeFormat = DateTimeFormatter.ofPattern(pattern);
        this.dateFormat = new SimpleDateFormat(pattern);
    }
}
