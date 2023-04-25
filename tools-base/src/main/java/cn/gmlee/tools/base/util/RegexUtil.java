package cn.gmlee.tools.base.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通用正则表达式匹配工具
 *
 * @author Jas °
 * @date 2021 /4/28 (周三)
 */
public class RegexUtil {
    /**
     * 查找出符合条件的集合
     *
     * @param source the source
     * @param regex  the regex
     * @return list list
     */
    public static List<String> find(String source, String regex) {
        List<String> result = new ArrayList();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(source);
        while (matcher.find()) {
            result.add(matcher.group());
        }
        return result;
    }

    /**
     * First string.
     *
     * @param source the source
     * @param regex  the regex
     * @return the string
     */
    public static String first(String source, String regex) {
        return first(source, regex, false);
    }

    /**
     * First string.
     *
     * @param source  the source
     * @param regex   the regex
     * @param allowEx the allow ex
     * @return the string
     */
    public static String first(String source, String regex, boolean allowEx) {
        List<String> list = find(source, regex);
        if (BoolUtil.notEmpty(list)) {
            return list.get(0);
        }
        if (allowEx) {
            return ExceptionUtil.cast(String.format("没有匹配到内容"));
        }
        return null;
    }

    /**
     * 判断字符串{source}是否符合{regex}正则规格
     * <p>
     * 内容是空一律返回 false(不匹配);
     * 规则是空一律返回 true(匹配);
     * </p>
     *
     * @param source 判断字符串
     * @param regex  目标正则表达式字符串
     * @return true 符合 false  不符合
     */
    public static boolean match(String source, String regex) {
        if (BoolUtil.isEmpty(source)) {
            return false;
        }
        if (BoolUtil.notEmpty(regex)) {
            return Pattern.matches(regex, source);
        }
        return true;
    }

    /**
     * Match boolean.
     *
     * @param source  the source
     * @param regexes the regexes
     * @return the boolean
     */
    public static boolean matchMoreRegex(String source, String... regexes) {
        if (BoolUtil.notEmpty(regexes)) {
            for (String regex : regexes) {
                boolean match = match(source, regex);
                if (match) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    /**
     * Match more source boolean.
     *
     * @param regex   the regex
     * @param sources the sources
     * @return the boolean
     */
    public static boolean matchMoreSource(String regex, String... sources) {
        if (BoolUtil.notEmpty(sources)) {
            for (String source : sources) {
                boolean match = match(source, regex);
                if (!match) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
