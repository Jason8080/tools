package cn.gmlee.tools.base;

import cn.gmlee.tools.base.util.BoolUtil;

import java.util.ArrayList;
import java.util.Collection;

public class BoolTests {

    public static void main(String[] args) {
        System.out.println(BoolUtil.isParentClass(Collection.class, ArrayList.class));
    }
}
