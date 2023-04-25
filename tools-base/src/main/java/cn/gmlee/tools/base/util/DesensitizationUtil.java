package cn.gmlee.tools.base.util;


import java.math.BigDecimal;

/**
 * The type Desensitization util.
 *
 * @author Jas °
 */
@SuppressWarnings("all")
public final class DesensitizationUtil {
    /**
     * 显示指定部分.
     *
     * @param content     内容
     * @param frontLength 显示前n位
     * @param laterLength 显示前m位
     * @return 脱敏内容 string
     */
    public static String show(String content, int frontLength, int laterLength) {
        if (BoolUtil.isEmpty(content)) {
            return content;
        }
        int length = content.length();
        if (BoolUtil.isEmpty(content)) {
            return content;
        }
        if (length < frontLength + laterLength) {
            return content.replaceAll(".", "*");
        }
        String front = content.substring(0, frontLength);
        String middle = content.substring(frontLength, length - laterLength);
        String later = content.substring(length - laterLength);
        return front + middle.replaceAll(".", "*") + later;
    }

    /**
     * 隐藏一半的内容.
     *
     * @param content the content
     * @return the string
     */
    public static String hide(String content) {
        return hide(content, 0.38);
    }

    /**
     * 隐藏指定比例的内容.
     *
     * @param content the content
     * @param ratio   the ratio
     * @return the string
     */
    public static String hide(String content, double ratio) {
        if (BoolUtil.isEmpty(content)) {
            return content;
        }
        int length = content.length();
        if (length == 2) {
            return show(content, 1, 0);
        }
        if (length == 3) {
            return show(content, 1, 1);
        }
        if (ratio > 1) {
            ratio = 1;
        }
        if (ratio < 0) {
            ratio = 0;
        }
        BigDecimal hideDecimal = BigDecimal.valueOf(length).multiply(BigDecimal.valueOf(ratio));
        BigDecimal showDecimal = BigDecimal.valueOf(length).subtract(hideDecimal);
        BigDecimal helfDecimal = showDecimal.divide(BigDecimal.valueOf(2));
        // 显示的长度向下取整: 尽量少显示
        int i = helfDecimal.setScale(0, BigDecimal.ROUND_DOWN).intValue();
        // 防止一个都不显示: 但不可能出现一个都不隐藏
        if(i < 1){
            return show(content, 1, 1);
        }
        return show(content, i, i);
    }

    /**
     * 显示指定符号之前的.
     *
     * @param content 内容
     * @param chars   指定符号
     * @return 脱敏内容 string
     */
    public static String showBefore(String content, String chars) {
        if (BoolUtil.isEmpty(content)) {
            return content;
        }
        if (!content.contains(chars)) {
            return content.replaceAll(".", "*");
        }
        int index = content.lastIndexOf(chars);
        String front = content.substring(0, index);
        String later = content.substring(index);
        return front + later.replaceAll(".", "*");
    }

    /**
     * 显示指定符号之后的.
     *
     * @param content 内容
     * @param chars   指定符号
     * @return 脱敏内容 string
     */
    public static String showAfter(String content, String chars) {
        if (BoolUtil.isEmpty(content)) {
            return content;
        }
        if (!content.contains(chars)) {
            return content.replaceAll(".", "*");
        }
        int index = content.lastIndexOf(chars);
        String front = content.substring(0, index);
        String later = content.substring(index);
        return front.replaceAll(".", "*") + later;
    }


    /**
     * 手机脱密.
     *
     * @param mobile the mobile
     * @return the string
     */
    public static String mobile(String mobile) {
        return show(mobile, 3, 4);
    }

    /**
     * 邮件地址脱敏.
     *
     * @param email the email
     * @return the string
     */
    public static String email(String email) {
        String after = showAfter(email.substring(1), "@");
        return email.substring(0, 1) + after;
    }

    /**
     * 银行卡号脱敏.
     *
     * @param bank the bank
     * @return the string
     */
    public static String bank(String bank) {
        return show(bank, 6, 4);
    }

    /**
     * 身份证号脱敏.
     *
     * @param idCard the id card
     * @return the string
     */
    public static String idCard(String idCard) {
        return show(idCard, 1, 4);
    }

    /**
     * 姓名脱敏.
     *
     * @param name the name
     * @return the string
     */
    public static String name(String name) {
        return show(name, name.length() / 2, 0);
    }
}
