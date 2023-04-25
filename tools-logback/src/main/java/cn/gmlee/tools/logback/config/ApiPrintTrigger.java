package cn.gmlee.tools.logback.config;

import cn.gmlee.tools.base.mod.JsonLog;

import java.io.Serializable;

/**
 * 日志触发器.
 */
public interface ApiPrintTrigger extends Serializable {
    /**
     * 触发器.
     *
     * @param jsonLog 日志内容
     * @param result  响应结果
     * @param e       异常信息
     */
    void log(JsonLog jsonLog, Object result, Exception e) throws Exception;
}
