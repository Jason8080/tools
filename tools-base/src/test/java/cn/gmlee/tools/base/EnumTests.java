package cn.gmlee.tools.base;

import cn.gmlee.tools.base.enums.XCode;
import cn.gmlee.tools.base.util.EnumUtil;

public class EnumTests {


    public static void main(String[] args) {
        XCode xCode = EnumUtil.value(200, XCode.class);
        System.out.println(xCode);
    }
}
