package cn.gmlee.tools.base;

import cn.gmlee.tools.base.util.ScriptUtil;
import org.junit.Test;

public class ScriptUtilTests {

    @Test
    public void testMath() {
        Boolean execute = ScriptUtil.eval("1>=1 && false");
        System.out.println(execute.getClass());
        System.out.println(execute);
    }
}
