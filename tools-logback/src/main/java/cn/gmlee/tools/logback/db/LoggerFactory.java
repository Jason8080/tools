package cn.gmlee.tools.logback.db;

import cn.gmlee.tools.logback.db.mysql.MysqlLogger;
import org.slf4j.Logger;

/**
 * 日志工具包.
 *
 * @author Jas °
 */
public final class LoggerFactory {


    /**
     * Get logger mysql logger.
     *
     * @param clazz  日志位置
     * @return 日志对象
     */
    public static MysqlLogger getLogger(Class<?> clazz){
        Logger logger = org.slf4j.LoggerFactory.getLogger(clazz);
        return new MysqlLogger(logger);
    }


    /**
     * Get logger mysql logger.
     *
     * @param clazz  日志位置
     * @param tClass 实体类/持久化命名空间
     * @return 日志对象
     */
    public static <T extends Log> MysqlLogger getLogger(Class<?> clazz, Class<T> tClass){
        Logger logger = org.slf4j.LoggerFactory.getLogger(clazz);
        return new MysqlLogger(logger, tClass);
    }
}
