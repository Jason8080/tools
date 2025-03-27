package cn.gmlee.tools.base.util;

import java.util.*;

/**
 * 驼峰命名工具类.
 *
 * @author Jas °
 * @date 2021 /11/3 (周三)
 */
public class HumpUtil {
    private static final String MINUS = "-";
    private static final String UNDERLINE = "_";


    /**
     * Underline 2 hump list.
     *
     * @param strings the strings
     * @return the list
     */
    public static List<String> underline2hump(Collection<String> strings) {
        if (BoolUtil.notEmpty(strings)) {
            List<String> list = new ArrayList(strings.size());
            for (String content : strings) {
                list.add(underline2hump(content));
            }
            return list;
        }
        return Collections.emptyList();
    }

    /**
     * Minus 2 hump list.
     *
     * @param strings the strings
     * @return the list
     */
    public static List<String> minus2hump(Collection<String> strings) {
        if (BoolUtil.notEmpty(strings)) {
            List<String> list = new ArrayList(strings.size());
            for (String content : strings) {
                list.add(minus2hump(content));
            }
            return list;
        }
        return Collections.emptyList();
    }

    /**
     * Underline 2 hump list.
     *
     * @param strings the strings
     * @return the list
     */
    public static List<String> underline2hump(String... strings) {
        return underline2hump(Arrays.asList(strings));
    }

    /**
     * Minus 2 hump list.
     *
     * @param strings the strings
     * @return the list
     */
    public static List<String> minus2hump(String... strings) {
        return minus2hump(Arrays.asList(strings));
    }

    /**
     * Hump 2 underline list.
     *
     * @param strings the strings
     * @return the list
     */
    public static List<String> hump2underline(Collection<String> strings) {
        if (BoolUtil.notEmpty(strings)) {
            List<String> list = new ArrayList(strings.size());
            for (String content : strings) {
                list.add(hump2underline(content));
            }
            return list;
        }
        return Collections.emptyList();
    }

    /**
     * Hump 2 minus list.
     *
     * @param strings the strings
     * @return the list
     */
    public static List<String> hump2minus(Collection<String> strings) {
        if (BoolUtil.notEmpty(strings)) {
            List<String> list = new ArrayList(strings.size());
            for (String content : strings) {
                list.add(hump2minus(content));
            }
            return list;
        }
        return Collections.emptyList();
    }

    /**
     * Hump 2 underline list.
     *
     * @param strings the strings
     * @return the list
     */
    public static List<String> hump2underline(String... strings) {
        return hump2underline(Arrays.asList(strings));
    }


    /**
     * Hump 2 minus list.
     *
     * @param strings the strings
     * @return the list
     */
    public static List<String> hump2minus(String... strings) {
        return hump2minus(Arrays.asList(strings));
    }

    /***
     * 下划线命名转为驼峰命名.
     *
     * @param content 下划线命名的字符串
     * @return the string
     */
    public static String underline2hump(String content) {
        StringBuilder result = new StringBuilder();
        String[] array = content.split(UNDERLINE);
        for (String initial : array) {
            if (!content.contains(UNDERLINE)) {
                result.append(initial);
                continue;
            }
            if (result.length() == 0) {
                result.append(initial.toLowerCase());
            } else {
                result.append(initial.substring(0, 1).toUpperCase());
                result.append(initial.substring(1).toLowerCase());
            }
        }
        return result.toString();
    }

    /**
     * Minus 2 hump string.
     *
     * @param content the content
     * @return the string
     */
    public static String minus2hump(String content) {
        StringBuilder result = new StringBuilder();
        String[] array = content.split(MINUS);
        for (String initial : array) {
            if (!content.contains(MINUS)) {
                result.append(initial);
                continue;
            }
            if (result.length() == 0) {
                result.append(initial.toLowerCase());
            } else {
                result.append(initial.substring(0, 1).toUpperCase());
                result.append(initial.substring(1).toLowerCase());
            }
        }
        return result.toString();
    }

    /***
     * 驼峰命名转为下划线命名.
     * <p>
     *     下划线命名即数据库命名, 会小写
     * </p>
     *
     * @param content the content
     * @return the string
     */
    public static String hump2underline(String content) {
        StringBuilder sb = new StringBuilder(content);
        //定位
        int temp = 0;
        if (!content.contains(UNDERLINE)) {
            for (int i = 0; i < content.length(); i++) {
                if (Character.isUpperCase(content.charAt(i))) {
                    sb.insert(i + temp, UNDERLINE);
                    temp += 1;
                }
            }
        }
        return sb.toString().toLowerCase();
    }


    /**
     * Hump 2 minus string.
     *
     * @param content the content
     * @return the string
     */
    public static String hump2minus(String content) {
        StringBuilder sb = new StringBuilder(content);
        //定位
        int temp = 0;
        if (!content.contains(MINUS)) {
            for (int i = 0; i < content.length(); i++) {
                if (Character.isUpperCase(content.charAt(i))) {
                    sb.insert(i + temp, MINUS);
                    temp += 1;
                }
            }
        }
        return sb.toString().toLowerCase();
    }
}
