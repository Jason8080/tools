package cn.gmlee.tools.base;

import cn.gmlee.tools.base.enums.XTime;
import cn.gmlee.tools.base.util.HexUtil;
import cn.gmlee.tools.base.util.SnUtil;
import cn.gmlee.tools.base.util.ThreadUtil;
import org.junit.Test;

/**
 * @author Jas°
 * @date 2021/7/7 (周三)
 */
public class SnUtilTests {

    public static void main(String[] args) {
        System.out.println(Long.toHexString(1234567890123456789L));
        String hex16 = HexUtil.hex16("1234567890123456789".getBytes());
        System.out.println(hex16);
        System.out.println(new String(HexUtil.hex16(hex16)));
    }


    @Test
    public void testDataSn() throws Exception {
        for (Integer i = 0; i < 100; i++) {
            Integer finalI = i;
            ThreadUtil.execute(() -> {
                String encode = SnUtil.dateSn(finalI.toString(), XTime.DAY_NONE);
                System.out.println(encode);
                String decode = SnUtil.dateId(encode, XTime.DAY_NONE);
                System.out.println(decode);
            });
        }
        Thread.sleep(100000);
    }


    @Test
    public void test() throws Exception {
        for (Integer i = 100; i < 101; i++) {
            Integer finalI = i;
            ThreadUtil.execute(() -> {
                String encode = SnUtil.encode(finalI.toString());
                System.out.println(encode);
                String decode = SnUtil.decode(encode);
                System.out.println(decode);
            });
        }
        Thread.sleep(100000);
    }
}
