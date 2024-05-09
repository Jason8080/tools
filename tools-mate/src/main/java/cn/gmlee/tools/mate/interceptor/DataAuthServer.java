package cn.gmlee.tools.mate.interceptor;

import java.util.List;
import java.util.Map;

/**
 * 数据鉴权服务.
 * <p>
 * 默认不做任何权限处理; 控制器权限需要重写相应的方法.
 * </p>
 */
public interface DataAuthServer {
    /**
     * 是否打印日志.
     *
     * @return the boolean
     */
    default boolean printSql() {
        return false;
    }

    /**
     * 当权限控制程序出错是否允许将异常抛出.
     * <p>
     * 如果设置为true则会导致查询出错
     * 如果设置为false则在程序出错时仅打印日志, 而数据将不再过滤(返回完整数据)
     * </p>
     *
     * @return the boolean
     */
    default boolean allowEx() {
        return false;
    }

    /**
     * 行权限.
     * 根据Map的key作为字段, 其values作为筛选条件, 过滤行数据.
     *
     * <p>
     * 举例: create_by:[1,2,3]
     * 说明: 将在原句柄中增加: where create_by in (1,2,3)
     * 注意: 值类型即list元素类型暂时只支持: Byte、Short、Int、Long、Float、Double、Boolean、Char、String、Date (八大基本类型+时间)
     * </p>
     *
     * @param flag
     * @return
     */
    default Map<String, List<? extends Comparable>> rowMap(String flag) {
        return null;
    }

    /**
     * 列权限.
     * 需要检测权限的字段.
     *
     * @param flag the flag
     * @return 返回空则所有列都不检测鉴权, 返回字段则该字段进行colFilter()校验
     */
    default List<String> colFields(String flag) {
        return null;
    }

    /**
     * 列权限.
     * 判断是否需要过滤该字段.
     *
     * <p>
     * true: 正常返回
     * false: 置空 (权限控制)
     * </p>
     *
     * @param flag   the flag
     * @param column the column
     * @return true : 正常返回, false: 置空 (权限控制)
     */
    default boolean colFilter(String flag, String column) {
        return true;
    }
}
