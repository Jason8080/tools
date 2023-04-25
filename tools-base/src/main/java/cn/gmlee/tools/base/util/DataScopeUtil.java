package cn.gmlee.tools.base.util;

import cn.gmlee.tools.base.anno.DataFilter;
import cn.gmlee.tools.base.anno.DataScope;

/**
 * 鉴权对象绑定到当前线程的工具.
 *
 * @author Jas °
 * @date 2021 /11/4 (周四)
 */
public class DataScopeUtil {

    /**
     * 存鉴权注解的地方
     */
    private static final ThreadLocal<DataScope> auths = new ThreadLocal();

    /**
     * Get api auth.
     *
     * @return the api auth
     */
    public static DataScope get() {
        return auths.get();
    }

    /**
     * 获取列鉴权元数据.
     *
     * @return the data auth [ ]
     */
    public static DataFilter[] getCol() {
        DataScope dataScope = get();
        if (dataScope != null) {
            return dataScope.col();
        }
        return new DataFilter[]{};
    }

    /**
     * 获取行鉴权元数据.
     *
     * @return the data auth [ ]
     */
    public static DataFilter[] getRow() {
        DataScope dataScope = get();
        if (dataScope != null) {
            DataFilter[] val1 = dataScope.value();
            DataFilter[] val2 = dataScope.row();
            return CollectionUtil.merge(val1, val2);
        }
        return new DataFilter[]{};
    }

    /**
     * Remove.
     *
     * @return
     */
    public static void remove() {
        auths.remove();
    }

    /**
     * Set.
     *
     * @return
     */
    public static void set(DataScope dataScope) {
        auths.set(dataScope);
    }
}
