package cn.gmlee.tools.base;

import cn.gmlee.tools.base.assist.ExpressionAssist;
import cn.gmlee.tools.base.util.SqlUtil;
import net.sf.jsqlparser.expression.Expression;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SqlTests {

    @Test
    public void testSql1() throws Exception{
        String sql = "SELECT log.id, sys.sys_name, `user`.username FROM log \n" +
                "LEFT JOIN `user` on `user`.id = log.created_by\n" +
                "LEFT JOIN sys on sys.id = log.sys_id;";
        Map<String, List<Expression>> wheres = new HashMap<>();
//        wheres.put("auth_id", ExpressionAssist.as());
//        wheres.put("auth_id", ExpressionAssist.as("1", "2", "3"));
        wheres.put("auth_type", ExpressionAssist.as(0, 1));
        SqlUtil.resetColumnQuoteSymbol("`");
        String newSql = SqlUtil.newSelect(sql, wheres);
        System.out.println(newSql);
    }
}
