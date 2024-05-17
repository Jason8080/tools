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
        String sql = "WITH t1 AS (\n" +
                "\tSELECT\n" +
                "\t\tu.real_name,\n" +
                "\t\tu.phone,\n" +
                "\t\tur.user_id,\n" +
                "\t\tu.post,\n" +
                "\t\tur.id \n" +
                "\tFROM\n" +
                "\t\tsys_user_role ur\n" +
                "\t\tLEFT JOIN sys_role r ON r.id = ur.role_id\n" +
                "\t\tLEFT JOIN sys_user u ON ur.user_id = u.id \n" +
                "\tWHERE\n" +
                "\t\tur.role_id = '1681870437338206209' \n" +
                "\t\tAND ur.merchant_id = 'M00610' \n" +
                "\t\tAND ur.has_deleted = '0' \n" +
                "\t),\n" +
                "\tt2 AS (\n" +
                "\tSELECT\n" +
                "\t\tud.user_id,\n" +
                "\t\twm_concat ( d.dept_name ) AS deptName \n" +
                "\tFROM\n" +
                "\t\tsys_user_dept ud\n" +
                "\t\tLEFT JOIN sys_dept d ON ud.dept_id = d.id\n" +
                "\t\tLEFT JOIN t1 ON ud.user_id = t1.user_id \n" +
                "\tWHERE\n" +
                "\t\tud.has_deleted = '0' \n" +
                "\t\tAND ud.merchant_id = 'M00610' \n" +
                "\tGROUP BY\n" +
                "\t\tud.user_id \n" +
                "\t),\n" +
                "\tt3 AS (\n" +
                "\tSELECT\n" +
                "\t\tsur.user_id,\n" +
                "\t\twm_concat ( sr.role_name ) AS roleName \n" +
                "\tFROM\n" +
                "\t\tsys_user_role sur\n" +
                "\t\tLEFT JOIN sys_role sr ON sur.role_id = sr.id\n" +
                "\t\tLEFT JOIN t1 ON sur.user_id = t1.user_id \n" +
                "\tWHERE\n" +
                "\t\tsur.has_deleted = '0' \n" +
                "\t\tAND sur.merchant_id = 'M00610' \n" +
                "\tGROUP BY\n" +
                "\t\tsur.user_id \n" +
                "\t),\n" +
                "\tt4 AS (\n" +
                "\tSELECT\n" +
                "\t\tt1.id,\n" +
                "\t\tt1.user_id userId,\n" +
                "\t\tt1.real_name realName,\n" +
                "\t\tt1.phone,\n" +
                "\t\tt1.post,\n" +
                "\t\tt2.deptName,\n" +
                "\t\tt3.roleName \n" +
                "\tFROM\n" +
                "\t\tt1\n" +
                "\t\tLEFT JOIN t2 ON t1.user_id = t2.user_id\n" +
                "\t\tLEFT JOIN t3 ON t1.user_id = t3.user_id \n" +
                "\tORDER BY\n" +
                "\t\tt1.user_id \n" +
                "\t) SELECT\n" +
                "\tCOUNT( * ) \n" +
                "FROM\n" +
                "\tt4";
        Map<String, List> wheres = new HashMap<>();
        wheres.put("env", Arrays.asList("0", "1"));
        SqlUtil.reset(SqlUtil.DataType.ORACLE);
        String newSql = SqlUtil.newSelect(sql, wheres);
        System.out.println(newSql);
    }
}
