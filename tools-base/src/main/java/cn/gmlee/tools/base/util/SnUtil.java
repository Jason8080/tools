package cn.gmlee.tools.base.util;

import cn.gmlee.tools.base.enums.XTime;

/**
 * 虚拟编号工具.
 *
 * @author Jas
 */
public class SnUtil {
    private static final String[] DEFAULT_KEYS = {"TOOLS"};

    /**
     * 编码.
     *
     * @param content 原文
     * @param keys    加密密钥
     * @return string 密文
     */
    public static String encode(String content, String... keys) {
        assert BoolUtil.notEmpty(content);
        // 默认密钥
        if (BoolUtil.isEmpty(keys)) {
            keys = DEFAULT_KEYS;
        }
        // 加密内容
        String key = CharUtil.replenish(String.join(",", keys), 8);
        String encode = DesUtil.encode(content, key);
        return HexUtil.hex16(encode.getBytes());
    }

    /**
     * 解码.
     *
     * @param content 密文
     * @param keys    加密密钥
     * @return string 原文
     */
    public static String decode(String content, String... keys) {
        assert BoolUtil.notEmpty(content);
        // 默认密钥
        if (BoolUtil.isEmpty(keys)) {
            keys = DEFAULT_KEYS;
        }
        // 加密内容
        String key = CharUtil.replenish(String.join(",", keys), 8);
        return DesUtil.decode(new String(HexUtil.hex16(content)), key);
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * 量号.
     * <p>
     * 量号: 将产量转换成以日期格式开头的序列号, 简称量号
     * 产值: 当年/当月/当日的产量; 比如: 2020年的订单量为874600, 那么其对应的唯一主键是: 2020874600
     * 序列号: 希望将产值体现到序列号上的需求; 比如2020874600, 它表示为2020年度第874600个订单
     * 注意: 请将id压制到合理的范围内 (比如产值超出4位数范畴请不要压制到4位以内, 默认20位)
     * </p>
     *
     * @param id    the id
     * @param idLen the len
     * @param xTime the x time
     * @return the string
     */
    public static String dateSn(String id, int idLen, XTime xTime) {
        String date = TimeUtil.getCurrentDatetime(xTime);
        return date + CharUtil.replenish(id, idLen);
    }

    /**
     * 量号 (原始长度).
     * <p>
     * 量号: 将产量转换成以日期格式开头的序列号, 简称量号
     * 产值: 当年/当月/当日的产量; 比如: 2020年的订单量为874600, 那么其对应的唯一主键是: 2020874600
     * 序列号: 希望将产值体现到序列号上的需求; 比如2020874600, 它表示为2020年度第874600个订单
     * 注意: 请将id压制到合理的范围内 (比如产值超出4位数范畴请不要压制到4位以内, 默认20位)
     * </p>
     *
     * @param id    the id
     * @param xTime the x time
     * @return the string
     */
    public static String dateSn(String id, XTime xTime) {
        String date = TimeUtil.getCurrentDatetime(xTime);
        return date + id;
    }

    /**
     * 还原产值.
     * <p>
     * 量号: 将产量转换成以日期格式开头的序列号, 简称量号
     * 产值: 当年/当月/当日的产量; 比如: 2020年的订单量为874600, 那么其对应的唯一主键是: 2020874600
     * 序列号: 希望将产值体现到序列号上的需求; 比如2020874600, 它表示为2020年度第874600个订单
     * 注意: 请将id压制到合理的范围内 (比如产值超出4位数范畴请不要压制到4位以内, 默认20位)
     * </p>
     *
     * @param sn    the sn
     * @param xTime the x time
     * @return the string
     */
    public static String dateId(String sn, XTime xTime) {
        StringBuilder sb = new StringBuilder();
        String str = sn.substring(xTime.pattern.length());
        for (char c : str.toCharArray()) {
            if (sb.length() > 0 || c != '0') {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
