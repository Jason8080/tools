package cn.gmlee.tools.base.enums;

import java.util.regex.Pattern;

/**
 * 正则匹配查找应用场景枚举
 *
 * @author Jas°
 * @date 2021/4/6 (周二)
 */
public enum Regex {
    // 数字
    NUMBER("-?\\d+(?:\\.\\d+)?"),
    // 数字 (支持千位分隔符)
    NUMBER_THOUSANDS_SEPARATOR("-?\\d+(?:,\\d{3})*(?:\\.\\d+)?"),
    // 手机
    MOBILE("(13[0-9]|14[5|7]|15[0|1|2|3|4|5|6|7|8|9]|18[0|1|2|3|5|6|7|8|9])\\d{8}"),
    // 邮箱
    MAIL("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*"),
    // 密码: 至少包含大写、小写、数字、特殊符号
    PASSWORD("^(?![A-z0-9]+$)(?=.[^%&',;=?$\\x22])(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9]).{6,20}"),
    // 用户名: 字母开头的 字母、数字、_、组成
    USERNAME("[A-Za-z][A-Za-z0-9_.]{0,19}"),

    // 待验证
    MYSQL_WHERE("WHERE(\\s+)(\\w+)(\\s+)(=|IS|!=|OR|NOT|BETWEEN|IN)(\\s+)(\\(?)(\\s+)(\\w+)(\\s+)(\\)?)((end)(\\s+)(\\w+))?"),
    // 待验证
    MYSQL_SET("SET(\\s+)(\\w+)(\\s+)(=)(\\s+)(\\(?)(\\s+)(\\w+)(\\s+)(\\)?)"),

    // 待验证
    HTTP_SERVER_ADDR("((http|https)://)(([a-zA-Z0-9._-]+)|([0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}))(([a-zA-Z]{2,6})|(:[0-9]{1,4})?)"),
    ;

    public static final String first = "^";
    public static final String last = "$";

    public final String regex;
    public final Pattern pattern;
    public final Pattern patternIgnoreCase;

    Regex(String regex) {
        this.regex = regex;
        this.pattern = Pattern.compile(regex);
        this.patternIgnoreCase = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }
}
