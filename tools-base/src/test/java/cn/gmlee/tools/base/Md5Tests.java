package cn.gmlee.tools.base;

import cn.gmlee.tools.base.util.Md5Util;
import org.junit.Test;

/**
 * .
 *
 * @author Jas°
 * @date 2021/10/13 (周三)
 */
public class Md5Tests {
    @Test
    public void testPass(){
        String password = "Aa123456.";
        String slat = "165983";
        System.out.println(Md5Util.encode(password + slat));
    }
}
