package cn.gmlee.tools.profile;

import cn.gmlee.tools.base.util.RegexUtil;

public class RegTests {


    public static void main(String[] args) {
        String first = RegexUtil.first("SELECT COUNT( * ) FROM MALL_GOODS mg LEFT JOIN MALL_GOODS_DETAIL d ON d.GOODS_ID = mg.ID WHERE mg.HAS_DELETED = 0 AND mg.GOODS_STATUS IN ('1', '2') AND (d.PAY_BOND_END_TIME >= sysdate OR d.PAY_BOND_END_TIME IS NULL) AND \"env\" IN (0, 1)"
                , "COUNT\\([\\s]?(\\*|\\d)[\\s]?\\)");
        System.out.println(first);
    }
}
