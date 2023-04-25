package cn.gmlee.tools.mate.interceptor;

import java.util.List;

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
     * 获取数据时根据哪个字段匹配数据权限
     * <p>
     * 多个字段以,号分割;
     * 字段必须在数据返回列中存在.
     * </p>
     *
     * @param flag the flag
     * @return 返回空则不做控制(全部返回).
     */
    default List<String> rowFields(String flag) {
        return null;
    }

    /**
     * 行权限.
     * 根据数据权限标志位获取所有的属性code.
     * <p>
     * 如果希望该函数达到: 不抛异常不返回任何数据, 可以将整个方法的异常扑捉并返回""(空字符串)
     * 如果希望该函数达到: 异常时返回所以数据, 只需要在此方法内抛出异常 (前提是allowEx = false)
     * </p>
     *
     * @param flag the flag 这是查询数据权限的必要标识
     * @return rowIn 最终拼接好的in内容: "1,2,3" (它将会插入到where field_name in (中))
     */
    default String rowIn(String flag) {
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
