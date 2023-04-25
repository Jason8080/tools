package cn.gmlee.tools.api.gray.model;

import java.util.Arrays;
import java.util.List;

/**
 * 灰度规则匹配器
 *
 * @author Jas°
 * @date 2020/12/1 (周二)
 */
public class GrayEnums {
    public static final String IPS = "ips";
    public static final String TOKENS = "tokens";
    public static final String WEIGHT = "weight";
    public static final List<String> RULES = Arrays.asList(IPS, TOKENS, WEIGHT);


    public static final String GRAY_COOKIE = "GRAY_COOKIE";
}
