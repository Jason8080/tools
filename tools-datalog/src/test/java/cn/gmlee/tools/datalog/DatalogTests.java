package cn.gmlee.tools.datalog;

import cn.gmlee.tools.base.util.JsonUtil;
import cn.gmlee.tools.datalog.model.Datalog;

/**
 * .
 *
 * @author Jas°
 * @date 2021/8/5 (周四)
 */
public class DatalogTests {
    public static void main(String[] args) {
        op();
    }

    private static void op() {
        Datalog datalog = new Datalog();
        System.out.println(JsonUtil.format(datalog));
    }
}
