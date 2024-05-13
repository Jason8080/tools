package cn.gmlee.tools.base.util;

import cn.gmlee.tools.base.mod.Kv;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
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
import java.util.Optional;

/**
 * jSqlParser脚本生成工具.
 */
@Slf4j
public class SqlUtil {

    private static DataType DATA_TYPE = DataType.MYSQL;


    /**
     * 重置数据库类型
     *
     * @param dataType
     */
    public static void reset(DataType dataType) {
        if (DATA_TYPE.equals(dataType)) {
            return;
        }
        DATA_TYPE = dataType;
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
     * <p>
     * 注意: 该API会给所有表添加wheres筛选条件
     * </p>
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
        sqlHandler((PlainSelect) selectBody, wheres);
        // 获取新语句
        String newSql = statement.toString();
        return newSql.equals(oldSql) ? originSql : newSql;
    }

    private static void sqlHandler(PlainSelect plainSelect, Map<String, List<Expression>> wheres) throws Exception {
        // 关联句柄处理
        join(plainSelect.getJoins(), wheres);
        // 子句递归处理
        subSelect(plainSelect.getFromItem(), wheres);
        // 条件句柄处理
        addWheres(plainSelect, wheres);
    }

    private static void subSelect(FromItem item, Map<String, List<Expression>> wheres) throws Exception {
        if (item instanceof SubSelect) {
            sqlHandler((PlainSelect) ((SubSelect) item).getSelectBody(), wheres);
        }
    }

    private static void join(List<Join> joins, Map<String, List<Expression>> wheres) throws Exception {
        if (BoolUtil.isEmpty(joins)) {
            return;
        }
        for (Join join : joins) {
            FromItem item = join.getRightItem();
            if (item instanceof Table) {
                addWheres(join, wheres);
            }
            if (item instanceof SubSelect) {
                subSelect(item, wheres);
            }
        }
    }

    private static void addWheres(PlainSelect plainSelect, Map<String, List<Expression>> wheres) {
        wheres.forEach((k, v) -> QuickUtil.isTrue(BoolUtil.notEmpty(k), () -> addWhere(plainSelect, k, v)));
    }

    private static void addWhere(PlainSelect plainSelect, String key, List<Expression> values) {
        // 仅加表句柄
        if(!(plainSelect.getFromItem() instanceof Table)){
            return;
        }
        // 构建返回列
        Column column = getColumn(plainSelect.getFromItem(), key);
        // 添加条件值
        Expression expression = getExpression(values, column);
        AndExpression and = new AndExpression()
                .withLeftExpression(plainSelect.getWhere())
                .withRightExpression(expression);
        // 设置条件
        plainSelect.setWhere(plainSelect.getWhere() != null ? and : expression);
    }

    private static boolean containsColumn(PlainSelect plainSelect, Column column) {
        // 检查是否存在当前列
        List<SelectItem> selectItems = plainSelect.getSelectItems();
        Optional<SelectItem> optional = selectItems.stream().filter(x -> {
            if (!(x instanceof SelectExpressionItem)) {
                return false;
            }
            Expression expression = ((SelectExpressionItem) x).getExpression();
            if (!(expression instanceof Column)) {
                return false;
            }
            return column.getColumnName().equalsIgnoreCase(((Column) expression).getColumnName());
        }).findAny();
        return optional.isPresent();
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
        String name = String.format("%s%s%s", getColumnQuoteSymbol(), field, getColumnQuoteSymbol());
        if (item.getAlias() == null) {
            return new Column(name);
        }
        String alias = NullUtil.get(item.getAlias().getName(), item.toString());
        // 考虑 oracle 的兼容性, table 暂不添加引用符 (开发者需注意别名不允许是数据库关键字)
        return new Column(String.format("%s.%s", alias, name));
    }

    private static String getColumnQuoteSymbol() {
        switch (DATA_TYPE) {
            case MYSQL:
                return "`";
            case ORACLE:
                return "\"";
        }
        return ExceptionUtil.cast("非法的数据源类型");
    }

    private static void addColumns(PlainSelect plainSelect, Map<String, List<Expression>> wheres) {
        wheres.forEach((k, v) -> QuickUtil.isTrue(BoolUtil.notEmpty(k), () -> addColumn(plainSelect, k, v)));
    }

    private static void addGroups(PlainSelect plainSelect, Map<String, List<Expression>> wheres) {
        // MySql不需要添加分组列
        if (DATA_TYPE.equals(DataType.MYSQL)) {
            return;
        }
        // Oracle存在聚合函数 或则 本身就存在分组 才需要添加分组列
        if (plainSelect.getGroupBy() == null && !containsAggregationFunction(plainSelect)) {
            return;
        }
        wheres.forEach((k, v) -> QuickUtil.isTrue(BoolUtil.notEmpty(k), () -> addGroup(plainSelect, k, v)));
    }

    private static boolean containsAggregationFunction(PlainSelect plainSelect) {
        List<SelectItem> selectItems = plainSelect.getSelectItems();
        Optional<SelectItem> optional = selectItems.stream().filter(x -> {
            if (!(x instanceof SelectExpressionItem)) {
                return false;
            }
            Expression expression = ((SelectExpressionItem) x).getExpression();
            return containsAggregationFunction(expression);
        }).findAny();
        return optional.isPresent();
    }

    private static boolean containsAggregationFunction(Expression expression) {
        if (!(expression instanceof Function)) {
            return false;
        }
        // 判断函数是否为聚合函数的方法
        return isFunctionAggregate((Function) expression);
    }

    private static boolean isFunctionAggregate(Function function) {
        String name = function.getName().toUpperCase(); // 将函数名转换为大写以便比较
        if (isFunctionAggregate(name)) {
            return true;
        }
        ExpressionList parameters = function.getParameters();
        if (parameters == null) {
            return false;
        }
        List<Expression> expressions = parameters.getExpressions();
        if (expressions == null) {
            return false;
        }
        return expressions.stream().filter(x -> containsAggregationFunction(x)).findAny().isPresent();
    }

    private static boolean isFunctionAggregate(String name) {
        return name.equals("COUNT") || name.equals("SUM") ||
                name.equals("AVG") || name.equals("MIN") ||
                name.equals("MAX");
    }


    private static void addColumn(PlainSelect plainSelect, String key, List<Expression> values) {
        List<SelectItem> selectItems = plainSelect.getSelectItems();
        // 如果是* 或 tab.* 标识包含所有字段, 这种情况不需要追加返回列 (否则会有重复列异常)
        Optional<SelectItem> optional = selectItems.stream().filter(x -> x instanceof AllColumns || x instanceof AllTableColumns).findAny();
        // 如果包含所有字段则不追key字段
        if (optional.isPresent()) {
            return;
        }
        // 添加返回列
        Column column = getColumn(plainSelect.getFromItem(), key);
        selectItems.add(new SelectExpressionItem(column));
    }

    private static void addGroup(PlainSelect plainSelect, String key, List<Expression> values) {
        // 构建返回列
        Column column = getColumn(plainSelect.getFromItem(), key);
        // 包含该列才能添加分组
        if (containsColumn(plainSelect, column)) {
            plainSelect.addGroupByColumnReference(column);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * 数据源类型
     */
    public enum DataType {
        MYSQL,
        ORACLE,
        ;

        public static DataType of(String dataType) {
            DataType[] values = DataType.values();
            for (DataType dt : values) {
                if (dt.name().equalsIgnoreCase(dataType)) {
                    return dt;
                }
            }
            return ExceptionUtil.cast(String.format("不支持的数据源类型: %s", dataType));
        }
    }
}
