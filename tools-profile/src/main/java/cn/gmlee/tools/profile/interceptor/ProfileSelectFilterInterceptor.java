package cn.gmlee.tools.profile.interceptor;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.QuickUtil;
import cn.gmlee.tools.base.util.RegexUtil;
import cn.gmlee.tools.profile.assist.SqlAssist;
import cn.gmlee.tools.profile.conf.ProfileProperties;
import cn.gmlee.tools.profile.helper.ProfileHelper;
import cn.gmlee.tools.profile.initializer.GrayDataTemplate;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

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
        // 非灰度环境不处理
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        BoundSql boundSql = statementHandler.getBoundSql();
        String originSql = boundSql.getSql();
        String newSql = getNewSql(originSql);
        if (originSql.equalsIgnoreCase(newSql)) {
            return;
        }
        SqlAssist.reset(boundSql, newSql);
    }

    public String getNewSql(String originSql) throws Exception {
        Statement statement = CCJSqlParserUtil.parse(originSql);
        // 非插入句柄不处理
        if (!(statement instanceof Select)) {
            return originSql;
        }
        SelectBody selectBody = ((Select) statement).getSelectBody();
        // 非普通句柄不处理
        if (!(selectBody instanceof PlainSelect)){
            return originSql;
        }
        // 句柄处理器
        sqlHandler((PlainSelect) selectBody);
        return statement.toString();
    }

    private void sqlHandler(PlainSelect plainSelect) throws Exception {
        // 关联句柄处理
        join(plainSelect.getJoins());
        // 子句递归处理
        subSelect(plainSelect.getFromItem());
        // 列值句柄处理
        addColumns(plainSelect);
        // 条件句柄处理
        addWheres(plainSelect);
    }

    private void subSelect(FromItem item) throws Exception {
        if (item instanceof SubSelect) {
            sqlHandler((PlainSelect) ((SubSelect) item).getSelectBody());
        }
    }

    private void join(List<Join> joins) throws Exception {
        if(BoolUtil.isEmpty(joins)){
            return;
        }
        for (Join join : joins){
            FromItem item = join.getRightItem();
            if(item instanceof Table){
                addWheres(join);
            }
        }
    }

    private void addWheres(PlainSelect plainSelect) throws SQLException {
        // 获取别名值
        Column column = getColumn(plainSelect.getFromItem());
        // 添加条件值
        InExpression in = new InExpression();
        in.setLeftExpression(column);
        ExpressionList expressionList = new ExpressionList();
        in.setRightItemsList(expressionList);
        AndExpression and = new AndExpression(plainSelect.getWhere(), in);
        // 添加条件列
        QuickUtil.isTrue(ProfileHelper.enable(), () -> expressionList.addExpressions(new LongValue(0)));
        expressionList.addExpressions(new LongValue(1));
        plainSelect.setWhere(and);
    }

    private void addWheres(Join join) throws SQLException {
        // 获取别名值
        Column column = getColumn(join.getRightItem());
        // 添加In条件
        InExpression in = new InExpression();
        in.setLeftExpression(column);
        // 添加In值集
        ExpressionList expressionList = new ExpressionList();
        in.setRightItemsList(expressionList);
        QuickUtil.isTrue(ProfileHelper.enable(), () -> expressionList.addExpressions(new LongValue(0)));
        expressionList.addExpressions(new LongValue(1));
        // 高版本升级
//        List<Expression> es = join.getOnExpressions().stream().map(x -> new AndExpression(x, in)).collect(Collectors.toList());
//        join.setOnExpressions(es);
        AndExpression and = new AndExpression(join.getOnExpression(), in);
        join.setOnExpression(and);
    }

    private Column getColumn(FromItem item) throws SQLException {
        Alias alias = item.getAlias();
        String name = String.format("%s.%s%s%s",
                alias.getName(),
//                    "\"",
                grayDataTemplate.getColumnSymbol(),
                properties.getEvn(),
                grayDataTemplate.getColumnSymbol()
//                "\""

        );
        return new Column(name);
    }

    private void addColumns(PlainSelect plainSelect) throws SQLException {
        List<SelectItem> selectItems = plainSelect.getSelectItems();
        for (SelectItem item : selectItems) {
            String first = RegexUtil.first(item.toString(), "[\\w\\.]?[\\*]+");
            if (BoolUtil.notEmpty(first)) {
                return;
            }
        }
        // 添加返回列
        Column column = getColumn(plainSelect.getFromItem());
        selectItems.add(new SelectExpressionItem(column));
    }
}
