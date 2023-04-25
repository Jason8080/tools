package cn.gmlee.tools.ds.dynamic;

import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.base.util.NullUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Dynamic data source holder.
 *
 * @author Jas °
 * @date 2020 /8/20 12:14
 */
public class DynamicDataSourceHolder {
    private static final ThreadLocal<String> contextHolder = new ThreadLocal<>();
    /**
     * 谨记: 使注解生效必须通过代理对象调用
     * 警告: 开启事务后, Spring不会再切换数据源; 因此事务内部的注解无效.
     */
    private static final ThreadLocal<Integer> countHolder = new ThreadLocal();

    private static List<String> datasourceKeys = new ArrayList();

    private static final Logger log = LoggerFactory.getLogger(DynamicDataSourceHolder.class);

    /**
     * Gets first.
     *
     * @return the first
     */
    public static String getFirst() {
        if (datasourceKeys.size() > 0) {
            return datasourceKeys.get(0);
        }
        return ExceptionUtil.cast(String.format("动态数据源路由失败: %s", "没有注册任何数据源"));
    }

    /**
     * Count integer.
     *
     * @param i the
     * @return the integer
     */
    public static void count(Integer i) {
        countHolder.set(NullUtil.get(countHolder.get(), 0) + i);
    }

    /**
     * Zero boolean.
     *
     * @return the ds
     */
    public static boolean zero() {
        return NullUtil.get(countHolder.get(), 0) <= 0;
    }

    /**
     * Get string.
     *
     * @return the string
     */
    public static String get() {
        String db = contextHolder.get();
        if (db == null) {
            db = getFirst();
        }
        log.info("数据源: " + db);
        return db;
    }

    /**
     * Set.
     *
     * @param str the str
     */
    public static void set(String str) {
        if (StringUtils.isEmpty(str)) {
            contextHolder.set(getFirst());
        }
        contextHolder.set(str);
    }

    /**
     * Clear.
     */
    public static void clear() {
        countHolder.remove();
        contextHolder.remove();
    }

    /**
     * Add datasource keys.
     *
     * @param datasource the datasource
     */
    public static void addDatasourceKeys(String datasource) {
        datasourceKeys.add(datasource);
    }

    /**
     * Add first datasource keys.
     *
     * @param datasource the datasource
     */
    public static void addFirstDatasourceKeys(String datasource) {
        datasourceKeys.add(0, datasource);
    }

    /**
     * Put first datasource keys.
     *
     * @param datasource the datasource
     */
    public static void putFirstDatasourceKeys(String datasource) {
        if(!datasourceKeys.isEmpty()){
            datasourceKeys.remove(0);
        }
        addFirstDatasourceKeys(datasource);
    }
}
