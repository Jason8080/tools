package cn.gmlee.tools.base;

import cn.gmlee.tools.base.entity.Create;
import cn.gmlee.tools.base.mod.JsonLog;
import cn.gmlee.tools.base.util.ClassUtil;
import cn.gmlee.tools.base.util.LoginUtil;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.Map;

/**
 * @author Jas.
 */
public class ClassTests {

    @Test
    public void test() {
        Create entity = new Create();
        entity.setCreateAt(new Date());
        Map<String, Object> map = ClassUtil.generateMap(entity);
        System.out.println(map);
    }

    @Test
    public void testDigest() {
        Object call = ClassUtil.call(null, "cn.gmlee.tools.base.util.LoginUtil#get(boolean)", false);
        System.out.println(call);
    }

    @Test
    public void testCall() {
        Object call1 = ClassUtil.callSimple(new JsonLog(), "log");
        Object call2 = ClassUtil.callSimple(call1, "setUrl", "https://www.baidu.com");
        System.out.println(call2);
    }

    public static void main(String[] args) throws InterruptedException {
        new ClassTests().test();
        Thread.sleep(100000000);
    }
}
