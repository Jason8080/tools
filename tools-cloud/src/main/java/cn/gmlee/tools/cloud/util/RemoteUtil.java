package cn.gmlee.tools.cloud.util;

import cn.gmlee.tools.base.ex.RemoteInvokeException;
import cn.gmlee.tools.base.mod.JsonResult;
import cn.gmlee.tools.base.util.BoolUtil;

/**
 * 通用远程调用工具
 *
 * @author Jas°
 * @date 2020 /10/20 (周二)
 */
public class RemoteUtil {

    /**
     * Get t.
     *
     * @param <T>    the type parameter
     * @param result the result
     * @return the t
     */
    public static <T> T get(JsonResult<T> result) {
        if (BoolUtil.isOk(result)) {
            return result.getData();
        }
        throw new RemoteInvokeException(result);
    }
}
