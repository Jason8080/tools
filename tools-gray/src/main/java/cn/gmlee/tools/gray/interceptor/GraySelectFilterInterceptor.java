package cn.gmlee.tools.gray.interceptor;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.QuickUtil;
import cn.gmlee.tools.base.util.RegexUtil;
import cn.gmlee.tools.gray.assist.SqlAssist;
import cn.gmlee.tools.gray.conf.GrayProperties;
import cn.gmlee.tools.gray.helper.GrayHelper;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;

import java.sql.Connection;
import java.util.List;

/**
 * 灰度环境数据标记拦截器.
 */
@Slf4j
@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}),
})
public class GraySelectFilterInterceptor implements Interceptor {

    private final GrayProperties properties;

    public GraySelectFilterInterceptor(GrayProperties properties) {
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
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        BoundSql boundSql = statementHandler.getBoundSql();
        String originSql = boundSql.getSql();
        String newSql = getNewSql(originSql);
        if(originSql.equalsIgnoreCase(newSql)){
            return;
        }
        SqlAssist.reset(boundSql, newSql);
    }

    private String getNewSql(String originSql) throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(originSql);
        // 非插入句柄不处理
        if (!(statement instanceof Select)) {
            return originSql;
        }
        return sqlHandler((Select) statement);
    }

    private String sqlHandler(Select statement) {
        // 获取条件列
        Column column = new Column("\"" + properties.getEvn() + "\"");
        foreach((PlainSelect) statement.getSelectBody(), column);
        return statement.toString();
    }

    private void foreach(PlainSelect plainSelect, Column column) {
        if (plainSelect.getSelectItems().size() == 1) {
            String countRegex = "COUNT\\([\\s]?(\\*|\\d)[\\s]?\\)";
            SelectItem selectItem = plainSelect.getSelectItems().get(0);
            List<String> all = RegexUtil.find(selectItem.toString(), countRegex);
            if (BoolUtil.notEmpty(all)) {
                addWheres(plainSelect, column);
                return;
            }
        }
        FromItem subItem = plainSelect.getFromItem();
        if (subItem instanceof SubSelect) {
            foreach((PlainSelect) ((SubSelect) subItem).getSelectBody(), column);
        }
        addColumns(plainSelect, column);
        addWheres(plainSelect, column);
    }

    private static void addWheres(PlainSelect plainSelect, Column column) {
        // 添加条件值
        InExpression in = new InExpression();
        in.setLeftExpression(column);
        ExpressionList expressionList = new ExpressionList();
        in.setRightItemsList(expressionList);
        AndExpression and = new AndExpression(plainSelect.getWhere(), in);
        // 添加条件列
        QuickUtil.isTrue(GrayHelper.enable(), () -> expressionList.addExpressions(new LongValue(0)));
        expressionList.addExpressions(new LongValue(1));
        plainSelect.setWhere(and);
    }

    private static void addColumns(PlainSelect plainSelect, Column column) {
        List<SelectItem> selectItems = plainSelect.getSelectItems();
        for (SelectItem item : selectItems){
            String first = RegexUtil.first(item.toString(), "[\\w\\.]?[\\*]+");
            if(BoolUtil.notEmpty(first)){
                return;
            }
        }
        // 添加返回列
        selectItems.add(new SelectExpressionItem(column));
    }
}
