package cn.gmlee.tools.base;

import cn.gmlee.tools.base.mod.User;
import cn.gmlee.tools.base.util.CopyUtil;

import java.util.Arrays;

public class CopyTests {


    public static void main(String[] args) {
        User o1 = new User();
        o1.setName("O1");
        User o2 = new User();
        o2.setName("O2");
        User o3 = new User();
        o3.setName("O3");
        o1.setPartners(Arrays.asList(o2, o3));
        System.out.println(o1);
        User o = CopyUtil.get(o1);
        System.out.println(o);
    }
}
