package cn.gmlee.tools.base;

import cn.gmlee.tools.base.builder.KvBuilder;
import cn.gmlee.tools.base.builder.MapBuilder;
import cn.gmlee.tools.base.mod.Kv;
import cn.gmlee.tools.base.util.JsonUtil;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class BuilderTests {

    @Test
    public void testMap() {
        Map<String, String> map = MapBuilder.build(
                "k1", "v1", // 首对键值对的类型决定集合的类型 (其他类型将不被保存)
                "k2", "v2", // ✓
                "k3", null, // ✓
                null, "v4", // ✓
                null, "v5", // ✓ (延续HashMap键唯一将覆盖楼上)
                5, 6, // ×
                5, "6", // ×
                "5", 6, // ×
                "5", "6", // ✓
                "7" // × (单独的键将不被保存)
        );
        System.out.println(map);
    }

    @Test
    public void testMapForeach() {
        MapBuilder<String, String> builder = MapBuilder.build("k1", "v1");
        for(int i=0; i<10; i++){
            builder.add("K"+i, "V"+i);
        }
        System.out.println(builder);
    }

    @Test
    public void testKvs() {
        List<Kv<String, String>> list = KvBuilder.build("k1", "v1");
        for(int i=0; i<2; i++){
            list.add(new Kv("K"+i, "V"+i));
        }
        System.out.println(JsonUtil.toJson(list));
    }


}
