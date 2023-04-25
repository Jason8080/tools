package cn.gmlee.tools.base;

import cn.gmlee.tools.base.util.QuickUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuickTests {

    @Test
    public void testIs() {
        QuickUtil.is("".isEmpty(), () -> System.out.println("true"), () -> System.out.println("false"));
        QuickUtil.is(x -> " ".isEmpty(), () -> System.out.println("true"), () -> System.out.println("false"));
    }

    @Test
    public void testBatch() {
        List<Integer> list = new ArrayList();
        for (int i = 0 ; i < 1010; i++){
            list.add(i);
        }
        QuickUtil.batch(list.toArray(new Integer[]{}), 100, c -> {
            System.out.println(Arrays.toString(c));
        });
    }
}
