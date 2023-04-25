package cn.gmlee.tools.logback;

import cn.gmlee.tools.logback.db.mysql.MysqlLogger;
import cn.gmlee.tools.logback.db.LoggerFactory;

/**
 * @author Jas°
 * @date 2021/3/8 (周一)
 */
public class LogTests {
    private static MysqlLogger logger = LoggerFactory.getLogger(LogTests.class);

    public static void main(String[] args) {
        logger.print("11111");
    }
}
