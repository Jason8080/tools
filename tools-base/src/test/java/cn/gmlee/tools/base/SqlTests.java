package cn.gmlee.tools.base;

import cn.gmlee.tools.base.util.SqlUtil;
import org.junit.Test;

import java.util.Arrays;
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
                "\t\tTMP.*,\n" +
                "\t\tROWNUM ROW_ID \n" +
                "\tFROM\n" +
                "\t\t(\n" +
                "\t\t\tWITH t1 AS (\n" +
                "\t\t\tSELECT\n" +
                "\t\t\t\tu.real_name,\n" +
                "\t\t\t\tu.phone,\n" +
                "\t\t\t\tur.user_id,\n" +
                "\t\t\t\tu.post,\n" +
                "\t\t\t\tur.id \n" +
                "\t\t\tFROM\n" +
                "\t\t\t\tsys_user_role ur\n" +
                "\t\t\t\tLEFT JOIN sys_role r ON r.id = ur.role_id\n" +
                "\t\t\t\tLEFT JOIN sys_user u ON ur.user_id = u.id \n" +
                "\t\t\tWHERE\n" +
                "\t\t\t\tur.role_id = '1681870437338206209' \n" +
                "\t\t\t\tAND ur.merchant_id = 'M00610' \n" +
                "\t\t\t\tAND ur.has_deleted = '0' \n" +
                "\t\t\t),\n" +
                "\t\t\tt2 AS (\n" +
                "\t\t\tSELECT\n" +
                "\t\t\t\tud.user_id,\n" +
                "\t\t\t\twm_concat ( d.dept_name ) AS deptName \n" +
                "\t\t\tFROM\n" +
                "\t\t\t\tsys_user_dept ud\n" +
                "\t\t\t\tLEFT JOIN sys_dept d ON ud.dept_id = d.id\n" +
                "\t\t\t\tLEFT JOIN t1 ON ud.user_id = t1.user_id \n" +
                "\t\t\tWHERE\n" +
                "\t\t\t\tud.has_deleted = '0' \n" +
                "\t\t\t\tAND ud.merchant_id = 'M00610' \n" +
                "\t\t\tGROUP BY\n" +
                "\t\t\t\tud.user_id \n" +
                "\t\t\t),\n" +
                "\t\t\tt3 AS (\n" +
                "\t\t\tSELECT\n" +
                "\t\t\t\tsur.user_id,\n" +
                "\t\t\t\twm_concat ( sr.role_name ) AS roleName \n" +
                "\t\t\tFROM\n" +
                "\t\t\t\tsys_user_role sur\n" +
                "\t\t\t\tLEFT JOIN sys_role sr ON sur.role_id = sr.id\n" +
                "\t\t\t\tLEFT JOIN t1 ON sur.user_id = t1.user_id \n" +
                "\t\t\tWHERE\n" +
                "\t\t\t\tsur.has_deleted = '0' \n" +
                "\t\t\t\tAND sur.merchant_id = 'M00610' \n" +
                "\t\t\tGROUP BY\n" +
                "\t\t\t\tsur.user_id \n" +
                "\t\t\t),\n" +
                "\t\t\tt4 AS (\n" +
                "\t\t\tSELECT\n" +
                "\t\t\t\tt1.id,\n" +
                "\t\t\t\tt1.user_id userId,\n" +
                "\t\t\t\tt1.real_name realName,\n" +
                "\t\t\t\tt1.phone,\n" +
                "\t\t\t\tt1.post,\n" +
                "\t\t\t\tt2.deptName,\n" +
                "\t\t\t\tt3.roleName \n" +
                "\t\t\tFROM\n" +
                "\t\t\t\tt1\n" +
                "\t\t\t\tLEFT JOIN t2 ON t1.user_id = t2.user_id\n" +
                "\t\t\t\tLEFT JOIN t3 ON t1.user_id = t3.user_id \n" +
                "\t\t\tORDER BY\n" +
                "\t\t\t\tt1.user_id \n" +
                "\t\t\t) SELECT\n" +
                "\t\t\t* \n" +
                "\t\tFROM\n" +
                "\t\t\tt4 \n" +
                "\t\tORDER BY\n" +
                "\t\t\tid \n" +
                "\t\t) TMP \n" +
                "\tWHERE\n" +
                "\t\tROWNUM <= 10 \n" +
                "\t) \n" +
                "WHERE\n" +
                "\tROW_ID > 0";
        Map<String, List> wheres = new HashMap<>();
        wheres.put("env", Arrays.asList("0", "1"));
        SqlUtil.reset(SqlUtil.DataType.ORACLE);
        String newSql = SqlUtil.newSelect(sql, wheres);
        System.out.println(newSql);
    }
}
