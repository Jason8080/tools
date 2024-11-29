package cn.gmlee.tools.base;

import cn.gmlee.tools.base.entity.Create;
import cn.gmlee.tools.base.mod.ConfigInfo;
import cn.gmlee.tools.base.mod.R;
import cn.gmlee.tools.base.util.CollectionUtil;
import cn.gmlee.tools.base.util.JsonUtil;
import cn.gmlee.tools.base.util.Md5Util;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.Getter;
import lombok.Setter;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class JsonTests {


    public static void main(String[] args) {
//        User obj = new User();
//        List<User> list = new ArrayList();
//        list.add(obj);
//        obj.setPartners(list);
//        System.out.println(JsonUtil.toJson(obj));
        // --------------------------------------
        JSONObject jo1 = new JSONObject();
        jo1.put("jo1", jo1);
        System.out.println(JsonUtil.toJson(jo1));
    }
    @Test
    public void testSort(){
        Aa obj = new Aa();
        obj.setObj(new Aa());
        System.out.println(JsonUtil.toJson(obj));
    }

    @Test
    public void testSort2(){
        String str = "{\"c1\":\"c1\",\"b1\":\"b1\",\"b2\":\"b2\",\"c1\":\"c1\",\"a2\":\"a2\",\"obj\":{\"e1\":\"e1\",\"b1\":\"b1\",\"b2\":\"b2\",\"c1\":\"c1\",\"obj\":null}}";
        Map map = JsonUtil.toBean(str, TreeMap.class);
        map = CollectionUtil.keyReverseSort(map);
        String json = JsonUtil.toJson(map);
        System.out.println(json);
        String md5 = Md5Util.encode(json);
        System.out.println(md5);
    }

    @Getter
    public static class Aa {
        private String b1 = "b1";
        private String a2 = "a2";
        private String c1 = "c1";
        private String b2 = "b2";
        private String e1 = "e1";
        @Setter
        private Aa obj;
    }



    @Test
    public void testGeneric() {
        // 静态1: 编译前指定范型 (强迫症患者)
        R<List<Create>> r1 = JsonUtil.toBean("", R.class, List.class, Create.class);
        // 静态2: 编译前指定范型 (匿名抽象类)
        R<List<Create>> r2 = JsonUtil.toBean("", new TypeReference<R<List<Create>>>() {});
        // 动态1: 运行时指定范型
        R<List<Create>> r3 = JsonUtil.toBean("", (JavaType) null);
    }


    @Test
    public void testGeneric2() {
        Class[] classes = {R.class, List.class, ConfigInfo.class, List.class, R.class};
        TypeFactory typeFactory = JsonUtil.getInstance().getTypeFactory();
        JavaType javaType = typeFactory.constructType(classes[classes.length - 1]);
        for (int i = classes.length - 1 - 1; i >= 0; i--) {
            javaType = typeFactory.constructParametricType(classes[i], javaType);
        }
        System.out.println(javaType);
    }
}
