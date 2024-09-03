package cn.gmlee.tools.base.util;

import java.io.UnsupportedEncodingException;

/**
 * 字符操作工具
 *
 * @author Jas
 */
public class CharUtil {
    /**
     * 摘要.
     *
     * <p>内容超出最大长度后,中间替换为内容过长仅保留前后段部分</p>
     *
     * @param content   目标内容
     * @param maxlength 限制长度
     * @return string 摘要内容
     */
    public static String digest(String content, int maxlength) {
        return digest(content, maxlength, maxlength);
    }

    /**
     * 摘要.
     *
     * <p>内容超出最大长度后,中间替换为内容过长仅保留前后段部分</p>
     *
     * @param content   目标内容
     * @param maxlength 限制长度
     * @param show      摘要长度
     * @return string 摘要内容
     */
    public static String digest(String content, int maxlength, int show) {
        if (maxlength == -1 || content.length() < maxlength) {
            return content;
        }
        int start = show / 2;
        return content.substring(0, start) +
                "\t 内容过长 \t" +
                content.substring(content.length() - start);
    }

    /**
     * 获取非空的字符串.
     *
     * @param os the os
     * @return the string
     */
    public static String getNonempty(String... os) {
        if (os == null) {
            return "";
        }
        for (String o : os) {
            if (BoolUtil.notEmpty(o)) {
                return o;
            }
        }
        return "";
    }

    /**
     * 字节转字符.
     *
     * @param charset the charset
     * @param bytes   the bytes
     * @return the string
     */
    public static String toString(String charset, byte... bytes) {
        if (bytes != null) {
            try {
                return new String(bytes, charset);
            } catch (UnsupportedEncodingException e) {
                ExceptionUtil.cast("字符集编码错误", e);
            }
        }
        return "";
    }

    /**
     * 字节转字符.
     *
     * @param bytes the bytes
     * @return the string
     */
    public static String toString(byte... bytes) {
        if (bytes != null) {
            return new String(bytes);
        }
        return "";
    }

    /**
     * 在{@param source}每个符号后面插入{@param target}.
     *
     * @param source 马冬梅
     * @param target %
     * @param before 是否将{@param target}放到最前面
     * @return %马%冬%梅
     */
    public static String charset(String source, String target, boolean before) {
        StringBuilder sb = new StringBuilder();
        if (BoolUtil.notEmpty(source)) {
            if (before) {
                sb.append(target);
            }
            for (char c : source.toCharArray()) {
                sb.append(c);
                sb.append(target);
            }
        }
        return sb.toString();
    }


    /**
     * 在{@param source}每个符号后面插入{@param target}.
     *
     * @param source the source
     * @param target the target
     * @return the string
     */
    public static String charset(String source, String target) {
        return charset(source, target, true);
    }


    /**
     * 自动补充到指定长度.
     *
     * @param num the num
     * @param len the len
     * @return the string
     */
    public static String replenish(Integer num, int len) {
        if (num != null) {
            return replenish(num.toString(), len, "0");
        }
        return null;
    }

    /**
     * 自动补充到指定长度.
     *
     * @param num the num
     * @param len the len
     * @return the string
     */
    public static String replenish(Long num, int len) {
        if (num != null) {
            return replenish(num.toString(), len, "0");
        }
        return null;
    }

    /**
     * 自动补充到指定长度.
     *
     * @param num the num
     * @param len the len
     * @return the string
     */
    public static String replenish(String num, int len) {
        if (num != null) {
            return replenish(num, len, "0");
        }
        return null;
    }

    /**
     * 自动补充到指定长度 (补在前面).
     *
     * @param num the num
     * @param len the len
     * @param cs  the cs
     * @return the string
     */
    public static String replenish(String num, int len, String cs) {
        return replenish(num, len, cs, false);
    }

    /**
     * 自动补充到指定长度.
     *
     * @param num   the num
     * @param len   the len
     * @param cs    the cs
     * @param after the after
     * @return the string
     */
    public static String replenish(String num, int len, String cs, boolean after) {
        if (num != null && BoolUtil.notEmpty(cs)) {
            StringBuilder sb = new StringBuilder(num);
            while (sb.length() < len) {
                if (after) {
                    sb.append(cs);
                } else {
                    sb.insert(0, cs);
                }
            }
            return sb.substring(0, len);
        }
        return null;
    }
}
