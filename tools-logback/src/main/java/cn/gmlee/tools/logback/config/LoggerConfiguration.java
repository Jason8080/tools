package cn.gmlee.tools.logback.config;

import cn.gmlee.tools.logback.db.mysql.MysqlLogger;
import cn.gmlee.tools.mybatis.dao.IBatisDao;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

/**
 * @author Jas°
 * @date 2021/3/8 (周一)
 */
@Configuration
@SuppressWarnings("all")
@ConditionalOnClass(IBatisDao.class)
public class LoggerConfiguration {

    public LoggerConfiguration(SqlSessionFactory sqlSessionFactory) {
        IBatisDao iBatisDao = new IBatisDao(sqlSessionFactory);
        MysqlLogger.I_BATIS_DAO = iBatisDao;
    }
}
