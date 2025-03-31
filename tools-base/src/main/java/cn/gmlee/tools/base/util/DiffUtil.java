package cn.gmlee.tools.base.util;

import java.util.List;
import java.util.Map;

/**
 * 差异对比工具.
 */
public class DiffUtil {
    /**
     * 对比报告(描述).
     * (10542):
     * {
     * 是否邮寄资料(1：是，0：否):[true]->[true]
     * 是否本人(1：是，0：否):[false]->[false]
     * 是否本人办理:[1]->[1]
     * 过户类型码:[1]->[1]
     * 逻辑删除:[false]->[false]
     * 过户原因类型:[3]->[3]
     * 是否违约(1：是，0：否):[false]->[false]
     * 主键id:[10542]->[10542]
     * 修改人:[0]->[3735]
     * }
     * (10543): ...
     *
     * @param primaryKey      关联主键
     * @param oldsData        旧数据
     * @param newData         新数据
     * @param fieldCommentMap 字段描述
     * @return 差异描述 string
     * @author Jas °
     */
    public static String comparativeReport(String primaryKey, List<Map<String, Object>> oldsData, Map<String, Object> newData, Map<String, String> fieldCommentMap) {
        StringBuilder sb = new StringBuilder();
        if (BoolUtil.notEmpty(newData) && BoolUtil.notEmpty(oldsData)) {
            for (int i = 0; i < oldsData.size(); i++) {
                Map<String, Object> oldData = oldsData.get(i);
                if (BoolUtil.isEmpty(oldData)) {
                    continue;
                }
                if (i != 0) {
                    sb.append(",\r\n");
                }
                sb.append(String.format("(%s):\r\n", oldData.get(primaryKey)));
                sb.append(comparativeReport(oldData, newData, fieldCommentMap));
            }
        }
        return sb.toString();
    }

    /**
     * 对比报告(描述).
     *
     * @param oldData         旧数据
     * @param newData         新数据
     * @param fieldCommentMap 字段描述
     * @return string 对比报告
     */
    public static String comparativeReport(Map<String, Object> oldData, Map<String, Object> newData, Map<String, String> fieldCommentMap) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        newData.forEach((key, value) -> {
            String replaceKey = key.replaceAll("`|'", "");
            // 减少不必要的内容输出
            if (BoolUtil.notNull(oldData.get(replaceKey)) && !BoolUtil.eq(oldData.get(replaceKey), value)) {
                sb.append("\r\n");
                sb.append("\t");
                sb.append(String.format("%s:[%s]->[%s] ",
                        fieldCommentMap.get(replaceKey), oldData.get(replaceKey), value));
            }
        });
        sb.append("\r\n}");
        return sb.toString();
    }
}