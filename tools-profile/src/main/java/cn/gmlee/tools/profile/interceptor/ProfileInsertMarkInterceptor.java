package cn.gmlee.tools.profile.interceptor;

import cn.gmlee.tools.base.builder.KvBuilder;
import cn.gmlee.tools.base.util.SqlUtil;
import cn.gmlee.tools.profile.assist.SqlAssist;
import cn.gmlee.tools.profile.conf.ProfileProperties;
import cn.gmlee.tools.profile.helper.ProfileHelper;
import cn.gmlee.tools.profile.initializer.ProfileDataTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Connection;

/**
 * 数据隔离标记拦截器.
 */
@Slf4j
@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}),
})
public class ProfileInsertMarkInterceptor implements Interceptor {

    private final ProfileProperties properties;

    @Autowired
    private ProfileDataTemplate profileDataTemplate;

    public ProfileInsertMarkInterceptor(ProfileProperties properties) {
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
            mark(invocation);
        } catch (Throwable throwable) {
            log.error("数据隔离标记失败", throwable);
        }
        return invocation.proceed();
    }

    private void mark(Invocation invocation) throws Exception {
        // 关闭的不处理: 数据库默认生产
        if (ProfileHelper.closed() || !ProfileHelper.enabled(ProfileHelper.ReadWrite.WRITE)) {
            return;
        }
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        BoundSql boundSql = statementHandler.getBoundSql();
        String originSql = boundSql.getSql();
        // 构建新的句柄
        SqlUtil.reset(SqlUtil.DataType.of(profileDataTemplate.getDatabaseProductName()));
        String newSql = SqlUtil.newInsert(originSql, KvBuilder.array(properties.getField(), 0));
        SqlAssist.reset(boundSql, newSql);
    }
}
