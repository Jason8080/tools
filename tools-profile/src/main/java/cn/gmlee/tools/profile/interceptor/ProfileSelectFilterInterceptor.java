package cn.gmlee.tools.profile.interceptor;

import cn.gmlee.tools.base.enums.Int;
import cn.gmlee.tools.base.util.QuickUtil;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据隔离选择拦截器.
 */
@Slf4j
@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}),
})
public class ProfileSelectFilterInterceptor implements Interceptor {

    @Autowired
    private ProfileDataTemplate profileDataTemplate;

    @Autowired
    private List<ProfileProperties> profilePropertiesList;

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
            // 过滤失败不处理
            log.error("数据隔离过滤失败", throwable);
        }
        return invocation.proceed();
    }

    private void filter(Invocation invocation) throws Exception {
        ProfileProperties profileProperties = profilePropertiesList.get(0);
        // 关闭的不处理
        if (!profileProperties.getOpen()) {
            return;
        }
        // 非灰度环境不处理
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        BoundSql boundSql = statementHandler.getBoundSql();
        String originSql = boundSql.getSql();
        // 构建环境筛选条件
        Map<String, List> wheres = new HashMap<>(Int.ONE);
        Set<ProfileHelper.Env> set = ProfileHelper.get(ProfileHelper.ReadWrite.READ);
        List<Integer> envs = set.stream().map(x -> x.value).collect(Collectors.toList());
        // 保证至少有1个环境: 默认生产
        QuickUtil.isTrue(envs.isEmpty(), () -> envs.add(ProfileHelper.Env.PRD.value));
        wheres.put(profileProperties.getField(), envs);
        // 构建新的筛选句柄
        SqlUtil.reset(SqlUtil.DataType.of(profileDataTemplate.getDatabaseProductName()));
        String newSql = SqlUtil.newSelect(originSql, wheres);
        SqlAssist.reset(boundSql, newSql, profileProperties.getPrint());
    }
}
