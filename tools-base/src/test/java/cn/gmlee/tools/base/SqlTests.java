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
        String sql = "WITH increaseCustomer AS (\n" +
                "\tSELECT\n" +
                "\t\tdayFormatTime,\n" +
                "\t\tCOUNT( 1 ) AS increaseNum \n" +
                "\tFROM\n" +
                "\t\t(\n" +
                "\t\tSELECT\n" +
                "\t\t\tMERCHANT_NAME,\n" +
                "\t\t\tMIN( dayFormatTime ) AS dayFormatTime \n" +
                "\t\tFROM\n" +
                "\t\t\t(\n" +
                "\t\t\tSELECT\n" +
                "\t\t\t\tMERCHANT_NAME,\n" +
                "\t\t\t\tTO_CHAR( CREATE_TIME, 'YYYY-MM' ) AS dayFormatTime \n" +
                "\t\t\tFROM\n" +
                "\t\t\t\tLDW_BID.BID_PRICE_RECORD \n" +
                "\t\t\tWHERE\n" +
                "\t\t\t\tHAS_DELETED = '0' UNION ALL\n" +
                "\t\t\tSELECT\n" +
                "\t\t\t\tSIGN_MERCHANT_NAME,\n" +
                "\t\t\t\tTO_CHAR( UPDATE_TIME, 'YYYY-MM' ) AS dayFormatTime \n" +
                "\t\t\tFROM\n" +
                "\t\t\t\tLDW_BID.BID_ENROLL \n" +
                "\t\t\tWHERE\n" +
                "\t\t\t\tAUDIT_STATUS = '2' \n" +
                "\t\t\t\tAND HAS_DELETED = '0' UNION ALL\n" +
                "\t\t\tSELECT\n" +
                "\t\t\t\tCUST_NAME AS MERCHANT_ID,\n" +
                "\t\t\t\tTO_CHAR( REG_TIME, 'YYYY-MM' ) AS dayFormatTime \n" +
                "\t\t\tFROM\n" +
                "\t\t\t\tebp.T_BID_PRICE_RECORD UNION ALL\n" +
                "\t\t\tSELECT\n" +
                "\t\t\t\tMERCHANT_NAME AS MERCHANT_ID,\n" +
                "\t\t\t\tTO_CHAR( UPD_TIME, 'YYYY-MM' ) AS dayFormatTime \n" +
                "\t\t\tFROM\n" +
                "\t\t\t\tebp.T_FAIR_MEMBERSHIP_ENROLL e\n" +
                "\t\t\t\tLEFT JOIN SYS_MERCHANT s ON e.cust_no = s.id \n" +
                "\t\t\tWHERE\n" +
                "\t\t\t\tAUDIT_STATUS = '2' \n" +
                "\t\t\t) \n" +
                "\t\tGROUP BY\n" +
                "\t\t\tMERCHANT_NAME \n" +
                "\t\t) temp \n" +
                "\tWHERE\n" +
                "\t\tMERCHANT_NAME NOT IN ( SELECT MERCHANT_NAME FROM DATA_BOARD_TEST_MERCHANT ) \n" +
                "\tGROUP BY\n" +
                "\t\tdayFormatTime \n" +
                "\t) SELECT\n" +
                "\tnvl( c1.increaseNum, 0 ) addCustomersCount,\n" +
                "\tnvl( c1.dayFormatTime, temp.day ) dayTime,\n" +
                "CASE\n" +
                "\t\t\n" +
                "\t\tWHEN c2.increaseNum IS NULL THEN\n" +
                "\t\t0 ELSE ROUND( ( nvl( c1.increaseNum, 0 ) - c2.increaseNum ) / c2.increaseNum, 4 ) * 100 \n" +
                "\tEND AS sameComparison,\n" +
                "CASE\n" +
                "\t\t\n" +
                "\t\tWHEN c3.increaseNum IS NULL THEN\n" +
                "\t\t0 ELSE ROUND( ( nvl( c1.increaseNum, 0 ) - c3.increaseNum ) / c3.increaseNum, 4 ) * 100 \n" +
                "\tEND AS sequentialComparison \n" +
                "FROM\n" +
                "\tincreaseCustomer c1\n" +
                "\tFULL JOIN (\n" +
                "\tSELECT\n" +
                "\t\tTO_CHAR( ADD_MONTHS( SYSDATE, - ( LEVEL - 1 ) ), 'YYYY-MM' ) AS day \n" +
                "\tFROM\n" +
                "\t\tDUAL CONNECT BY LEVEL <= (\n" +
                "\t\tSELECT\n" +
                "\t\t\tMONTHS_BETWEEN(\n" +
                "\t\t\t\tTO_DATE( TO_CHAR( SYSDATE, 'YYYY-MM' ), 'YYYY-MM' ),\n" +
                "\t\t\t\tTO_DATE(\n" +
                "\t\t\t\t\t(\n" +
                "\t\t\t\t\tSELECT\n" +
                "\t\t\t\t\t\tmin( dayFormatTime ) \n" +
                "\t\t\t\t\tFROM\n" +
                "\t\t\t\t\t\t(\n" +
                "\t\t\t\t\t\tSELECT\n" +
                "\t\t\t\t\t\t\tMERCHANT_NAME,\n" +
                "\t\t\t\t\t\t\tTO_CHAR( CREATE_TIME, 'YYYY-MM' ) dayFormatTime \n" +
                "\t\t\t\t\t\tFROM\n" +
                "\t\t\t\t\t\t\tLDW_BID.BID_PRICE_RECORD \n" +
                "\t\t\t\t\t\tWHERE\n" +
                "\t\t\t\t\t\t\tHAS_DELETED = '0' UNION ALL\n" +
                "\t\t\t\t\t\tSELECT\n" +
                "\t\t\t\t\t\t\tSIGN_MERCHANT_NAME,\n" +
                "\t\t\t\t\t\t\tTO_CHAR( UPDATE_TIME, 'YYYY-MM' ) dayFormatTime \n" +
                "\t\t\t\t\t\tFROM\n" +
                "\t\t\t\t\t\t\tLDW_BID.BID_ENROLL \n" +
                "\t\t\t\t\t\tWHERE\n" +
                "\t\t\t\t\t\t\tAUDIT_STATUS = '2' \n" +
                "\t\t\t\t\t\t\tAND HAS_DELETED = '0' UNION ALL\n" +
                "\t\t\t\t\t\tSELECT\n" +
                "\t\t\t\t\t\t\tCUST_NAME,\n" +
                "\t\t\t\t\t\t\tTO_CHAR( REG_TIME, 'YYYY-MM' ) dayFormatTime \n" +
                "\t\t\t\t\t\tFROM\n" +
                "\t\t\t\t\t\t\tebp.T_BID_PRICE_RECORD UNION ALL\n" +
                "\t\t\t\t\t\tSELECT\n" +
                "\t\t\t\t\t\t\tmerchant_name,\n" +
                "\t\t\t\t\t\t\tTO_CHAR( UPD_TIME, 'YYYY-MM' ) dayFormatTime \n" +
                "\t\t\t\t\t\tFROM\n" +
                "\t\t\t\t\t\t\tebp.T_FAIR_MEMBERSHIP_ENROLL e\n" +
                "\t\t\t\t\t\t\tLEFT JOIN SYS_MERCHANT s ON e.cust_no = s.id \n" +
                "\t\t\t\t\t\tWHERE\n" +
                "\t\t\t\t\t\t\tAUDIT_STATUS = '2' \n" +
                "\t\t\t\t\t\t) \n" +
                "\t\t\t\t\t),\n" +
                "\t\t\t\t\t'YYYY-MM' \n" +
                "\t\t\t\t) \n" +
                "\t\t\t) + 1 \n" +
                "\t\tFROM\n" +
                "\t\t\tdual \n" +
                "\t\t) \n" +
                "\t) temp ON temp.day = c1.dayFormatTime\n" +
                "\tLEFT JOIN increaseCustomer c2 ON nvl( c1.dayFormatTime, temp.day ) = TO_CHAR( ADD_MONTHS( TO_DATE( c2.dayFormatTime, 'YYYY-MM' ), 12 ), 'YYYY-MM' )\n" +
                "\tLEFT JOIN increaseCustomer c3 ON nvl( c1.dayFormatTime, temp.day ) = TO_CHAR( ADD_MONTHS( TO_DATE( c3.dayFormatTime, 'YYYY-MM' ), 1 ), 'YYYY-MM' ) \n" +
                "ORDER BY\n" +
                "\tdayTime";
        Map<String, List> wheres = new HashMap<>();
        wheres.put("env", Arrays.asList("0", "1"));
        SqlUtil.reset(SqlUtil.DataType.ORACLE);
        String newSql = SqlUtil.newSelect(sql, wheres);
        System.out.println(newSql);
    }
}
