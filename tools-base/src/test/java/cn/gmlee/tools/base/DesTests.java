package cn.gmlee.tools.base;

import cn.gmlee.tools.base.util.DesUtil;
import cn.gmlee.tools.base.util.IdUtil;

/**
 * @author Jas°
 * @date 2021/4/27 (周二)
 */
public class DesTests {
    public static void main(String[] args) {
        String content = IdUtil.uuidReplaceUpperCase();
        String secretKey = "1608408689";
        String encode = DesUtil.encode(content, secretKey);
        System.out.println(encode);
        System.out.println(encode.getBytes().length);
        System.out.println(DesUtil.decode(encode, secretKey));
    }
}
