package cn.gmlee.tools.profile.interceptor;

import cn.gmlee.tools.profile.assist.SqlAssist;
import cn.gmlee.tools.profile.conf.ProfileProperties;
import cn.gmlee.tools.profile.helper.ProfileHelper;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;

import java.sql.Connection;

/**
 * 环境数据标记拦截器.
 */
@Slf4j
@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}),
})
public class ProfileInsertMarkInterceptor implements Interceptor {

    private final ProfileProperties properties;

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
            log.error("数据环境标记失败", throwable);
        }
        return invocation.proceed();
    }

    private void mark(Invocation invocation) throws Exception {
        // 关闭的不处理
        if (ProfileHelper.closed()) {
            return;
        }
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        BoundSql boundSql = statementHandler.getBoundSql();
        String originSql = boundSql.getSql();
        Statement statement = CCJSqlParserUtil.parse(originSql);
        // 非插入句柄不处理
        if (!(statement instanceof Insert)) {
            return;
        }
        String newSql = sqlHandler((Insert) statement);
        SqlAssist.reset(boundSql, newSql);
    }

    private String sqlHandler(Insert statement) {
        Column column = new Column("\"" + properties.getEvn() + "\"");
        statement.addColumns(column);
        ExpressionList expressionList = (ExpressionList) statement.getItemsList();
        expressionList.getExpressions().add(new LongValue(0));
        return statement.toString();
    }
}
