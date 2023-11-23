package cn.gmlee.tools.base.assist;

import cn.gmlee.tools.base.mod.R;
import cn.gmlee.tools.base.util.ExceptionUtil;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.InetAddress;

/**
 * 通用方法参数解析辅助工具
 *
 * @author Jas °
 * @date 2021 /2/18 (周四)
 */
public class ApiAssist {

    /**
     * 获取代码执行位置.
     *
     * @param className the class name
     * @param methodObj the method obj
     * @return the key
     */
    public static String getSite(String className, Method methodObj) {
        String ip = ExceptionUtil.sandbox(() -> InetAddress.getLocalHost().getHostAddress());
        Long threadId = Thread.currentThread().getId();
        return ip + ":" + threadId + ":" + className + "." + methodObj.getName();
    }

    /**
     * 获取相应参数.
     *
     * @param returnType the return type
     * @param result     the result
     * @return the response params
     */
    public static Object getResponseParams(Class<?> returnType, Object result) {
        if (result == null || returnType == null) {
            return "null";
        } else if (result instanceof R) {
            return result;
        } else if (result instanceof Serializable) {
            return result;
        } else if (returnType.equals(result.getClass())) {
            return result;
        } else {
            return "请响应规则数据: Serializable!";
        }
    }
}
