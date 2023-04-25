package cn.gmlee.tools.base;

import cn.gmlee.tools.base.entity.Create;
import cn.gmlee.tools.base.util.ClassUtil;
import org.junit.Test;

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

}
