package cn.gmlee.tools.log.cof;

import cn.gmlee.tools.base.mod.JsonLog;

import java.io.Serializable;

/**
 * 日志触发器.
 */
public interface ApiPrintTrigger extends Serializable {
    /**
     * 触发器.
     *
     * @param jsonLog   日志内容
     * @param result    响应结果
     * @param throwable 异常信息
     * @throws Throwable 抛出异常
     */
    void log(JsonLog jsonLog, Object result, Throwable throwable) throws Throwable;
}
