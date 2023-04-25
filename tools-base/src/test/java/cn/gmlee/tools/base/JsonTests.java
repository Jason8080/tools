package cn.gmlee.tools.base;

import cn.gmlee.tools.base.entity.Create;
import cn.gmlee.tools.base.mod.ConfigInfo;
import cn.gmlee.tools.base.mod.JsonResult;
import cn.gmlee.tools.base.util.JsonUtil;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Test;

import java.util.List;

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
    public void testGeneric() {
        // 静态1: 编译前指定范型 (强迫症患者)
        JsonResult<List<Create>> jsonResult1 = JsonUtil.toBean("", JsonResult.class, List.class, Create.class);
        // 静态2: 编译前指定范型 (匿名抽象类)
        JsonResult<List<Create>> jsonResult2 = JsonUtil.toBean("", new TypeReference<JsonResult<List<Create>>>() {});
        // 动态1: 运行时指定范型
        JsonResult<List<Create>> jsonResult3 = JsonUtil.toBean("", (JavaType) null);
    }


    @Test
    public void testGeneric2() {
        Class[] classes = {JsonResult.class, List.class, ConfigInfo.class, List.class, JsonResult.class};
        TypeFactory typeFactory = JsonUtil.getInstance().getTypeFactory();
        JavaType javaType = typeFactory.constructType(classes[classes.length - 1]);
        for (int i = classes.length - 1 - 1; i >= 0; i--) {
            javaType = typeFactory.constructParametricType(classes[i], javaType);
        }
        System.out.println(javaType);
    }
}
