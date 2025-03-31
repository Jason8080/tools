package cn.gmlee.tools.datalog.model;

import cn.gmlee.tools.base.util.DiffUtil;
import cn.gmlee.tools.datalog.anno.Ignore;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 数据变化
 * </p>
 *
 * @author Jas °
 * @since 2020 /7/27
 */
@Data
@Ignore
public class Datalog extends LogUser {
    protected Long id;
    protected Long startMs;
    protected Long totalMs;
    protected String originalSql;
    protected String whereSql;
    protected String oldDataJson;
    protected String newDataJson;
    protected String datalogSelectSql;
    protected String dataTable;
    protected String dataLog;
    protected Date createAt;

    /**
     * 更新描述.
     * (10542):
     * {
     *      是否邮寄资料(1：是，0：否):[true]->[true]
     *      是否本人(1：是，0：否):[false]->[false]
     *      是否本人办理:[1]->[1]
     *      过户类型码:[1]->[1]
     *      逻辑删除:[false]->[false]
     *      过户原因类型:[3]->[3]
     *      是否违约(1：是，0：否):[false]->[false]
     *      主键id:[10542]->[10542]
     *      修改人:[0]->[3735]
     * }
     * (10543): ...
     * @param primaryKey
     * @param oldsData
     * @param newData
     * @param fieldCommentMap
     * @return
     */
    public String loadDatalog(String primaryKey, List<Map<String, Object>> oldsData, Map<String, Object> newData, Map<String, String> fieldCommentMap) {
        String report = DiffUtil.comparativeReport(primaryKey, oldsData, newData, fieldCommentMap);
        return this.dataLog = report;
    }
}
