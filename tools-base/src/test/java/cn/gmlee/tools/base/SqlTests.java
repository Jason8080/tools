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
        String sql = "SELECT\n" +
                "\t* \n" +
                "FROM\n" +
                "\t(\n" +
                "\tSELECT\n" +
                "\t\tmax( sc.id ) AS ID,\n" +
                "\t\tsc.batch_no,\n" +
                "\t\tsum( sc.CONTAINER_WEIGHT ) AS CONTAINER_WEIGHT,\n" +
                "\t\twmsys.wm_concat ( to_char( sc.CONTAINER_ID ) ) AS CONTAINER_ID \n" +
                "\tFROM\n" +
                "\t\tMALL_STOCK_CONTAINER sc \n" +
                "\tWHERE\n" +
                "\t\tsc.GOODS_ID = '20240511794753' \n" +
                "\t\tAND sc.HAS_DELETED = 0 \n" +
                "\tGROUP BY\n" +
                "\tBATCH_NO \n" +
                "\t)";
        Map<String, List<Expression>> wheres = new HashMap<>();
        wheres.put("env", ExpressionAssist.as("0", "1"));
//        wheres.put("auth_id", ExpressionAssist.as());
//        wheres.put("auth_id", ExpressionAssist.as("1", "2", "3"));
//        wheres.put("auth_type", ExpressionAssist.as(0, 1));
        SqlUtil.reset(SqlUtil.DataType.ORACLE);
        String newSql = SqlUtil.newSelect(sql, wheres);
        System.out.println(newSql);
    }
}
