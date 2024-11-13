package cn.gmlee.tools.base;

import cn.gmlee.tools.base.util.CollectionUtil;
import org.junit.Test;

import java.util.*;

public class CollectionUtilTests {

    @Test
    public void testDiff(){
        List<Integer> c1 = Arrays.asList(1, 2, 3, 4);
        List<Integer> c2 = Arrays.asList(3, 4, 5, 6);
        List<Integer> c3 = Arrays.asList(4, 5, 6, 7);
        Set diffKeys = CollectionUtil.commonKeys(c1, c2, c3);
        System.out.println(diffKeys);
    }
}
