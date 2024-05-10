package cn.gmlee.tools.base.util;

import cn.gmlee.tools.base.mod.Kv;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.*;

import java.util.List;
import java.util.Map;

/**
 * jSqlParser脚本生成工具.
 */
@Slf4j
public class SqlUtil {

    private static String COLUMN_QUOTE_SYMBOL = "\"";

    /**
     * 重置列引用符.
     *
     * @param s 新符号
     * @return string 旧符号
     */
    public static String resetColumnQuoteSymbol(String s) {
        // 没有变化不处理
        if (COLUMN_QUOTE_SYMBOL.equals(s)) {
            return COLUMN_QUOTE_SYMBOL;
        }
        // 变化后返回原符
        String old = COLUMN_QUOTE_SYMBOL;
        COLUMN_QUOTE_SYMBOL = s;
        return old;
    }

    /**
     * New insert sql.
     *
     * @param originSql the origin sql
     * @param kvs       the kvs
     * @return the string
     * @throws Exception the exception
     */
    public static String newInsert(String originSql, Kv<String, Expression>... kvs) throws Exception {
        // 非空条件方才处理
        if (BoolUtil.isEmpty(kvs)) {
            return originSql;
        }
        Statement statement = CCJSqlParserUtil.parse(originSql);
        String oldSql = statement.toString();
        // 非插入句柄不处理
        if (!(statement instanceof Insert)) {
            return originSql;
        }
        // 句柄处理器
        sqlHandler((Insert) statement, kvs);
        // 获取新语句
        String newSql = statement.toString();
        return newSql.equals(oldSql) ? originSql : newSql;
    }

    private static void sqlHandler(Insert statement, Kv<String, Expression>... kvs) {
        for (Kv<String, Expression> kv : kvs) {
            Column column = getColumn(statement.getTable(), kv.getKey());
            statement.addColumns(column);
            ((ExpressionList) statement.getItemsList()).addExpressions(kv.getVal());
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * New select sql.
     *
     * @param originSql the origin sql
     * @param wheres    the wheres
     * @return the new sql
     * @throws Exception the exception
     */
    public static String newSelect(String originSql, Map<String, List<Expression>> wheres) throws Exception {
        // 非空条件方才处理
        if (BoolUtil.isEmpty(wheres)) {
            return originSql;
        }
        Statement statement = CCJSqlParserUtil.parse(originSql);
        String oldSql = statement.toString();
        // 非查询句柄不处理
        if (!(statement instanceof Select)) {
            return originSql;
        }
        SelectBody selectBody = ((Select) statement).getSelectBody();
        // 非普通句柄不处理
        if (!(selectBody instanceof PlainSelect)) {
            return originSql;
        }
        // 句柄处理器
        sqlHandler((PlainSelect) selectBody, wheres, true);
        // 获取新语句
        String newSql = statement.toString();
        return newSql.equals(oldSql) ? originSql : newSql;
    }

    private static void sqlHandler(PlainSelect plainSelect, Map<String, List<Expression>> wheres, boolean first) throws Exception {
        // 关联句柄处理
        join(plainSelect.getJoins(), wheres);
        // 子句递归处理
        subSelect(plainSelect.getFromItem(), wheres);
        // 列值句柄处理
        QuickUtil.isFalse(first, () -> addColumns(plainSelect, wheres));
        // 条件句柄处理
        addWheres(plainSelect, wheres);
    }

    private static void subSelect(FromItem item, Map<String, List<Expression>> wheres) throws Exception {
        if (item instanceof SubSelect) {
            sqlHandler((PlainSelect) ((SubSelect) item).getSelectBody(), wheres, false);
        }
    }

    private static void join(List<Join> joins, Map<String, List<Expression>> wheres) {
        if (BoolUtil.isEmpty(joins)) {
            return;
        }
        for (Join join : joins) {
            FromItem item = join.getRightItem();
            if (item instanceof Table) {
                addWheres(join, wheres);
            }
        }
    }

    private static void addWheres(PlainSelect plainSelect, Map<String, List<Expression>> wheres) {
        wheres.forEach((k, v) -> QuickUtil.isTrue(BoolUtil.notEmpty(k), () -> addWhere(plainSelect, k, v)));
    }

    private static void addWhere(PlainSelect plainSelect, String key, List<Expression> values) {
        // 获取别名值
        Column column = getColumn(plainSelect.getFromItem(), key);
        // 添加条件值
        Expression expression = getExpression(values, column);
        AndExpression and = new AndExpression()
                .withLeftExpression(plainSelect.getWhere())
                .withRightExpression(expression);
        // 设置条件
        plainSelect.setWhere(plainSelect.getWhere() != null ? and : expression);
    }

    private static Expression getExpression(List<Expression> values, Column column) {
        // 构建条件
        Expression e = BoolUtil.notEmpty(values) ? new InExpression() : new IsNullExpression();
        QuickUtil.isTrue(e instanceof InExpression, () -> ((InExpression) e).setLeftExpression(column));
        QuickUtil.isTrue(e instanceof IsNullExpression, () -> ((IsNullExpression) e).setLeftExpression(column));
        ExpressionList expressionList = new ExpressionList();
        QuickUtil.isTrue(e instanceof InExpression, () -> ((InExpression) e).setRightItemsList(expressionList));
        // 添加条件
        expressionList.withExpressions(values);
        return e;
    }

    private static void addWheres(Join join, Map<String, List<Expression>> wheres) {
        wheres.forEach((k, v) -> QuickUtil.isTrue(BoolUtil.notEmpty(k), () -> addWhere(join, k, v)));
    }

    private static void addWhere(Join join, String key, List<Expression> values) {
        // 获取别名值
        Column column = getColumn(join.getRightItem(), key);
        // 构建条件
        Expression expression = getExpression(values, column);
        // 设置条件
        AndExpression and = new AndExpression()
                .withLeftExpression(join.getOnExpression())
                .withRightExpression(expression);
        join.setOnExpression(and);
    }

    private static Column getColumn(FromItem item, String field) {
        String name = String.format("%s%s%s", COLUMN_QUOTE_SYMBOL, field, COLUMN_QUOTE_SYMBOL);
        if (item instanceof SubSelect){
            return new Column(name);
        }
        String alias = NullUtil.get(() -> item.getAlias().getName(), item.toString());
        // 考虑 oracle 的兼容性, table 暂不添加引用符 (开发者需注意别名不允许是数据库关键字)
        return new Column(String.format("%s.%s", alias, name));
    }

    private static void addColumns(PlainSelect plainSelect, Map<String, List<Expression>> wheres) {
        wheres.forEach((k, v) -> QuickUtil.isTrue(BoolUtil.notEmpty(k), () -> addColumn(plainSelect, k, v)));
    }


    private static void addColumn(PlainSelect plainSelect, String key, List<Expression> values) {
        List<SelectItem> selectItems = plainSelect.getSelectItems();
        for (SelectItem item : selectItems) {
            // tab.* 不需要添加返回列
            String first = RegexUtil.first(item.toString(), "[\\w\\.]?[\\*]+");
            if (BoolUtil.notEmpty(first)) {
                return;
            }
        }
        // 添加返回列
        Column column = getColumn(plainSelect.getFromItem(), key);
        selectItems.add(new SelectExpressionItem(column));
    }
}
