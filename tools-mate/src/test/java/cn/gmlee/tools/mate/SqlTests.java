package cn.gmlee.tools.mate;

import cn.gmlee.tools.mate.assist.ExpressionAssist;
import cn.gmlee.tools.base.util.SqlUtil;
import net.sf.jsqlparser.expression.Expression;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SqlTests {


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
        wheres.put("auth_id", ExpressionAssist.as());
//        wheres.put("auth_id", ExpressionAssist.as("1", "2", "3"));
//        wheres.put("auth_type", ExpressionAssist.as(0, 1));
        String newSql = SqlUtil.newSelect(sql, wheres);
        System.out.println(newSql);
        System.out.println(newSql.equals(sql));
    }
}
