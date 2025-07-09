package cn.gmlee.tools.base;

import cn.gmlee.tools.base.mod.*;
import cn.gmlee.tools.base.util.DiffUtil;
import cn.gmlee.tools.base.util.JsonUtil;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class DiffTests {

    @Test
    public void testDiff() {
        ConfigInfo source = new ConfigInfo();
        source.setId(1L);
        source.setCode("c1");
        source.setName(null);
        source.setValue(null);
        ConfigInfo target = new ConfigInfo();
        target.setId(1L);
        target.setCode(null);
        target.setName("n2");
        target.setValue(null);
        List<Diff> diffs = DiffUtil.get(Arrays.asList(source, target), Arrays.asList(target, source), 1);
        System.out.println(JsonUtil.format(diffs));
    }
}
