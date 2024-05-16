package cn.gmlee.tools.profile;

import cn.gmlee.tools.base.builder.KvBuilder;
import cn.gmlee.tools.base.util.SqlUtil;

import java.util.Arrays;

public class SqlTests {
    public static void main(String[] args) throws Exception {
        String sql = "SELECT\n" +
                "\tCOUNT( * ) \n" +
                "FROM\n" +
                "\tmall_order_info moi\n" +
                "\tLEFT JOIN MALL_GOODS_DETAIL_SNAPSHOT mgds ON mgds.ORDER_NO = moi.ORDER_NO\n" +
                "\tRIGHT JOIN MALL_DELIVERY_INFO_SNAPSHOT mdis ON mdis.ORDER_NO = moi.ORDER_NO\n" +
                "\tLEFT JOIN MALL_GOODS_SNAPSHOT m ON m.ORDER_NO = moi.ORDER_NO \n" +
                "WHERE\n" +
                "\tmoi.buy_merchant_id = '1691020167383453698'\n" +
                "\tAND ( moi.order_status != '99' OR ( moi.order_status = '99' AND moi.deleted_by != 'buy' ) ) ";
        SqlUtil.reset(SqlUtil.DataType.ORACLE);
        String newSql = SqlUtil.newSelect(sql, KvBuilder.map("env", Arrays.asList(0, 1)));
        System.out.println();
        System.out.println(newSql);
        System.out.println();
    }
}
