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
                "\t\tTMP.*,\n" +
                "\t\tROWNUM ROW_ID \n" +
                "\tFROM\n" +
                "\t\t(\n" +
                "\t\tSELECT\n" +
                "\t\t\tmg.ID,\n" +
                "\t\t\tmg.GOODS_ATTACH_ID,\n" +
                "\t\t\tmg.GOODS_NAME,\n" +
                "\t\t\tmg.GOODS_STATUS goodsStatus,\n" +
                "\t\t\tmg.PACKAGING_TYPE packagingType,\n" +
                "\t\t\tmg.MERCHANT_ID,\n" +
                "\t\t\tmg.GOODS_PRICE,\n" +
                "\t\t\tmg.UNIT,\n" +
                "\t\t\tmg.PROVINCE,\n" +
                "\t\t\tmg.PROVINCE_CODE,\n" +
                "\t\t\tmg.CITY,\n" +
                "\t\t\tmg.CITY_CODE,\n" +
                "\t\t\tmg.UPDATE_TIME,\n" +
                "\t\t\tmg.sell_way,\n" +
                "\t\t\tmg.sales_way,\n" +
                "\t\t\tmg.BUSINESS_TYPE businessType,\n" +
                "\t\t\tmg.CONTAINER_STANDARD_WEIGHT containerStandardWeight,\n" +
                "\t\t\tmg.DELIVERY_TYPE deliveryType,\n" +
                "\t\t\tmsr.saleGoodsNumber,\n" +
                "\t\t\tmsr.soldGoodsNumber,\n" +
                "\t\t\tmsr.lockGoodsNumber,\n" +
                "\t\t\tmsr.saleBoxNumber,\n" +
                "\t\t\tmsr.virtuallySaleBoxNumber,\n" +
                "\t\t\tmsr.virtuallySoldGoodsNumber \n" +
                "\t\tFROM\n" +
                "\t\t\tMALL_GOODS mg\n" +
                "\t\t\tLEFT JOIN MALL_GOODS_DETAIL d ON d.GOODS_ID = mg.ID\n" +
                "\t\t\tLEFT JOIN (\n" +
                "\t\t\tSELECT\n" +
                "\t\t\t\tGOODS_ID,\n" +
                "\t\t\t\tsum( HAS_VIRTUALLY ) hasVirtually,\n" +
                "\t\t\t\tSUM( SALE_BOX_NUMBER ) saleBoxNumber,\n" +
                "\t\t\t\tSUM( SALE_GOODS_NUMBER ) saleGoodsNumber,\n" +
                "\t\t\t\tSUM( SOLD_GOODS_NUMBER ) soldGoodsNumber,\n" +
                "\t\t\tNVL( sum( CASE WHEN HAS_VIRTUALLY = 1 THEN 0 ELSE LOCK_GOODS_NUMBER END ), 0 ) lockGoodsNumber,\n" +
                "\tNVL( sum( CASE WHEN HAS_VIRTUALLY = 1 THEN SALE_BOX_NUMBER ELSE 0 END ), 0 ) virtuallySaleBoxNumber,\n" +
                "\tNVL( sum( CASE WHEN HAS_VIRTUALLY = 1 THEN SALE_GOODS_NUMBER ELSE 0 END ), 0 ) virtuallySaleGoodsNumber,\n" +
                "\tNVL( sum( CASE WHEN HAS_VIRTUALLY = 1 THEN SOLD_GOODS_NUMBER ELSE 0 END ), 0 ) virtuallySoldGoodsNumber \n" +
                "FROM\n" +
                "\tMALL_STOCK_RECEIPT_DOC \n" +
                "GROUP BY\n" +
                "\tGOODS_ID \n" +
                "\t) msr ON msr.GOODS_ID = mg.ID \n" +
                "WHERE\n" +
                "\tmg.HAS_DELETED = 0 \n" +
                "\tAND mg.GOODS_STATUS IN ( '1', '2' ) \n" +
                "\tAND ( d.PAY_BOND_END_TIME >= SYSDATE OR d.PAY_BOND_END_TIME IS NULL ) \n" +
                "ORDER BY\n" +
                "CASE\n" +
                "\t\t\n" +
                "\t\tWHEN mg.GOODS_STATUS = 2 \n" +
                "\t\tAND (\n" +
                "\t\t\t( hasVirtually > 0 AND ( msr.virtuallySaleBoxNumber > 0 OR msr.virtuallySaleGoodsNumber > 0 ) ) \n" +
                "\t\t\tOR ( hasVirtually < 1 AND ( ( msr.saleBoxNumber > 0 OR msr.saleGoodsNumber > 0 ) ) ) \n" +
                "\t\t\t) THEN\n" +
                "\t\t\t1 \n" +
                "\t\t\tWHEN mg.GOODS_STATUS = 2 \n" +
                "\t\t\tAND ( msr.saleBoxNumber = 0 OR msr.virtuallySaleBoxNumber = 0 ) THEN\n" +
                "\t\t\t\t2 \n" +
                "\t\t\t\tWHEN mg.GOODS_STATUS = 2 \n" +
                "\t\t\t\tAND ( msr.saleBoxNumber IS NULL OR msr.saleGoodsNumber IS NULL ) THEN\n" +
                "\t\t\t\t\t3 \n" +
                "\t\t\t\t\tWHEN mg.GOODS_STATUS = 1 THEN\n" +
                "\t\t\t\t\t4 \n" +
                "\t\t\t\tEND,\n" +
                "\t\t\t\tmg.UPDATE_TIME DESC \n" +
                "\t\t\t) TMP \n" +
                "\t\tWHERE\n" +
                "\t\t\tROWNUM <= 6\n" +
                "\t\t) \n" +
                "WHERE\n" +
                "\tROW_ID > 0";
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
