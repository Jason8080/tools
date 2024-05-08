package cn.gmlee.tools.mate.assist;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.NullUtil;
import cn.gmlee.tools.base.util.QuickUtil;
import cn.gmlee.tools.base.util.RegexUtil;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
@Slf4j
public class SqlAssist {

    public static void main(String[] args) throws Exception {
        String sql = "SELECT id, sys_name, creator from (\n" +
                "SELECT\n" +
                "\tlog.id,\n" +
                "\tsys.sys_name,\n" +
                "\t( SELECT `user`.username FROM `user` WHERE `user`.id = log.created_by ) creator \n" +
                "FROM\n" +
                "\tlog\n" +
                "\tLEFT JOIN sys ON sys.id = log.sys_id \n" +
                "WHERE\n" +
                "\tlog.del = 0\n" +
                "\t) t";
        Map<String, List<Expression>> wheres = new HashMap<>();
        wheres.put("auth_id", ExpressionAssist.as("1", "2", "3"));
        wheres.put("auth_type", ExpressionAssist.as(0, 1));
        String newSql = getNewSql(sql, wheres);
        System.out.println(newSql);
    }

    public static String getNewSql(String originSql, Map<String, List<Expression>> wheres) throws Exception {
        // 非空条件方才处理
        if (BoolUtil.isEmpty(wheres)) {
            return originSql;
        }
        Statement statement = CCJSqlParserUtil.parse(originSql);
        // 非插入句柄不处理
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
        return statement.toString();
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
        wheres.forEach((k, v) -> addWhere(plainSelect, k, v));
    }

    private static void addWhere(PlainSelect plainSelect, String key, List<Expression> values) {
        // 获取别名值
        Column column = getColumn(plainSelect.getFromItem(), key);
        // 添加条件值
        InExpression in = new InExpression();
        in.setLeftExpression(column);
        ExpressionList expressionList = new ExpressionList();
        in.setRightItemsList(expressionList);
        AndExpression and = new AndExpression()
                .withLeftExpression(plainSelect.getWhere())
                .withRightExpression(in);
        // 添加条件列
        expressionList.withExpressions(values);
        plainSelect.setWhere(plainSelect.getWhere() != null ? and : in);
    }

    private static void addWheres(Join join, Map<String, List<Expression>> wheres) {
        wheres.forEach((k, v) -> addWhere(join, k, v));
    }

    private static void addWhere(Join join, String key, List<Expression> values) {
        // 获取别名值
        Column column = getColumn(join.getRightItem(), key);
        // 添加In条件
        InExpression in = new InExpression();
        in.setLeftExpression(column);
        // 添加In值集
        ExpressionList expressionList = new ExpressionList();
        in.setRightItemsList(expressionList);
        expressionList.addExpressions(values);
        // 高版本升级
//        List<Expression> es = join.getOnExpressions().stream().map(x -> new AndExpression(x, in)).collect(Collectors.toList());
//        join.setOnExpressions(es);
        AndExpression and = new AndExpression(join.getOnExpression(), in);
        join.setOnExpression(and);
    }

    private static Column getColumn(FromItem item, String field) {
        String alias = NullUtil.get(() -> item.getAlias().getName(), item.toString());
        String tab = String.format("%s%s%s", "`", alias, "`");
        String name = String.format("%s.%s%s%s", tab, "`", field, "`");
        return new Column(name);
    }

    private static void addColumns(PlainSelect plainSelect, Map<String, List<Expression>> wheres) {
        wheres.forEach((k, v) -> addColumn(plainSelect, k, v));
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
