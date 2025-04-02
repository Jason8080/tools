package cn.gmlee.tools.base;

import cn.gmlee.tools.base.util.DiffUtil;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DiffUtilTests {

    @Test
    public void comparativeReport() {
        String primaryKey = "id";
        Map<String, Object> oldData = new HashMap<>();
        oldData.put("id", 1);
        oldData.put("name", "张三");
        oldData.put("age", 18);
        oldData.put("sex", "男");
        oldData.put("address", "北京");
        oldData.put("create_time", LocalDateTime.now());
        Map<String, Object> newData = new HashMap<>();
        newData.put("id", 1);
        newData.put("name", "李四");
        newData.put("age", 19);
        newData.put("sex", "男");
        newData.put("address", "北京");
        newData.put("update_time", new Date());
        Map<String, String> fieldCommentMap = new HashMap<>();
        fieldCommentMap.put("id", "主键");
        fieldCommentMap.put("name", "姓名");
        fieldCommentMap.put("age", "年龄");
        fieldCommentMap.put("sex", "性别");
        fieldCommentMap.put("address", "地址");
        fieldCommentMap.put("create_time", "创建时间");
        fieldCommentMap.put("update_time", "更新时间");
        System.out.println(DiffUtil.comparativeReport(oldData, newData, fieldCommentMap));
    }
}
