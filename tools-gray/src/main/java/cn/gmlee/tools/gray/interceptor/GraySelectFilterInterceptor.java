package cn.gmlee.tools.gray.interceptor;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.QuickUtil;
import cn.gmlee.tools.base.util.RegexUtil;
import cn.gmlee.tools.gray.assist.SqlAssist;
import cn.gmlee.tools.gray.conf.GrayProperties;
import cn.gmlee.tools.gray.helper.GrayHelper;
import cn.gmlee.tools.gray.initializer.GrayDataTemplate;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Alias;
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
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Connection;
import java.sql.SQLException;
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

    @Autowired
    private GrayDataTemplate grayDataTemplate;

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
        return sqlHandler((Select) statement);
    }

    private String sqlHandler(Select statement) throws Exception {
        // 获取条件列
        foreach((PlainSelect) statement.getSelectBody());
        return statement.toString();
    }

    private void foreach(PlainSelect plainSelect) throws Exception {
        if (plainSelect.getSelectItems().size() == 1) {
            String countRegex = "COUNT\\([\\s]?(\\*|\\d)[\\s]?\\)";
            SelectItem selectItem = plainSelect.getSelectItems().get(0);
            List<String> all = RegexUtil.find(selectItem.toString(), countRegex);
            if (BoolUtil.notEmpty(all)) {
                addWheres(plainSelect);
                return;
            }
        }
        FromItem item = plainSelect.getFromItem();
        if (item instanceof SubSelect) {
            foreach((PlainSelect) ((SubSelect) item).getSelectBody());
        }
        addColumns(plainSelect);
        addWheres(plainSelect);
    }

    private void addWheres(PlainSelect plainSelect) throws SQLException {
        // 获取别名值
        Column column = getColumn(plainSelect);
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

    private Column getColumn(PlainSelect plainSelect) throws SQLException {
        FromItem item = plainSelect.getFromItem();
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
        Column column = getColumn(plainSelect);
        selectItems.add(new SelectExpressionItem(column));
    }
}
