package cn.gmlee.tools.base.util;

import cn.gmlee.tools.base.assist.ExpressionAssist;
import cn.gmlee.tools.base.mod.Kv;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.OracleHierarchicalExpression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.*;

import java.util.*;
import java.util.stream.Collectors;

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
    public static String newInsert(String originSql, Kv<String, ? extends Comparable>... kvs) throws Exception {
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

    private static void sqlHandler(Insert statement, Kv<String, ?>... kvs) {
        for (Kv<String, ?> kv : kvs) {
            Column column = getColumn(statement.getTable(), kv.getKey());
            statement.addColumns(column);
            ((ExpressionList) statement.getItemsList()).addExpressions(ExpressionAssist.as(kv.getVal()));
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
    public static String newSelect(String originSql, Map<String, List> wheres) throws Exception {
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
        List<WithItem> withItemsList = ((Select) statement).getWithItemsList();
        // 句柄处理器
        sqlHandler(selectBody, withItemsList, wheres);
        // 获取新语句
        String newSql = statement.toString();
        return newSql.equals(oldSql) ? originSql : newSql;
    }

    private static void sqlHandler(SelectBody selectBody, List<WithItem> withItemsList, Map<String, List> wheres) {
        // 获取临时表
        Set<String> virtualTables = getVirtualTables(withItemsList);
        // 处理查询体
        selectBodyHandler(selectBody, virtualTables, wheres);
        // 处理临时表
        withItemsListHandler(withItemsList, virtualTables, wheres);
    }

    private static void selectBodiesHandler(List<SelectBody> selectBodies, Set<String> virtualTables, Map<String, List> wheres) {
        if (selectBodies == null) {
            return;
        }
        for (SelectBody selectBody : selectBodies) {
            // 处理查询体
            selectBodyHandler(selectBody, virtualTables, wheres);
        }
    }

    private static void selectBodyHandler(SelectBody selectBody, Set<String> virtualTables, Map<String, List> wheres) {
        if (selectBody == null) {
            return;
        }
        selectBody.accept(new SelectVisitorAdapter() {
            @Override
            public void visit(PlainSelect plainSelect) {
                //------------------------------------------------------------------------------------------------------
                // 嵌套子查询
                fromItemHandler(plainSelect.getFromItem(), virtualTables, wheres);
                // 关联子查询
                joinsHandler(plainSelect.getJoins(), virtualTables, wheres);
                // 字段子查询
                selectItemsHandler(plainSelect.getSelectItems(), virtualTables, wheres);
                // 条件子查询
                whereHandler(plainSelect.getWhere(), virtualTables, wheres);
                // 层级子查询
                oracleHierarchicalHandler(plainSelect.getOracleHierarchical(), virtualTables, wheres);
                //------------------------------------------------------------------------------------------------------
                addWheres(plainSelect, virtualTables, wheres); // 构建条件
                //------------------------------------------------------------------------------------------------------
            }

            @Override
            public void visit(SetOperationList setOpList) {
                List<SelectBody> selects = setOpList.getSelects();
                selectBodiesHandler(selects, virtualTables, wheres);
            }

            @Override
            public void visit(WithItem withItem) {
                // 处理查询体
                selectBodyHandler(withItem.getSelectBody(), virtualTables, wheres);
                // 字段子查询
                selectItemsHandler(withItem.getWithItemList(), virtualTables, wheres);
            }
        });
    }

    private static void withItemsListHandler(List<WithItem> withItemsList, Set<String> virtualTables, Map<String, List> wheres) {
        if (withItemsList == null) {
            return;
        }
        for (WithItem withItem : withItemsList) {
            // 处理查询体
            selectBodyHandler(withItem.getSelectBody(), virtualTables, wheres);
            // 字段子查询
            selectItemsHandler(withItem.getWithItemList(), virtualTables, wheres);
        }
    }

    private static void fromItemHandler(FromItem fromItem, Set<String> virtualTables, Map<String, List> wheres) {
        if (fromItem == null) {
            return;
        }
        fromItem.accept(new FromItemVisitorAdapter() {
            @Override
            public void visit(SubSelect subSelect) {
                // 处理查询体
                selectBodyHandler(subSelect.getSelectBody(), virtualTables, wheres);
            }
        });
    }

    private static void joinsHandler(List<Join> joins, Set<String> virtualTables, Map<String, List> wheres) {
        if (joins == null) {
            return;
        }
        for (Join join : joins) {
            //------------------------------------------------------------------------------------------------------
            // 嵌套子查询
            fromItemHandler(join.getRightItem(), virtualTables, wheres);
            // 条件子查询
            whereHandler(join.getOnExpression(), virtualTables, wheres);
            //------------------------------------------------------------------------------------------------------
            addWheres(join, virtualTables, wheres); // 构建条件
            //------------------------------------------------------------------------------------------------------
        }
    }

    private static void selectItemsHandler(List<SelectItem> selectItems, Set<String> virtualTables, Map<String, List> wheres) {
        if (selectItems == null) {
            return;
        }
        for (SelectItem selectItem : selectItems) {
            // 字段子查询
            selectItemHandler(selectItem, virtualTables, wheres);
        }
    }

    private static void selectItemHandler(SelectItem selectItem, Set<String> virtualTables, Map<String, List> wheres) {
        if (selectItem == null) {
            return;
        }
        // 嵌套子查询
        selectItem.accept(new SelectItemVisitorAdapter() {
            @Override
            public void visit(SelectExpressionItem item) {
                // 条件子查询
                whereHandler(item.getExpression(), virtualTables, wheres);
            }
        });
    }

    private static void wheresHandler(List<Expression> expressions, Set<String> virtualTables, Map<String, List> wheres) {
        if (expressions == null) {
            return;
        }
        for (Expression expression : expressions) {
            // 条件子查询
            whereHandler(expression, virtualTables, wheres);
        }
    }

    private static void whereHandler(Expression expression, Set<String> virtualTables, Map<String, List> wheres) {
        if (expression == null) {
            return;
        }
        if (expression instanceof SubSelect) {
            // 嵌套子查询
            fromItemHandler((FromItem) expression, virtualTables, wheres);
            return;
        }
        if (expression instanceof Function) {
            // 函数子查询
            expressionListHandler(((Function) expression).getParameters(), virtualTables, wheres);
            return;
        }
        // 深度处理
        ItemsList leftItemsList = ClassUtil.getValue(expression, "leftItemsList");
        itemsListHandler(leftItemsList, virtualTables, wheres);
        ItemsList rightItemsList = ClassUtil.getValue(expression, "rightItemsList");
        itemsListHandler(rightItemsList, virtualTables, wheres);
        // 深度处理
        Expression leftExpression = ClassUtil.getValue(expression, "leftExpression");
        whereHandler(leftExpression, virtualTables, wheres);
        Expression rightExpression = ClassUtil.getValue(expression, "rightExpression");
        whereHandler(rightExpression, virtualTables, wheres);
    }

    private static void expressionListHandler(ExpressionList expressionList, Set<String> virtualTables, Map<String, List> wheres) {
        if (expressionList == null) {
            return;
        }
        List<Expression> expressions = expressionList.getExpressions();
        // 条件子查询
        wheresHandler(expressions, virtualTables, wheres);
    }

    private static void oracleHierarchicalHandler(OracleHierarchicalExpression ohExpression, Set<String> virtualTables, Map<String, List> wheres) {
        if (!DATA_TYPE.equals(DataType.ORACLE)) {
            return;
        }
        if (ohExpression == null) {
            return;
        }
        // 条件子查询
        whereHandler(ohExpression.getStartExpression(), virtualTables, wheres);
        // 条件子查询
        whereHandler(ohExpression.getConnectExpression(), virtualTables, wheres);
    }

    private static void itemsListHandler(ItemsList itemsList, Set<String> virtualTables, Map<String, List> wheres) {
        if (itemsList == null) {
            return;
        }
        itemsList.accept(new ItemsListVisitorAdapter() {
            @Override
            public void visit(SubSelect subSelect) {
                // 嵌套子查询
                fromItemHandler(subSelect, virtualTables, wheres);
            }

            @Override
            public void visit(ExpressionList expressionList) {
                super.visit(expressionList);
            }
        });
    }

    // -----------------------------------------------------------------------------------------------------------------

    private static void addWheres(Join join, Set<String> virtualTables, Map<String, List> wheres) {
        wheres.forEach((k, v) -> QuickUtil.isTrue(BoolUtil.notEmpty(k), () -> addWhere(join, virtualTables, k, v)));
    }

    private static void addWhere(Join join, Set<String> virtualTables, String key, List<? extends Comparable> values) {
        FromItem fromItem = join.getRightItem();
        // 如果不是表
        if (!(fromItem instanceof Table)) {
            return;
        }
        // 还是虚拟表
        if (isVirtualTable((Table) fromItem, virtualTables)) {
            return;
        }
        // 获取别名值
        Column column = getColumn(fromItem, key);
        // 构建条件
        Expression expression = getExpression(values, column);
        // 设置条件
        AndExpression and = new AndExpression()
                .withLeftExpression(join.getOnExpression())
                .withRightExpression(expression);
        join.setOnExpression(and);
    }

    private static void addWheres(PlainSelect plainSelect, Set<String> virtualTables, Map<String, List> wheres) {
        wheres.forEach((k, v) -> QuickUtil.isTrue(BoolUtil.notEmpty(k), () -> addWhere(plainSelect, virtualTables, k, v)));
    }

    private static void addWhere(PlainSelect plainSelect, Set<String> virtualTables, String key, List<? extends Comparable> values) {
        FromItem fromItem = plainSelect.getFromItem();
        // 如果不是表
        if (!(fromItem instanceof Table)) {
            return;
        }
        // 还是虚拟表
        if (isVirtualTable((Table) fromItem, virtualTables)) {
            return;
        }
        // 构建返回列
        Column column = getColumn(fromItem, key);
        // 添加条件值
        Expression expression = getExpression(values, column);
        AndExpression and = new AndExpression()
                .withLeftExpression(plainSelect.getWhere())
                .withRightExpression(expression);
        // 设置条件
        plainSelect.setWhere(plainSelect.getWhere() != null ? and : expression);
    }

    private static Expression getExpression(List<? extends Comparable> values, Column column) {
        // 构建条件
        Expression e = BoolUtil.notEmpty(values) ? new InExpression() : new IsNullExpression();
        QuickUtil.isTrue(e instanceof InExpression, () -> ((InExpression) e).setLeftExpression(column));
        QuickUtil.isTrue(e instanceof IsNullExpression, () -> ((IsNullExpression) e).setLeftExpression(column));
        ExpressionList expressionList = new ExpressionList();
        QuickUtil.isTrue(e instanceof InExpression, () -> ((InExpression) e).setRightItemsList(expressionList));
        // 添加条件
        expressionList.withExpressions(ExpressionAssist.as(values));
        return e;
    }

    private static Column getColumn(FromItem item, String field) {
        String name = String.format("%s%s%s", getColumnQuoteSymbol(), field, getColumnQuoteSymbol());
        if (item.getAlias() != null) {
            String alias = NullUtil.get(item.getAlias().getName(), item.toString());
            // 考虑 oracle 的兼容性, table 暂不添加引用符 (开发者需注意别名不允许是数据库关键字)
            return new Column(String.format("%s.%s", alias, name));
        }
        if (item instanceof Table) {
            String alias = item.toString();
            // 考虑 oracle 的兼容性, table 暂不添加引用符 (开发者需注意别名不允许是数据库关键字)
            return new Column(String.format("%s.%s", alias, name));
        }
        return new Column(name);
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

    private static boolean isVirtualTable(Table table, Set<String> duals) {
        String name = table.getName(); // 当前表名
        return duals.stream().filter(name::equalsIgnoreCase).findAny().isPresent();
    }

    private static Set<String> getVirtualTables(List<WithItem> withItems) {
        Set<String> duals = new HashSet();
        QuickUtil.isTrue(DATA_TYPE.equals(DataType.ORACLE), () -> duals.add("DUAL")); // 添加虚拟表关键字
        Set<String> temps = withItems == null ? duals : withItems.stream()
                .map(WithItem::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        duals.addAll(temps); // 支持WITH AS
        return duals;
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
