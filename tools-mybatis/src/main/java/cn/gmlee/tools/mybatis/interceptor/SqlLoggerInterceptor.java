package cn.gmlee.tools.mybatis.interceptor;

import cn.gmlee.tools.base.mod.Kv;
import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.mybatis.assist.SqlRealAssist;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.sql.PreparedStatement;

@Slf4j
@Intercepts({
        @Signature(type = ParameterHandler.class, method = "setParameters", args = {PreparedStatement.class})
})
public class SqlLoggerInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Kv<Long, Long> kv = new Kv();
        Object proceed;
        try {
            kv.setKey(System.currentTimeMillis());
            proceed = invocation.proceed();
        } catch (Throwable throwable) {
            throw throwable;
        } finally {
            kv.setVal(System.currentTimeMillis());
            ExceptionUtil.sandbox(() -> print(invocation, kv.getVal() - kv.getKey()), false);
        }
        return proceed;
    }

    private void print(Invocation invocation, long ms) {
        // 处理程序
        ParameterHandler parameterHandler = (ParameterHandler) invocation.getTarget();
        MetaObject metaObject = SystemMetaObject.forObject(parameterHandler);
        // 获取脚本
        BoundSql boundSql = (BoundSql) metaObject.getValue("boundSql");
        // 获取参数
        Object parameterObject = parameterHandler.getParameterObject();
        // 获取配置
        Configuration configuration = (Configuration) metaObject.getValue("configuration");
        // 替换压缩
        String sql = SqlRealAssist.replaceStrip(configuration, boundSql, parameterObject);
        // 获取事务
        String tx = TransactionSynchronizationManager.getCurrentTransactionName();
        boolean active = TransactionSynchronizationManager.isActualTransactionActive();
        boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
        Integer isolation = TransactionSynchronizationManager.getCurrentTransactionIsolationLevel();
        // 打印语句
        log.info("Transaction(active/read-only/isolation/ms): {}({}/{}/{}/{})\r\n\r\n==> Executing sql: {}", tx, active, readOnly, isolation, ms, sql);
    }

}
