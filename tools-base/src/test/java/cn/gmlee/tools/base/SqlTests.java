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
        String sql = "select dayTime, categoryName, ROUND(qty, 0) as tradeVolume , ROUND(tradeAmount / 10000, 0) as tradeAmount from ( select dayTime, sortS, categoryName, sum(qty) as qty , sum(tradeAmount) as tradeAmount from ( select to_char(case  when moi.SELL_WAY = '2' then mob.paytime else moi.PAYTIME end, 'YYYY-MM-dd') as dayTime , to_number(to_char(case  when moi.SELL_WAY = '2' then mob.paytime else moi.PAYTIME end, 'YYYYMMdd')) as sortS , a.category_name as categoryName, BUY_NUMBER as qty, ACTUAL_RECEIPT as tradeAmount from ldw_mall.mall_order_info moi left join ldw_mall.mall_goods_snapshot mgs on moi.order_no = mgs.order_no  left join ( select ORDER_NO, min(UPDATE_TIME) as paytime from MALL_ORDER_BOND where HAS_DELETE = '0' and STATUS = '2' group by ORDER_NO ) mob on moi.ORDER_NO = mob.ORDER_NO  left join ( select distinct CATEGORY_NAME , REGEXP_SUBSTR(RELATED_VARIETY_ID, '[^,]+', 1, LEVEL) as varietyId from ( select CATEGORY_NAME, RELATED_VARIETY_ID from ldw_sys.SYS_CATEGORY_MANAGEMENT where BUSINESS_MODE = '16' and STATUS = '0' and HAS_DELETED = '0' ) a connect by LEVEL <= LENGTH(RELATED_VARIETY_ID) - LENGTH(REGEXP_REPLACE(RELATED_VARIETY_ID, ',', null)) + 1 ) a on mgs.variety_id = a.varietyId  where moi.ORDER_STATUS != '1' and moi.BUSINESS_TYPE = '2' and moi.SELL_MERCHANT_ID = 'M00610' and moi.BUY_MERCHANT_ID not in ( select merchant_id from ldw_sys.data_board_test_merchant ) ) a group by dayTime, categoryName, sortS union all select dayTime, sortS, '全部' as categoryName, sum(qty) as qty , sum(tradeAmount) as tradeAmount from ( select to_char(case  when moi.SELL_WAY = '2' then mob.paytime else moi.PAYTIME end, 'YYYY-MM-dd') as dayTime , to_number(to_char(case  when moi.SELL_WAY = '2' then mob.paytime else moi.PAYTIME end, 'YYYYMMdd')) as sortS , a.category_name as categoryName, BUY_NUMBER as qty, ACTUAL_RECEIPT as tradeAmount from ldw_mall.mall_order_info moi left join ldw_mall.mall_goods_snapshot mgs on moi.order_no = mgs.order_no  left join ( select ORDER_NO, min(UPDATE_TIME) as paytime from MALL_ORDER_BOND where HAS_DELETE = '0' and STATUS = '2' group by ORDER_NO ) mob on moi.ORDER_NO = mob.ORDER_NO  left join ( select distinct CATEGORY_NAME , REGEXP_SUBSTR(RELATED_VARIETY_ID, '[^,]+', 1, LEVEL) as varietyId from ( select CATEGORY_NAME, RELATED_VARIETY_ID from ldw_sys.SYS_CATEGORY_MANAGEMENT where BUSINESS_MODE = '16' and STATUS = '0' and HAS_DELETED = '0' ) a connect by LEVEL <= LENGTH(RELATED_VARIETY_ID) - LENGTH(REGEXP_REPLACE(RELATED_VARIETY_ID, ',', null)) + 1 ) a on mgs.variety_id = a.varietyId  where moi.ORDER_STATUS != '1' and moi.BUSINESS_TYPE = '2' and moi.SELL_MERCHANT_ID = 'M00610' and moi.BUY_MERCHANT_ID not in ( select merchant_id from ldw_sys.data_board_test_merchant ) ) a where categoryName is not null group by dayTime, sortS ) a where categoryName is not null and dayTime is not null order by sortS";
        Map<String, List<? extends Comparable>> wheres = new HashMap<>();
        wheres.put("env", Arrays.asList("0", "1"));
        SqlUtil.reset(SqlUtil.DataType.ORACLE);
        String newSql = SqlUtil.newSelect(sql, wheres);
        System.out.println(newSql);
    }
}
