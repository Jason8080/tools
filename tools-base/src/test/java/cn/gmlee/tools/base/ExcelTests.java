package cn.gmlee.tools.base;

import cn.gmlee.tools.base.enums.XTime;
import cn.gmlee.tools.base.util.ExcelUtil;
import cn.gmlee.tools.base.util.TimeUtil;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.FileOutputStream;
import java.util.*;

/**
 * @author Jas°
 * @date 2021/3/31 (周三)
 */
public class ExcelTests {

    public static void main(String[] args) throws Exception {
        Collection<Map<String, Object>> c = new ArrayList();
        Map<String, Object> map1 = new LinkedHashMap();
        map1.put("one", 1);
        map1.put("two", 2);
        map1.put("three", 3);
        map1.put("five", 5);
        c.add(map1);
        System.out.println(map1.keySet());
        Map<String, Object> map2 = new LinkedHashMap();
        map2.put("one", 1);
        map2.put("two", 2);
        map2.put("three", 3);
        map2.put("four", new Date());
        c.add(map2);
        System.out.println(map2.keySet());
        ExcelUtil.install(Date.class, (date) -> TimeUtil.format(date, XTime.DAY_MINUS));
        Workbook workbook = ExcelUtil.create(c);
        workbook.write(new FileOutputStream("C:\\Users\\gmlee\\Desktop\\实验大厅\\tools\\out.xlsx"));
    }
}
