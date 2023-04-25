package cn.gmlee.tools.logback.config;

import cn.gmlee.tools.logback.db.mysql.MysqlLogger;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 关闭插入到数据库的日志
 *
 * @author Jas°
 * @date 2021/3/8 (周一)
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({DisabledInsertLogger.DisabledInsertLog.class})
public @interface DisabledInsertLogger {

    class DisabledInsertLog {
        public DisabledInsertLog() {
            MysqlLogger.enable = false;
        }
    }
}
