package cn.gmlee.tools.base.util;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 字符操作工具
 *
 * @author Jas
 */
public class CharUtil {
    /**
     * 小写字母
     */
    public static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    /**
     * 大写字母
     */
    public static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    /**
     * 大小写字母
     */
    public static final String ALPHABET = LOWERCASE + UPPERCASE;
    /**
     * 数字
     */
    public static final String DIGITS = "0123456789";
    /**
     * 符号
     */
    public static final String SYMBOL = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";
    /**
     * 字母+数字+符号
     */
    public static final String DEFAULT_CHARACTERS = ALPHABET + DIGITS + SYMBOL;
    /**
     * 特殊字符
     */
    public static final String SPECIAL_CHARACTER = getSpecialCharacter();

    private static final Random RANDOM = new Random();

    /**
     * 获取Unicode中可打印的特殊字符
     *
     * @return special character
     */
    public static String getSpecialCharacter() {
        // 基本拉丁字符
        IntStream base = IntStream.rangeClosed(0x0020, 0x007E).filter(c -> !Character.isLetterOrDigit(c));
        // 补充拉丁字符
        IntStream supplement = IntStream.rangeClosed(0x00A0, 0x00FF).filter(c -> !Character.isLetterOrDigit(c));
        // 常用标点字符
        IntStream punctuation = IntStream.rangeClosed(0x2000, 0x206F).filter(c -> !Character.isLetterOrDigit(c));
        // 常见拉丁字符 = 基本拉丁字符 + 补充拉丁字符
        IntStream latin = IntStream.concat(base, supplement);
        return IntStream.concat(latin, punctuation).mapToObj(c -> String.valueOf((char) c))
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
    }

    /**
     * 随机生成字符串.
     *
     * @param length 指定长度
     * @return string 字符串
     */
    public static String randomString(int length) {
        return randomString(length, DEFAULT_CHARACTERS);
    }

    /**
     * 随机生成字符串 (限定符).
     *
     * <p>只使用指定的字符生成</p>
     *
     * @param length 指定长度
     * @param cs     可用字符
     * @return string 字符串
     */
    public static String randomString(int length, CharSequence cs) {
        if (length < 1) {
            return "";
        }
        CharSequence charSequence = NullUtil.get(cs, DEFAULT_CHARACTERS);
        return IntStream.range(0, length).map(i -> RANDOM.nextInt(charSequence.length())).mapToObj(charSequence::charAt)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
    }

    /**
     * Split list.
     *
     * @param content the content
     * @param symbol  the symbol
     * @return the list
     */
    public static List<String> split(String content, String symbol) {
        List<String> list = new ArrayList<>();
        if (BoolUtil.isEmpty(content)) {
            return list;
        }
        for (String c : content.split(symbol)) {
            list.add(c);
        }
        return list;
    }

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
        if (maxlength == -1 || NullUtil.get(content).length() < maxlength) {
            return content;
        }
        int start = show / 2;
        return NullUtil.get(content).substring(0, start) +
                "\t 内容过长 \t" +
                NullUtil.get(content).substring(NullUtil.get(content).length() - start);
    }

    /**
     * 截取.
     *
     * <p>内容超出最大长度后,直接丢弃</p>
     *
     * @param content   the content
     * @param maxlength the maxlength
     * @return string string
     */
    public static String cut(String content, int maxlength) {
        if (maxlength == -1 || NullUtil.get(content).length() < maxlength) {
            return content;
        }
        return NullUtil.get(content).substring(0, maxlength);
    }


    /**
     * Join c.
     *
     * @param <C> the type parameter
     * @param <O> the type parameter
     * @param os  the os
     * @return the c
     */
    public static <C extends CharSequence, O> C join(O... os) {
        return join(null, Arrays.asList(os));
    }

    /**
     * Join c.
     *
     * @param <C> the type parameter
     * @param <O> the type parameter
     * @param os  the os
     * @return the c
     */
    public static <C extends CharSequence, O> C join(Collection<O> os) {
        return join(null, Arrays.asList(os));
    }

    /**
     * Join c.
     *
     * @param <C> the type parameter
     * @param <O> the type parameter
     * @param c   the c
     * @param os  the os
     * @return the c
     */
    public static <C extends CharSequence, O> C join(C c, O... os) {
        return join(c, Arrays.asList(os));
    }

    /**
     * Join c.
     *
     * @param <C> the type parameter
     * @param <O> the type parameter
     * @param c   the c
     * @param os  the os
     * @return the c
     */
    public static <C extends CharSequence, O> C join(C c, Collection<O> os) {
        if (BoolUtil.isEmpty(os)) {
            return (C) "";
        }
        return (C) os.stream().filter(Objects::nonNull).map(Objects::toString)
                .collect(Collectors.joining(BoolUtil.isEmpty(c) ? "" : c.toString()));
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
