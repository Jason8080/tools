package cn.gmlee.tools.datalog.model;

import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.base.util.NullUtil;
import cn.gmlee.tools.base.util.TimeUtil;
import cn.gmlee.tools.base.util.WebUtil;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 数据变化
 * </p>
 *
 * @author Jas°
 * @since 2020/7/27
 */
@Data
public class LogData extends Datalog {
    private Map<String, String> newData;
    private List<Object> oldData;


    public LogData(LogApi logApi, Map<String, String> newData) {
        this(newData, null);
        this.site = ExceptionUtil.sandbox(() -> logApi.site);
        this.params = ExceptionUtil.sandbox(() -> logApi.params);
        this.api = NullUtil.get(ExceptionUtil.sandbox(() -> logApi.api), WebUtil.getCurrentRelPath());
        this.requestIp = NullUtil.get(ExceptionUtil.sandbox(() -> logApi.requestIp), WebUtil.getCurrentIp());
        this.requestUrl = NullUtil.get(ExceptionUtil.sandbox(() -> logApi.requestUrl), WebUtil.getCurrentUrl());
        this.requestTime = NullUtil.get(ExceptionUtil.sandbox(() -> logApi.requestTime), TimeUtil.getCurrentDate());
    }

    public LogData(LogApi logApi, Map<String, String> newData, List<Object> oldsData) {
        this(newData, oldsData);
        this.site = ExceptionUtil.sandbox(() -> logApi.site);
        this.params = ExceptionUtil.sandbox(() -> logApi.params);
        this.api = NullUtil.get(ExceptionUtil.sandbox(() -> logApi.api), WebUtil.getCurrentRelPath());
        this.requestIp = NullUtil.get(ExceptionUtil.sandbox(() -> logApi.requestIp), WebUtil.getCurrentIp());
        this.requestUrl = NullUtil.get(ExceptionUtil.sandbox(() -> logApi.requestUrl), WebUtil.getCurrentUrl());
        this.requestTime = NullUtil.get(ExceptionUtil.sandbox(() -> logApi.requestTime), TimeUtil.getCurrentDate());
    }

    public LogData(Map<String, String> newData, List<Object> oldsData) {
        this.newData = newData;
        this.oldData = oldsData;
    }
}
