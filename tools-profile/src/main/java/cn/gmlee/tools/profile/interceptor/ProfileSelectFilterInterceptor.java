package cn.gmlee.tools.profile.interceptor;

import cn.gmlee.tools.base.enums.Int;
import cn.gmlee.tools.base.util.QuickUtil;
import cn.gmlee.tools.base.util.SqlUtil;
import cn.gmlee.tools.profile.assist.FutureAssist;
import cn.gmlee.tools.profile.assist.SqlAssist;
import cn.gmlee.tools.profile.conf.ProfileProperties;
import cn.gmlee.tools.profile.helper.ProfileHelper;
import cn.gmlee.tools.profile.initializer.GrayDataTemplate;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据环境选择拦截器.
 */
@Slf4j
@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}),
})
public class ProfileSelectFilterInterceptor implements Interceptor {

    private final ProfileProperties properties;

    @Autowired
    private GrayDataTemplate grayDataTemplate;

    public ProfileSelectFilterInterceptor(ProfileProperties properties) {
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
            log.error("数据环境过滤失败", throwable);
        }
        return invocation.proceed();
    }

    private void filter(Invocation invocation) throws Exception {
        // 关闭的不处理
        if (ProfileHelper.closed()) {
            return;
        }
        // 非灰度环境不处理
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        BoundSql boundSql = statementHandler.getBoundSql();
        String originSql = boundSql.getSql();
        // 构建环境筛选条件
        Map<String, List<Expression>> wheres = new HashMap<>(Int.ONE);
        List<Expression> envs = new ArrayList<>(Int.TWO);
        QuickUtil.isTrue(ProfileHelper.enabled(),
                () -> envs.add(new LongValue(Int.ZERO)) // 开启后查测试数据
        );
        envs.add(new LongValue(Int.ONE)); // 保证查看正式数据
        wheres.put(properties.getField(), envs);
        // 构建新的筛选句柄
//        String symbol = FutureAssist.supplyAsync(grayDataTemplate::getColumnQuoteSymbol);
        SqlUtil.resetColumnQuoteSymbol(grayDataTemplate.getColumnQuoteSymbol());
        String newSql = SqlUtil.newSelect(originSql, wheres);
        SqlAssist.reset(boundSql, newSql);
    }
}
