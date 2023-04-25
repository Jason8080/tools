package cn.gmlee.tools.base;

import cn.gmlee.tools.base.util.AddressUtil;
import cn.gmlee.tools.base.util.TimeUtil;
import org.junit.Test;

public class AddressTests {

    @Test
    public void test(){
        System.out.println(TimeUtil.getCurrentTimestampYear().byteValue());
        String ip = AddressUtil.getIp();
        String mac = AddressUtil.getMac();
        System.out.println(ip);
        System.out.println(mac);
    }
}
