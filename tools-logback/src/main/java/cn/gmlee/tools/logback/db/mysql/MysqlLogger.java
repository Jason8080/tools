package cn.gmlee.tools.logback.db.mysql;

import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.logback.db.Log;
import cn.gmlee.tools.mybatis.dao.IBatisDao;
import org.slf4j.Logger;

import java.util.Objects;

/**
 * @author Jas°
 * @date 2021/3/8 (周一)
 */
@SuppressWarnings("all")
public class MysqlLogger<T extends Log> {

    private final Logger logger;
    private final Class<T> tClass;
    public static IBatisDao I_BATIS_DAO;
    public static boolean enable = true;

    public MysqlLogger(Logger logger) {
        this.logger = logger;
        this.tClass = null;
    }

    public MysqlLogger(Logger logger, Class<T> tClass) {
        this.logger = logger;
        this.tClass = tClass;
    }

    public void print(String msg) {
        if (logger.isTraceEnabled()) {
            logger.trace(msg);
        } else if (logger.isDebugEnabled()) {
            logger.debug(msg);
        } else if (logger.isInfoEnabled()) {
            logger.info(msg);
        } else if (logger.isWarnEnabled()) {
            logger.warn(msg);
        } else if (logger.isErrorEnabled()) {
            logger.error(msg);
        }
        insert(msg);
    }

    public void print(String format, Object arg) {
        if (logger.isTraceEnabled()) {
            logger.trace(format, arg);
        } else if (logger.isDebugEnabled()) {
            logger.debug(format, arg);
        } else if (logger.isInfoEnabled()) {
            logger.info(format, arg);
        } else if (logger.isWarnEnabled()) {
            logger.warn(format, arg);
        } else if (logger.isErrorEnabled()) {
            logger.error(format, arg);
        }
        String replace = format.replace("{}", "%s");
        insert(String.format(replace, arg));
    }

    public void print(String format, Object arg1, Object arg2) {
        if (logger.isTraceEnabled()) {
            logger.trace(format, arg1, arg2);
        } else if (logger.isDebugEnabled()) {
            logger.debug(format, arg1, arg2);
        } else if (logger.isInfoEnabled()) {
            logger.info(format, arg1, arg2);
        } else if (logger.isWarnEnabled()) {
            logger.warn(format, arg1, arg2);
        } else if (logger.isErrorEnabled()) {
            logger.error(format, arg1, arg2);
        }
        String replace = format.replace("{}", "%s");
        insert(String.format(replace, arg1, arg2));
    }

    public void print(String format, Object... arguments) {
        if (logger.isTraceEnabled()) {
            logger.trace(format, arguments);
        } else if (logger.isDebugEnabled()) {
            logger.debug(format, arguments);
        } else if (logger.isInfoEnabled()) {
            logger.info(format, arguments);
        } else if (logger.isWarnEnabled()) {
            logger.warn(format, arguments);
        } else if (logger.isErrorEnabled()) {
            logger.error(format, arguments);
        }
        String replace = format.replace("{}", "%s");
        insert(String.format(replace, arguments));
    }

    public void print(String msg, Throwable t) {
        if (logger.isTraceEnabled()) {
            logger.trace(msg, t);
        } else if (logger.isDebugEnabled()) {
            logger.debug(msg, t);
        } else if (logger.isInfoEnabled()) {
            logger.info(msg, t);
        } else if (logger.isWarnEnabled()) {
            logger.warn(msg, t);
        } else if (logger.isErrorEnabled()) {
            logger.error(msg, t);
        }
        insert(msg, t);
    }

    private void insert(String msg, Throwable throwable) {
        if (enable && Objects.nonNull(I_BATIS_DAO) && Objects.nonNull(tClass)) {
            try {
                Log log = tClass.getConstructor().newInstance();
                log.setMsg(msg);
                log.setThrowableMsg(ExceptionUtil.getAllMsg(throwable));
                I_BATIS_DAO.execute(log, log.getClass().getName().concat(".").concat("insert"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void insert(String msg) {
        if (enable && Objects.nonNull(I_BATIS_DAO) && Objects.nonNull(tClass)) {
            try {
                Log log = tClass.getConstructor().newInstance();
                log.setMsg(msg);
                I_BATIS_DAO.execute(log, log.getClass().getName().concat(".").concat("insert"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
