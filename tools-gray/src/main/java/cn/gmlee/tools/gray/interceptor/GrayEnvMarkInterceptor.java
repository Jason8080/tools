package cn.gmlee.tools.gray.interceptor;

import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.base.util.JdbcUtil;
import cn.gmlee.tools.base.util.QuickUtil;
import cn.gmlee.tools.base.util.RegexUtil;
import cn.gmlee.tools.gray.conf.GrayProperties;
import cn.gmlee.tools.gray.helper.GrayHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.List;

/**
 * 灰度环境数据标记拦截器.
 */
@Slf4j
@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}),
})
public class GrayEnvMarkInterceptor implements Interceptor {

    private final GrayProperties properties;

    public GrayEnvMarkInterceptor(GrayProperties properties) {
        this.properties = properties;
    }

    /**
     * 为了兼容3.4.5以下版本
     *
     * @param target
     * @return
     */
    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        try {
            filter(invocation);
        } catch (Throwable throwable) {
            log.error("灰度环境过滤失败", throwable);
        }
        return invocation.proceed();
    }

    private void filter(Invocation invocation) throws Exception {
        // 非灰度环境不处理
        if (!GrayHelper.enable()) {
            return;
        }
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        BoundSql boundSql = statementHandler.getBoundSql();
        String originSql = boundSql.getSql();
        boolean insert = originSql.trim().startsWith(JdbcUtil.INSERT);
        // 非插入句柄不处理
        if (!insert) {
            return;
        }
        String newSql = sqlHandler(originSql);
        Field field = boundSql.getClass().getDeclaredField("sql");
        boolean ok = field.isAccessible();
        QuickUtil.isFalse(ok, () -> field.setAccessible(true));
        ExceptionUtil.suppress(() -> field.set(boundSql, newSql));
        QuickUtil.isFalse(ok, () -> field.setAccessible(false));
    }

    private String sqlHandler(String originSql) {
        List<String> all = RegexUtil.find(originSql, "(?<=\\().*?(?=\\))");
        for (int i = 0; i < all.size(); i++){
            if(i == 0){
                String origin = all.get(i);
                String append = origin + ", \"" + properties.getEvn() + "\"";
                originSql = originSql.replace(origin, append);
                continue;
            }
            String origin = all.get(i);
            String append = origin + ", " + "0";
            originSql = originSql.replace(origin, append);
        }
        return originSql;
    }
}
